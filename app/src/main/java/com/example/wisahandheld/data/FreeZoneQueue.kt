package com.example.wisahandheld.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray

/**
 * Local-first queue for Free Zone scans — same idea as SyncManager (Fix
 * Zone), scaled down for Free Zone's simpler shape: a flat list of raw
 * Kanban QR strings, no per-part business key to upsert against. Every
 * scan is written here FIRST — persisted to disk immediately — so leaving
 * this screen, killing the app, or a dropped connection mid-Send can never
 * lose a box that was already scanned.
 *
 * Sends happen two ways, both funneled through the same sendAll(): a
 * background attempt fires automatically after every scan (triggerSync),
 * and the operator can also force one right away via the Send button —
 * syncMutex means the two can never race and double-submit.
 */
object FreeZoneQueue {
    /** Raw Kanban QR text for every box scanned but not yet confirmed saved by the server. Compose-observable so FreeZoneScreen reflects it live, and survives navigating away since it lives here, not in the screen's own state. */
    val scans = mutableStateListOf<String>()

    /**
     * Raw QR text for every box the server has already confirmed saved —
     * persisted (see Prefs.saveFreeZoneSentJson) purely as a "yes, that
     * scan really went through" display for the operator; the server is
     * still the actual source of truth, nothing here is ever re-sent from
     * this list. Capped at MAX_SENT_HISTORY so it can't grow forever across
     * months of use.
     */
    val sentThisSession = mutableStateListOf<String>()

    private const val MAX_SENT_HISTORY = 500

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncMutex = Mutex()

    private var appContext: Context? = null

    /** Call once — e.g. from MainActivity.onCreate — before addScan/removeScan/sendAll are used elsewhere. Restores whatever was still queued (and whatever was already confirmed sent) from before the app was last closed/killed — including a plain restart to pick up new code during development, which happens far more often than an actual "close the app" ever would. */
    fun init(context: Context) {
        if (appContext != null) return
        val ctx = context.applicationContext
        appContext = ctx
        scans.clear()
        scans.addAll(loadAll(ctx))
        sentThisSession.clear()
        sentThisSession.addAll(loadSent(ctx))
    }

    /**
     * FreeZoneScreen's scan field calls this — never talks to the network
     * itself. Every scan is appended as its own entry, even a repeat of
     * the exact same raw text — deliberately NOT deduped/replaced. When a
     * part sits stacked too deep to reach each individual box's own tag,
     * the operator's workaround is re-scanning the one reachable tag once
     * per box they can see, so the same Kanban text scanned N times must
     * count as N physical boxes, not collapse into one (see the design
     * discussion — this was a real request, not an oversight). FreeZoneScreen
     * groups identical raw text back together for display (showing a box
     * COUNT per group), but the underlying queue here stays one entry per
     * physical scan.
     */
    fun addScan(raw: String) {
        scans.add(0, raw)
        persist()
    }

    /** Removes exactly ONE occurrence of this raw text (List.remove already only takes the first match) — the right semantics for the ×-button on a grouped row: tapping it decrements that group's count by one, e.g. correcting an accidental double-scan, rather than clearing the whole group at once. Only ever called on NOT-YET-sent entries — a sentThisSession row has no × in the UI, there's nothing to undo once the server has it. */
    fun removeScan(raw: String) {
        scans.remove(raw)
        persist()
    }

    /** Fires a background sync attempt without blocking the caller — FreeZoneScreen calls this right after every addScan, so most boxes are already confirmed by the time the operator looks up from the scanner. Safe to call often; syncMutex serializes overlapping attempts instead of racing them. */
    fun triggerSync(deviceId: String, batchId: String, employeeName: String) {
        scope.launch { sendAll(deviceId, batchId, employeeName) }
    }

    /**
     * Sends every queued scan in one request. Only raw codes the server
     * actually confirmed saved are removed from `scans` — anything it
     * reports as a failure (unsupported format, bad data, etc.) stays
     * queued so the operator can see it and retry, instead of the old
     * behavior of silently discarding the result no matter what happened.
     * A total failure to reach the server at all (see Api.submitFreeZoneQr)
     * comes back with every code marked failed, so nothing gets cleared
     * and the whole batch stays queued for the next attempt. Confirmed
     * codes move to `sentThisSession` rather than just vanishing.
     */
    suspend fun sendAll(deviceId: String, batchId: String, employeeName: String): Api.FreeZoneQrResult {
        if (scans.isEmpty()) {
            return Api.FreeZoneQrResult(success = true, savedCount = 0, failedCount = 0)
        }
        return syncMutex.withLock {
            if (scans.isEmpty()) return@withLock Api.FreeZoneQrResult(success = true, savedCount = 0, failedCount = 0)

            val toSend = scans.toList()
            val result = Api.submitFreeZoneQr(batchId, deviceId, employeeName, toSend)
            val failedSet = result.failedRaws.toSet()
            val confirmed = toSend.filter { it !in failedSet }

            scans.removeAll { it in toSend && it !in failedSet }
            sentThisSession.addAll(0, confirmed)
            while (sentThisSession.size > MAX_SENT_HISTORY) sentThisSession.removeAt(sentThisSession.size - 1)
            persist()
            persistSent()
            result
        }
    }

    private fun persist() {
        appContext?.let { saveAll(it, scans) }
    }

    private fun persistSent() {
        appContext?.let { saveSent(it, sentThisSession) }
    }

    private fun loadAll(context: Context): List<String> {
        val raw = Prefs.loadFreeZoneScansJson(context) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }
        }.getOrDefault(emptyList())
    }

    private fun saveAll(context: Context, items: List<String>) {
        val arr = JSONArray()
        items.forEach { arr.put(it) }
        Prefs.saveFreeZoneScansJson(context, arr.toString())
    }

    private fun loadSent(context: Context): List<String> {
        val raw = Prefs.loadFreeZoneSentJson(context) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }
        }.getOrDefault(emptyList())
    }

    private fun saveSent(context: Context, items: List<String>) {
        val arr = JSONArray()
        items.forEach { arr.put(it) }
        Prefs.saveFreeZoneSentJson(context, arr.toString())
    }
}
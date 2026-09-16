package com.example.wisahandheld.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray

/**
 * Local-first queue for Free Zone scans — same idea as SyncManager (Fix
 * Zone), scaled down for Free Zone's simpler shape: a flat list of raw
 * Kanban QR strings, no per-part business key to upsert against. Every
 * scan is written here FIRST — persisted to disk immediately — so leaving
 * this screen, killing the app, or a dropped connection mid-Send can never
 * lose a box that was already scanned.
 */
object FreeZoneQueue {
    /** Raw Kanban QR text for every box scanned but not yet confirmed saved by the server. Compose-observable so FreeZoneScreen reflects it live, and survives navigating away since it lives here, not in the screen's own state. */
    val scans = mutableStateListOf<String>()

    private var appContext: Context? = null

    /** Call once — e.g. from MainActivity.onCreate — before addScan/removeScan/sendAll are used elsewhere. Restores whatever was still queued from before the app was last closed/killed. */
    fun init(context: Context) {
        if (appContext != null) return
        val ctx = context.applicationContext
        appContext = ctx
        scans.clear()
        scans.addAll(loadAll(ctx))
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

    /** Removes exactly ONE occurrence of this raw text (List.remove already only takes the first match) — the right semantics for the ×-button on a grouped row: tapping it decrements that group's count by one, e.g. correcting an accidental double-scan, rather than clearing the whole group at once. */
    fun removeScan(raw: String) {
        scans.remove(raw)
        persist()
    }

    /**
     * Sends every queued scan in one request. Only raw codes the server
     * actually confirmed saved are removed from `scans` — anything it
     * reports as a failure (unsupported format, bad data, etc.) stays
     * queued so the operator can see it and retry, instead of the old
     * behavior of silently discarding the result no matter what happened.
     * A total failure to reach the server at all (see Api.submitFreeZoneQr)
     * comes back with every code marked failed, so nothing gets cleared
     * and the whole batch stays queued for the next attempt.
     */
    suspend fun sendAll(deviceId: String, batchId: String, employeeName: String): Api.FreeZoneQrResult {
        if (scans.isEmpty()) {
            return Api.FreeZoneQrResult(success = true, savedCount = 0, failedCount = 0)
        }
        val toSend = scans.toList()
        val result = Api.submitFreeZoneQr(batchId, deviceId, employeeName, toSend)
        val failedSet = result.failedRaws.toSet()
        scans.removeAll { it in toSend && it !in failedSet }
        persist()
        return result
    }

    private fun persist() {
        appContext?.let { saveAll(it, scans) }
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
}
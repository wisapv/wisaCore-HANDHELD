package com.example.wisahandheld.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Local-first sync for Fix Zone counts (see the offline-resilience design
 * discussion). Every Input Stock "Send" / "Not Found" is written here
 * FIRST — to disk, before any network call — so nothing is ever lost to a
 * dropped or flaky connection: the local write always succeeds instantly,
 * the network call is a best-effort follow-up that's free to fail and
 * retry on its own, without the operator ever having to wait on it.
 *
 * `pending` is Compose-observable (mutableStateListOf), so any screen can
 * show "N รายการยังไม่ได้ส่ง" live just by reading it — no separate event
 * bus needed for that.
 */
object SyncManager {
    val pending = mutableStateListOf<PendingCount>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Guards syncNow so an auto-trigger (fired on every enqueue) and a
    // manual "ลองส่งอีกครั้ง" tap can never both be mid-flight at once and
    // submit the same item twice.
    private val syncMutex = Mutex()

    private var appContext: Context? = null

    /** Call once — e.g. from MainActivity.onCreate — before enqueue/triggerSync are used anywhere else. Restores whatever was still pending from before the app was last closed/killed. */
    fun init(context: Context) {
        if (appContext != null) return
        val ctx = context.applicationContext
        appContext = ctx
        pending.clear()
        pending.addAll(PendingCount.loadAll(ctx))
        if (pending.isNotEmpty()) triggerSync()
    }

    /**
     * Input Stock "Send"/"Not Found" call this — and only this; neither
     * screen talks to Api.submitCount directly anymore. Never touches the
     * network itself, so it never blocks the UI.
     */
    fun enqueue(item: PendingCount) {
        // A re-submission of the same part+address (correction) replaces
        // the earlier pending entry instead of stacking a duplicate — the
        // same upsert key the backend itself uses (pic+shortAddr+addr+kbn).
        val existing = pending.indexOfFirst {
            it.pic == item.pic && it.shortAddr == item.shortAddr && it.addr == item.addr && it.kbn == item.kbn
        }
        if (existing >= 0) pending[existing] = item else pending.add(item)
        persist()
        triggerSync()
    }

    /** Fires a background sync attempt without blocking the caller. Safe to call often (every enqueue, every time Fix Zone opens, a pull-to-refresh, etc.) — syncMutex means an already-in-flight attempt just gets skipped rather than racing. */
    fun triggerSync() {
        scope.launch { syncNow() }
    }

    /**
     * Sends every pending item, grouped into one bulk request per distinct
     * batchId (in practice almost always just one group). Only items the
     * server actually confirmed are removed from `pending` — everything
     * else (no connection, or a per-row failure the server reported) stays
     * queued for the next attempt. Returns true only if the queue ended up
     * fully empty.
     */
    suspend fun syncNow(): Boolean {
        val context = appContext ?: return false
        if (pending.isEmpty()) return true
        if (!isOnline(context)) return false

        return syncMutex.withLock {
            if (pending.isEmpty()) return@withLock true

            val byBatch = pending.toList().groupBy { it.batchId }
            for ((batchId, items) in byBatch) {
                val deviceId = items.first().deviceId
                val result = Api.submitCountsBulk(batchId, deviceId, items)
                if (result == null) continue // couldn't reach the server at all — leave this whole group queued

                items.forEachIndexed { i, item ->
                    val ok = result.getOrNull(i)?.success == true
                    if (ok) pending.remove(item)
                }
            }
            persist()
            pending.isEmpty()
        }
    }

    private fun persist() {
        appContext?.let { PendingCount.saveAll(it, pending) }
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

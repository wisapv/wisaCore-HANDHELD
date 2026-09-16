package com.example.wisahandheld.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * One Input Stock submission ("Send" or "Not Found") that has been written
 * to disk but not yet confirmed saved by the server — see SyncManager,
 * which is the only thing that ever adds/removes items here.
 *
 * Mirrors every field Api.submitCount/submitCountsBulk needs. `localId` is
 * generated once at enqueue time purely so the UI (a list of pending items)
 * has a stable key — it's never sent to the backend, which still
 * identifies a row the same way it always has: (pic, shortAddr, addr, kbn).
 */
data class PendingCount(
    val localId: String = UUID.randomUUID().toString(),
    val batchId: String,
    val deviceId: String,
    val pic: String,
    val shortAddr: String,
    val addr: String,
    val kbn: String,
    val partNo: String,
    val partName: String,
    val supplier: String,
    val shop: String,
    val dock: String,
    val sPlant: String,
    val sDock: String,
    val qty: Int,
    val box: String,
    val pcs: String,
    val seq: String,
    val notFound: Boolean,
    val employeeName: String,
    val employeePhone: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("localId", localId); put("batchId", batchId); put("deviceId", deviceId)
        put("pic", pic); put("shortAddr", shortAddr); put("addr", addr); put("kbn", kbn)
        put("partNo", partNo); put("partName", partName); put("supplier", supplier)
        put("shop", shop); put("dock", dock); put("sPlant", sPlant); put("sDock", sDock)
        put("qty", qty); put("box", box); put("pcs", pcs); put("seq", seq)
        put("notFound", notFound); put("employeeName", employeeName); put("employeePhone", employeePhone)
        put("createdAt", createdAt)
    }

    companion object {
        fun fromJson(o: JSONObject): PendingCount = PendingCount(
            localId = o.optString("localId").ifBlank { UUID.randomUUID().toString() },
            batchId = o.optString("batchId"),
            deviceId = o.optString("deviceId"),
            pic = o.optString("pic"),
            shortAddr = o.optString("shortAddr"),
            addr = o.optString("addr"),
            kbn = o.optString("kbn"),
            partNo = o.optString("partNo"),
            partName = o.optString("partName"),
            supplier = o.optString("supplier"),
            shop = o.optString("shop"),
            dock = o.optString("dock"),
            sPlant = o.optString("sPlant"),
            sDock = o.optString("sDock"),
            qty = o.optInt("qty"),
            box = o.optString("box"),
            pcs = o.optString("pcs"),
            seq = o.optString("seq"),
            notFound = o.optBoolean("notFound"),
            employeeName = o.optString("employeeName"),
            employeePhone = o.optString("employeePhone"),
            createdAt = o.optLong("createdAt", System.currentTimeMillis())
        )

        /** Restores the queue on app start (see SyncManager.init) — a corrupt/unreadable blob is treated as empty rather than crashing the app. */
        fun loadAll(context: Context): List<PendingCount> {
            val raw = Prefs.loadPendingCountsJson(context) ?: return emptyList()
            return runCatching {
                val arr = JSONArray(raw)
                (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
            }.getOrDefault(emptyList())
        }

        /** Called after every queue mutation (enqueue, and after a sync removes confirmed items) — see SyncManager.persist. */
        fun saveAll(context: Context, items: List<PendingCount>) {
            val arr = JSONArray()
            items.forEach { arr.put(it.toJson()) }
            Prefs.savePendingCountsJson(context, arr.toString())
        }
    }
}

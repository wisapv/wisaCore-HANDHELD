package com.example.wisahandheld.data

import android.content.Context

/**
 * Simple persisted state so closing/reopening the app doesn't force
 * re-entering the device code or employee info every time. Device code is
 * effectively permanent per physical device (set once at Login). Employee
 * info persists until the person explicitly taps "เปลี่ยนคน" and checks in
 * again — it's who's holding the device *right now*, not tied to the app
 * process being alive.
 */
object Prefs {
    private const val NAME = "wisahandheld_prefs"
    private const val KEY_DEVICE_CODE = "device_code"
    private const val KEY_EMPLOYEE_NAME = "employee_name"
    private const val KEY_EMPLOYEE_PHONE = "employee_phone"
    private const val KEY_WORK_BATCH_ID = "work_batch_id"

    // Fix Zone's local-first pending queue (see PendingCount/SyncManager) —
    // one JSON array string holding every count not yet confirmed by the
    // server. Deliberately app-wide, not scoped to the current batch: each
    // item already carries its own batchId, so nothing is lost if the
    // operator switches work batch while something is still queued.
    private const val KEY_PENDING_COUNTS = "pending_counts_json"

    // Free Zone's local-first queue (see FreeZoneQueue) — one JSON array of
    // raw Kanban QR strings not yet confirmed saved by the server.
    private const val KEY_FREE_ZONE_SCANS = "free_zone_scans_json"

    // Free Zone's confirmed-sent history (see FreeZoneQueue.sentThisSession)
    // — was deliberately in-memory-only at first, but every code update
    // during development restarts the app process just as much as an
    // actual close ever would, so it kept looking like real data had
    // vanished when it hadn't. Persisting it costs nothing (the real data
    // is on the server either way; this is purely a "yes that really went
    // through" display) and matches what people actually expect to see
    // survive a restart.
    private const val KEY_FREE_ZONE_SENT = "free_zone_sent_json"

    fun saveDeviceCode(context: Context, code: String) {
        prefs(context).edit().putString(KEY_DEVICE_CODE, code).apply()
    }

    fun saveEmployee(context: Context, name: String, phone: String) {
        prefs(context).edit()
            .putString(KEY_EMPLOYEE_NAME, name)
            .putString(KEY_EMPLOYEE_PHONE, phone)
            .apply()
    }

    /** Which batch (Part Runout active batch, or a specific Getsudo batch) was picked on WorkModeScreen — survives closing the app, so it isn't asked again every cold start. Cleared when checking in again as a different person. */
    fun saveWorkBatch(context: Context, batchId: String?) {
        prefs(context).edit().putString(KEY_WORK_BATCH_ID, batchId).apply()
    }

    fun loadDeviceCode(context: Context): String? = prefs(context).getString(KEY_DEVICE_CODE, null)
    fun loadEmployeeName(context: Context): String? = prefs(context).getString(KEY_EMPLOYEE_NAME, null)
    fun loadEmployeePhone(context: Context): String? = prefs(context).getString(KEY_EMPLOYEE_PHONE, null)
    fun loadWorkBatch(context: Context): String? = prefs(context).getString(KEY_WORK_BATCH_ID, null)

    /** Raw JSON array string of every pending count — see PendingCount.saveAll/loadAll, which own the actual (de)serialization. Written on every queue change, not just on app close, since the whole point is surviving a kill/crash mid-count. */
    fun savePendingCountsJson(context: Context, json: String) {
        prefs(context).edit().putString(KEY_PENDING_COUNTS, json).apply()
    }

    fun loadPendingCountsJson(context: Context): String? = prefs(context).getString(KEY_PENDING_COUNTS, null)

    /** Raw JSON array string of every not-yet-sent Free Zone scan — see FreeZoneQueue.saveAll/loadAll. */
    fun saveFreeZoneScansJson(context: Context, json: String) {
        prefs(context).edit().putString(KEY_FREE_ZONE_SCANS, json).apply()
    }

    fun loadFreeZoneScansJson(context: Context): String? = prefs(context).getString(KEY_FREE_ZONE_SCANS, null)

    /** Raw JSON array string of every confirmed-sent Free Zone scan (display history only — the server is still the real source of truth). */
    fun saveFreeZoneSentJson(context: Context, json: String) {
        prefs(context).edit().putString(KEY_FREE_ZONE_SENT, json).apply()
    }

    fun loadFreeZoneSentJson(context: Context): String? = prefs(context).getString(KEY_FREE_ZONE_SENT, null)

    private fun prefs(context: Context) = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}
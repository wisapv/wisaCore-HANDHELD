package com.example.wisahandheld.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Talks to the same backend the wisaCore web app (wisaCore-DASHINV/backend)
 * uses — no separate API, this device just calls the same server.
 *
 * IMPORTANT: change BASE_URL below to your server's actual LAN IP before
 * testing on a real device (e.g. "http://192.168.1.50:3000"). "localhost"
 * only works from the Android emulator talking to a server on the SAME
 * computer, and even then you must use "10.0.2.2", not "localhost" — the
 * emulator has its own loopback separate from your PC's.
 *
 * Uses plain HttpURLConnection (built into Android, no Gradle dependency
 * to add/sync) rather than Retrofit/OkHttp, to keep this simple to review
 * and drop in. Swap it for Retrofit later if the API surface grows.
 */
object Api {
    // Emulator talking to a server on the SAME computer → use 10.0.2.2
    // (the emulator's special alias for the host machine's localhost).
    // var BASE_URL = "http://10.0.2.2:3000"

    // Real Zebra device on WiFi → comment the line above and uncomment
    // this one instead. 172.20.10.3 was your PC's IP when this was set —
    // re-check with ipconfig if it changes (e.g. reconnecting to a
    // different WiFi/hotspot), and make sure the Zebra device joins the
    // SAME WiFi/hotspot as this PC.
    var BASE_URL = "http://172.20.10.3:3000"

    data class Job(val code: String, val pic: String, val itemCount: Int)
    data class JobAddress(val addr: String, val remain: Int, val done: Boolean)
    data class AddressDetailRow(
        val supplier: String, val kbn: String, val address: String, val partName: String, val partNo: String,
        val shop: String, val dock: String, val sPlant: String, val sDock: String, val qty: String
    )
    data class Device(val id: String, val name: String, val status: String)

    /** One part somewhere in an assigned zone — flat across all its addresses (replaces address-by-address paging). */
    data class ZonePart(
        val supplier: String, val shop: String, val dock: String, val sPlant: String, val sDock: String,
        val kbn: String, val address: String, val partName: String, val partNo: String, val qty: String,
        val counted: Boolean, val countedQty: Int?, val countedBox: String?, val countedPcs: String?,
        val countedSeq: String?, val countedNotFound: Boolean
    )

    /** GET /api/handheld-assign/job-zone-parts — every part in a zone, flat, with counted/previous-submission info for edit mode. */
    suspend fun fetchJobZoneParts(batchId: String, deviceId: String, pic: String, shortAddr: String): List<ZonePart>? =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = get(
                    "$BASE_URL/api/handheld-assign/job-zone-parts?batchId=${enc(batchId)}&deviceId=${enc(deviceId)}" +
                            "&pic=${enc(pic)}&shortAddr=${enc(shortAddr)}"
                )
                val arr: JSONArray = json.optJSONArray("data") ?: JSONArray()
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    ZonePart(
                        supplier = o.optString("supplier"), shop = o.optString("shop"), dock = o.optString("dock"),
                        sPlant = o.optString("sPlant"), sDock = o.optString("sDock"), kbn = o.optString("kbn"),
                        address = o.optString("address"), partName = o.optString("partName"), partNo = o.optString("partNo"),
                        qty = o.optString("qty"), counted = o.optBoolean("counted"),
                        countedQty = if (o.isNull("countedQty")) null else o.optInt("countedQty"),
                        countedBox = if (o.isNull("countedBox")) null else o.optString("countedBox"),
                        countedPcs = if (o.isNull("countedPcs")) null else o.optString("countedPcs"),
                        countedSeq = if (o.isNull("countedSeq")) null else o.optString("countedSeq"),
                        countedNotFound = o.optBoolean("countedNotFound")
                    )
                }
            }.onFailure { Log.e(TAG, "fetchJobZoneParts failed", it) }.getOrNull()
        }

    /** GET /api/handheld-devices — the registered device list (Login shows the active ones as a picker). */
    suspend fun fetchActiveDevices(): List<Device>? = withContext(Dispatchers.IO) {
        runCatching {
            val json = get("$BASE_URL/api/handheld-devices")
            val arr: JSONArray = json.optJSONArray("data") ?: JSONArray()
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Device(id = o.optString("id"), name = o.optString("name"), status = o.optString("status"))
            }.filter { it.status == "active" }
        }.onFailure { Log.e(TAG, "fetchActiveDevices failed", it) }.getOrNull()
    }

    /** GET /api/part-list/current-batch — which batch is currently active on the web. */
    /** Which batches this device actually has work in — Part Runout (the app-wide active batch) and/or one or more Getsudo batches, independently. */
    data class WorkModes(val tbosBatchId: String?, val getsudoBatchIds: List<String>)

    /** GET /api/handheld-assign/my-work-modes — used right after check-in to decide whether to show the "which work?" picker (only when both apply) or skip straight to Home. */
    suspend fun fetchWorkModes(deviceId: String): WorkModes? = withContext(Dispatchers.IO) {
        runCatching {
            val json = get("$BASE_URL/api/handheld-assign/my-work-modes?deviceId=${enc(deviceId)}")
            val tbos = json.optJSONObject("tbos")
            val getsudoArr = json.optJSONArray("getsudo") ?: JSONArray()
            WorkModes(
                tbosBatchId = tbos?.optString("batchId"),
                getsudoBatchIds = (0 until getsudoArr.length()).map { getsudoArr.getJSONObject(it).optString("batchId") }
            )
        }.onFailure { Log.e(TAG, "fetchWorkModes failed", it) }.getOrNull()
    }

    suspend fun fetchCurrentBatchId(): String? = withContext(Dispatchers.IO) {
        runCatching {
            val json = get("$BASE_URL/api/part-list/current-batch")
            val id = json.optString("batchId")
            id.takeIf { it.isNotBlank() && it != "null" }
        }.onFailure { Log.e(TAG, "fetchCurrentBatchId failed", it) }.getOrNull()
    }

    /** GET /api/handheld-assign/my-jobs — the address groups assigned to this device for this batch. */
    suspend fun fetchMyJobs(batchId: String, deviceId: String): List<Job>? = withContext(Dispatchers.IO) {
        runCatching {
            val json = get("$BASE_URL/api/handheld-assign/my-jobs?batchId=${enc(batchId)}&deviceId=${enc(deviceId)}")
            val arr: JSONArray = json.optJSONArray("data") ?: JSONArray()
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Job(code = o.optString("code"), pic = o.optString("pic"), itemCount = o.optInt("itemCount"))
            }
        }.onFailure { Log.e(TAG, "fetchMyJobs failed", it) }.getOrNull()
    }

    /** One Free Zone this device is assigned to — see FreeZoneAssignment. */
    data class FreeZoneAssignment(val code: String, val dock: String)

    /** GET /api/handheld-assign/my-free-zones — "does this device have a Free Zone assignment, and if so which
     *  zone(s)?" Home uses this (alongside fetchMyJobs) to decide whether the Free zone entry point should even
     *  show — previously it always showed regardless of what this device was actually assigned to. */
    suspend fun fetchMyFreeZones(batchId: String, deviceId: String): List<FreeZoneAssignment>? = withContext(Dispatchers.IO) {
        runCatching {
            val json = get("$BASE_URL/api/handheld-assign/my-free-zones?batchId=${enc(batchId)}&deviceId=${enc(deviceId)}")
            val arr: JSONArray = json.optJSONArray("data") ?: JSONArray()
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                FreeZoneAssignment(code = o.optString("code"), dock = o.optString("dock"))
            }
        }.onFailure { Log.e(TAG, "fetchMyFreeZones failed", it) }.getOrNull()
    }

    /** GET /api/handheld-assign/job-addresses — physical addresses inside one assigned zone (Select Address). */
    suspend fun fetchJobAddresses(batchId: String, deviceId: String, pic: String, shortAddr: String): List<JobAddress>? =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = get(
                    "$BASE_URL/api/handheld-assign/job-addresses?batchId=${enc(batchId)}&deviceId=${enc(deviceId)}" +
                            "&pic=${enc(pic)}&shortAddr=${enc(shortAddr)}"
                )
                val arr: JSONArray = json.optJSONArray("data") ?: JSONArray()
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    JobAddress(addr = o.optString("addr"), remain = o.optInt("remain"), done = o.optBoolean("done"))
                }
            }.onFailure { Log.e(TAG, "fetchJobAddresses failed", it) }.getOrNull()
        }

    /** GET /api/handheld-assign/job-address-detail — the part rows still remaining at one specific address (Address Detail). */
    suspend fun fetchJobAddressDetail(batchId: String, deviceId: String, pic: String, shortAddr: String, addr: String): List<AddressDetailRow>? =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = get(
                    "$BASE_URL/api/handheld-assign/job-address-detail?batchId=${enc(batchId)}&deviceId=${enc(deviceId)}" +
                            "&pic=${enc(pic)}&shortAddr=${enc(shortAddr)}&addr=${enc(addr)}"
                )
                val arr: JSONArray = json.optJSONArray("data") ?: JSONArray()
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    AddressDetailRow(
                        supplier = o.optString("supplier"),
                        kbn = o.optString("kbn"),
                        address = o.optString("address"),
                        partName = o.optString("partName"),
                        partNo = o.optString("partNo"),
                        shop = o.optString("shop"),
                        dock = o.optString("dock"),
                        sPlant = o.optString("sPlant"),
                        sDock = o.optString("sDock"),
                        qty = o.optString("qty")
                    )
                }
            }.onFailure { Log.e(TAG, "fetchJobAddressDetail failed", it) }.getOrNull()
        }

    /** POST /api/handheld-assign/submit-count — Input Stock "Send"/"Not Found". Overwrites any earlier submission for the same part+address. */
    suspend fun submitCount(
        batchId: String, deviceId: String, pic: String, shortAddr: String, addr: String, kbn: String,
        partNo: String, partName: String, supplier: String, shop: String, dock: String, sPlant: String, sDock: String,
        qty: Int, box: String, pcs: String, seq: String, notFound: Boolean, employeeName: String, employeePhone: String
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val body = JSONObject().apply {
                put("batchId", batchId); put("deviceId", deviceId); put("pic", pic); put("shortAddr", shortAddr)
                put("addr", addr); put("kbn", kbn); put("partNo", partNo); put("partName", partName)
                put("supplier", supplier); put("shop", shop); put("dock", dock); put("sPlant", sPlant); put("sDock", sDock)
                put("qty", qty); put("box", box); put("pcs", pcs); put("seq", seq); put("notFound", notFound)
                put("employeeName", employeeName); put("employeePhone", employeePhone)
            }
            val json = post("$BASE_URL/api/handheld-assign/submit-count", body)
            json.optBoolean("success")
        }.onFailure { Log.e(TAG, "submitCount failed", it) }.getOrDefault(false)
    }

    /** POST /api/handheld-assign/submit-free-zone — Free Zone "Send". Box counts here ADD to any existing total for that barcode.
     *  @deprecated superseded by submitFreeZoneQr, which sends the real Kanban QR text and lets the backend decode it
     *  (part no/qty/order/box seq all come from the QR itself instead of a manual tap-count) — kept only in case
     *  anything still calls it during the transition. */
    suspend fun submitFreeZone(batchId: String, deviceId: String, employeeName: String, items: List<Pair<String, Int>>): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val itemsArr = JSONArray()
                items.forEach { (barcode, boxCount) ->
                    itemsArr.put(JSONObject().apply { put("barcode", barcode); put("boxCount", boxCount) })
                }
                val body = JSONObject().apply {
                    put("batchId", batchId); put("deviceId", deviceId); put("employeeName", employeeName)
                    put("items", itemsArr)
                }
                val json = post("$BASE_URL/api/handheld-assign/submit-free-zone", body)
                json.optBoolean("success")
            }.onFailure { Log.e(TAG, "submitFreeZone failed", it) }.getOrDefault(false)
        }

    /** Result of submitFreeZoneQr — savedCount/failedCount so the screen can tell the operator "5 sent, 1 failed"
     *  instead of a flat pass/fail for what might be a mixed batch of boxes. `failedRaws` carries the actual raw
     *  QR text of every box that failed (not just how many) — FreeZoneQueue needs this to know exactly which
     *  entries to keep queued for retry and which to clear as confirmed. */
    data class FreeZoneQrResult(
        val success: Boolean,
        val savedCount: Int,
        val failedCount: Int,
        val failedRaws: List<String> = emptyList()
    )

    /** POST /api/handheld-assign/submit-free-zone-qr — Free Zone "Send", QR-based. Sends the untouched raw text of
     *  every scanned Kanban QR; the backend decodes each one itself (decodeLocalFreeZoneQr) and upserts into
     *  handheld_free_zone_scans keyed by (order_number, part_no, box_seq) — re-sending the same box corrects it,
     *  it does not double-count. Process Stock sums qty across every scan for the same part on its own, so this
     *  call never needs to pre-aggregate anything — one raw string per physical box scanned is enough. */
    suspend fun submitFreeZoneQr(batchId: String, deviceId: String, employeeName: String, qrCodes: List<String>): FreeZoneQrResult =
        withContext(Dispatchers.IO) {
            runCatching {
                val codesArr = JSONArray()
                qrCodes.forEach { codesArr.put(it) }
                val body = JSONObject().apply {
                    put("batchId", batchId); put("deviceId", deviceId); put("employeeName", employeeName)
                    put("qrCodes", codesArr)
                }
                val json = post("$BASE_URL/api/handheld-assign/submit-free-zone-qr", body)
                val savedCount = json.optInt("savedCount", 0)
                val failuresArr = json.optJSONArray("failures") ?: JSONArray()
                val failedRaws = (0 until failuresArr.length()).map { failuresArr.getJSONObject(it).optString("raw") }
                FreeZoneQrResult(
                    success = json.optBoolean("success"), savedCount = savedCount,
                    failedCount = failedRaws.size, failedRaws = failedRaws
                )
            }.onFailure { Log.e(TAG, "submitFreeZoneQr failed", it) }
                // Couldn't even reach the server — treat every code sent as failed/unsent rather than as an
                // unknown, so FreeZoneQueue.sendAll's "keep only what's in failedRaws" logic leaves everything
                // queued for retry instead of accidentally clearing boxes that were never actually confirmed.
                .getOrDefault(FreeZoneQrResult(success = false, savedCount = 0, failedCount = qrCodes.size, failedRaws = qrCodes))
        }

    /** One item's outcome from submitCountsBulk — index-aligned with the request's `counts` list, same order in and out. */
    data class BulkSubmitItemResult(val success: Boolean, val error: String? = null)

    /** POST /api/handheld-assign/submit-counts-bulk — bulk variant of submitCount, built for SyncManager's
     *  local-first queue: one request replays every not-yet-confirmed count instead of one request per item,
     *  so a spotty connection costs one retry of the whole batch, not N separate timeouts.
     *  Returns null on a total failure (couldn't even reach the server) — SyncManager treats that as
     *  "leave everything queued", as opposed to a real response where each item has its own success/failure. */
    suspend fun submitCountsBulk(batchId: String, deviceId: String, items: List<PendingCount>): List<BulkSubmitItemResult>? =
        withContext(Dispatchers.IO) {
            runCatching {
                val countsArr = JSONArray()
                items.forEach { c ->
                    countsArr.put(JSONObject().apply {
                        put("pic", c.pic); put("shortAddr", c.shortAddr); put("addr", c.addr); put("kbn", c.kbn)
                        put("partNo", c.partNo); put("partName", c.partName); put("supplier", c.supplier)
                        put("shop", c.shop); put("dock", c.dock); put("sPlant", c.sPlant); put("sDock", c.sDock)
                        put("qty", c.qty); put("box", c.box); put("pcs", c.pcs); put("seq", c.seq)
                        put("notFound", c.notFound); put("employeeName", c.employeeName); put("employeePhone", c.employeePhone)
                    })
                }
                val body = JSONObject().apply {
                    put("batchId", batchId); put("deviceId", deviceId); put("counts", countsArr)
                }
                val json = post("$BASE_URL/api/handheld-assign/submit-counts-bulk", body)
                val resultsArr = json.optJSONArray("results") ?: JSONArray()
                (0 until resultsArr.length()).map { i ->
                    val o = resultsArr.getJSONObject(i)
                    BulkSubmitItemResult(
                        success = o.optBoolean("success"),
                        error = o.optString("error").takeIf { it.isNotBlank() }
                    )
                }
            }.onFailure { Log.e(TAG, "submitCountsBulk failed", it) }.getOrNull()
        }

    /** POST /api/handheld-assign/heartbeat — fire-and-forget "I'm alive" ping (see SyncManager and the
     *  real-time/offline-badge design discussion). A failure here is EXPECTED whenever offline — it must
     *  never be surfaced to the operator, it's just best-effort dashboard liveness, nothing else depends on it. */
    suspend fun sendHeartbeat(deviceId: String, batchId: String?) {
        withContext(Dispatchers.IO) {
            runCatching {
                val body = JSONObject().apply {
                    put("deviceId", deviceId); put("batchId", batchId)
                }
                post("$BASE_URL/api/handheld-assign/heartbeat", body)
            }
        }
    }

    private const val TAG = "WisaApi"

    /** POST /api/handheld-assign/checkin — audit log entry, fire-and-forget (a failure here must never block getting to Home). */
    suspend fun logCheckIn(batchId: String?, deviceId: String, employeeId: String, employeePhone: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val body = JSONObject().apply {
                    put("batchId", batchId); put("deviceId", deviceId)
                    put("employeeId", employeeId); put("employeePhone", employeePhone)
                }
                val json = post("$BASE_URL/api/handheld-assign/checkin", body)
                json.optBoolean("success")
            }.onFailure { Log.e(TAG, "logCheckIn failed", it) }.getOrDefault(false)
        }

    private fun enc(value: String): String = URLEncoder.encode(value, "UTF-8")

    /** Thrown when the server responds with a non-2xx status — carries the server's own error message when it sent one. */
    class ApiException(val httpCode: Int, message: String) : Exception(message)

    private fun get(urlString: String): JSONObject {
        val conn = URL(urlString).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        return try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: "{}"
            // A response can be non-2xx and still have a perfectly valid JSON
            // body (our own error handlers always send { error: "..." }) —
            // without this check that error body gets silently parsed as if
            // it were successful data.
            if (code !in 200..299) {
                val serverMessage = runCatching { JSONObject(text).optString("error") }.getOrNull()
                throw ApiException(code, serverMessage?.takeIf { it.isNotBlank() } ?: "HTTP $code")
            }
            JSONObject(text)
        } finally {
            conn.disconnect()
        }
    }

    private fun post(urlString: String, body: JSONObject): JSONObject {
        val conn = URL(urlString).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        return try {
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: "{}"
            if (code !in 200..299) {
                val serverMessage = runCatching { JSONObject(text).optString("error") }.getOrNull()
                throw ApiException(code, serverMessage?.takeIf { it.isNotBlank() } ?: "HTTP $code")
            }
            JSONObject(text)
        } finally {
            conn.disconnect()
        }
    }
}
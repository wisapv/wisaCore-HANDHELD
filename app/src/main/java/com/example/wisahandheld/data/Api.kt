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
    var BASE_URL = "http://10.0.2.2:3000"

    // Real Zebra device on WiFi → comment the line above and uncomment
    // this one instead. 172.20.10.3 was your PC's IP when this was set —
    // re-check with ipconfig if it changes (e.g. reconnecting to a
    // different WiFi/hotspot), and make sure the Zebra device joins the
    // SAME WiFi/hotspot as this PC.
    // var BASE_URL = "http://172.20.10.3:3000"

    data class Job(val code: String, val pic: String, val itemCount: Int)
    data class JobAddress(val addr: String, val remain: Int, val done: Boolean)
    data class AddressDetailRow(
        val supplier: String, val kbn: String, val address: String, val partName: String, val partNo: String,
        val shop: String, val dock: String, val sPlant: String, val sDock: String, val qty: String
    )
    data class Device(val id: String, val name: String, val status: String)

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

    /** POST /api/handheld-assign/submit-free-zone — Free Zone "Send". Box counts here ADD to any existing total for that barcode. */
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
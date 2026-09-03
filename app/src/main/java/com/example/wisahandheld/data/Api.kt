package com.example.wisahandheld.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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

    /** GET /api/part-list/current-batch — which batch is currently active on the web. */
    suspend fun fetchCurrentBatchId(): String? = withContext(Dispatchers.IO) {
        runCatching {
            val json = get("$BASE_URL/api/part-list/current-batch")
            val id = json.optString("batchId")
            id.takeIf { it.isNotBlank() && it != "null" }
        }.getOrNull()
    }

    /** GET /api/handheld-assign/my-jobs — the address groups assigned to this device for this batch. */
    suspend fun fetchMyJobs(batchId: String, deviceId: String): List<Job> = withContext(Dispatchers.IO) {
        runCatching {
            val json = get("$BASE_URL/api/handheld-assign/my-jobs?batchId=$batchId&deviceId=$deviceId")
            val arr: JSONArray = json.optJSONArray("data") ?: JSONArray()
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Job(code = o.optString("code"), pic = o.optString("pic"), itemCount = o.optInt("itemCount"))
            }
        }.getOrDefault(emptyList())
    }

    private fun get(urlString: String): JSONObject {
        val conn = URL(urlString).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        return try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: "{}"
            JSONObject(text)
        } finally {
            conn.disconnect()
        }
    }
}
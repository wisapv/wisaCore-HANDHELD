package com.example.wisahandheld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.wisahandheld.data.Api
import com.example.wisahandheld.data.Prefs
import com.example.wisahandheld.ui.screens.*
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.WISAHANDHELDTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WISAHANDHELDTheme {
                val context = LocalContext.current

                // Restore saved login/check-in so closing and reopening the
                // app doesn't force re-entering everything (see Prefs.kt).
                var deviceCode by remember { mutableStateOf(Prefs.loadDeviceCode(context) ?: "") }
                var employeeName by remember { mutableStateOf(Prefs.loadEmployeeName(context) ?: "") }
                var employeePhone by remember { mutableStateOf(Prefs.loadEmployeePhone(context) ?: "") }
                var currentScreen by remember {
                    mutableStateOf(
                        when {
                            deviceCode.isNotBlank() && employeeName.isNotBlank() -> "Home"
                            deviceCode.isNotBlank() -> "CheckIn"
                            else -> "Overview"
                        }
                    )
                }

                var selectedJob by remember { mutableStateOf<ZoneJob?>(null) }
                // The part matched (scanned/typed/tapped) in the zone screen — carries
                // everything Input Stock needs, plus whether it was already counted.
                var selectedPart by remember { mutableStateOf<KbnRow?>(null) }
                var currentBatchId by remember { mutableStateOf<String?>(null) }

                val scope = rememberCoroutineScope()

                // "idle" / "loading" / "ready" / "error" for each fetch below.
                var jobsStatus by remember { mutableStateOf("idle") }
                var jobs by remember { mutableStateOf<List<ZoneJob>>(emptyList()) }

                var zonePartsStatus by remember { mutableStateOf("idle") }
                var zoneParts by remember { mutableStateOf<List<KbnRow>>(emptyList()) }

                fun loadJobs() {
                    scope.launch {
                        jobsStatus = "loading"
                        val batchId = Api.fetchCurrentBatchId()
                        if (batchId == null) {
                            jobsStatus = "error"
                            return@launch
                        }
                        currentBatchId = batchId
                        val result = Api.fetchMyJobs(batchId, deviceCode)
                        if (result == null) {
                            jobsStatus = "error"
                            return@launch
                        }
                        jobs = result.map { ZoneJob(code = it.code, pic = it.pic, itemCount = it.itemCount) }
                        jobsStatus = "ready"
                    }
                }

                // Replaces the old loadAddresses + loadAddressDetail two-step —
                // one flat fetch of everything in the zone (see AddressDetailScreen).
                fun loadZoneParts(job: ZoneJob) {
                    scope.launch {
                        zonePartsStatus = "loading"
                        val batchId = currentBatchId ?: Api.fetchCurrentBatchId()
                        if (batchId == null) {
                            zonePartsStatus = "error"
                            return@launch
                        }
                        currentBatchId = batchId
                        val result = Api.fetchJobZoneParts(batchId, deviceCode, job.pic, job.code)
                        if (result == null) {
                            zonePartsStatus = "error"
                            return@launch
                        }
                        zoneParts = result.map {
                            KbnRow(
                                supplier = it.supplier, kbn = it.kbn, address = it.address, partName = it.partName,
                                partNo = it.partNo, shop = it.shop, dock = it.dock, sPlant = it.sPlant, sDock = it.sDock,
                                qty = it.qty, counted = it.counted, countedQty = it.countedQty, countedBox = it.countedBox,
                                countedPcs = it.countedPcs, countedSeq = it.countedSeq
                            )
                        }
                        zonePartsStatus = "ready"
                    }
                }

                val remainingCount = jobs.sumOf { it.itemCount } // itemCount is already "remaining" — see my-jobs backend

                // The part matched on the zone screen becomes the "scanned" result
                // on Input Stock. If it was already counted, qty shown is what was
                // submitted last time (edit mode); otherwise it's the expected qty
                // from the batch's own data.
                val scanResult = remember(selectedPart) {
                    val row = selectedPart
                    ScanResult(
                        kbn = row?.kbn ?: "",
                        address = row?.address ?: "",
                        partName = row?.partName ?: "",
                        qty = row?.countedQty ?: row?.qty?.toIntOrNull() ?: 0
                    )
                }

                when (currentScreen) {
                    "Overview" -> OverviewScreen(onGetStarted = {
                        scope.launch { delay(150); currentScreen = "Login" }
                    })

                    "Login" -> LoginScreen(
                        onLoggedIn = { code ->
                            scope.launch {
                                delay(150)
                                deviceCode = code
                                Prefs.saveDeviceCode(context, code)
                                currentScreen = "CheckIn"
                            }
                        },
                        onBack = { scope.launch { delay(120); currentScreen = "Overview" } }
                    )

                    "CheckIn" -> EmployeeCheckInScreen(
                        deviceCode = deviceCode,
                        onCheckedIn = { id, phone ->
                            scope.launch {
                                delay(150)
                                employeeName = id
                                employeePhone = phone
                                Prefs.saveEmployee(context, id, phone)
                                currentScreen = "Home"
                                loadJobs()
                                Api.logCheckIn(currentBatchId, deviceCode, id, phone) // audit log — fire and forget
                            }
                        },
                        onBack = { scope.launch { delay(120); currentScreen = "Login" } }
                    )

                    "Home" -> {
                        // First launch straight into Home (both device code
                        // and employee already saved from a previous run) —
                        // kick off the jobs fetch once, since CheckIn's
                        // onCheckedIn (which normally triggers it) was skipped.
                        LaunchedEffect(Unit) {
                            if (jobsStatus == "idle") loadJobs()
                        }
                        HomeScreen(
                            deviceCode = deviceCode,
                            employeeName = employeeName,
                            phone = employeePhone,
                            zonesToday = jobs.size,
                            remainingCount = remainingCount,
                            onOpenPartList = { scope.launch { delay(150); currentScreen = "PartList" } },
                            onOpenFreeZone = { scope.launch { delay(150); currentScreen = "FreeZone" } },
                            onChangePerson = { scope.launch { delay(150); currentScreen = "CheckIn" } },
                            onBack = { scope.launch { delay(120); currentScreen = "CheckIn" } }
                        )
                    }

                    "PartList" -> when (jobsStatus) {
                        "loading", "idle" -> Box(
                            modifier = Modifier.fillMaxSize().background(Canvas),
                            contentAlignment = Alignment.Center
                        ) { Text(text = "กำลังโหลดงาน…", color = Ink) }

                        "error" -> Box(
                            modifier = Modifier.fillMaxSize().background(Canvas).clickable { loadJobs() },
                            contentAlignment = Alignment.Center
                        ) { Text(text = "โหลดงานไม่สำเร็จ — เช็คว่าเครื่องต่อ WiFi เดียวกับเซิร์ฟเวอร์อยู่ไหม (แตะเพื่อลองใหม่)", color = Ink) }

                        else -> PartListScreen(
                            deviceCode = deviceCode,
                            jobs = jobs,
                            onSelectJob = { job ->
                                scope.launch {
                                    delay(150)
                                    selectedJob = job
                                    currentScreen = "AddressDetail"
                                    loadZoneParts(job)
                                }
                            },
                            onBack = { scope.launch { delay(120); currentScreen = "Home" } }
                        )
                    }

                    "AddressDetail" -> when (zonePartsStatus) {
                        "loading", "idle" -> Box(
                            modifier = Modifier.fillMaxSize().background(Canvas),
                            contentAlignment = Alignment.Center
                        ) { Text(text = "กำลังโหลดรายการ…", color = Ink) }

                        "error" -> Box(
                            modifier = Modifier.fillMaxSize().background(Canvas).clickable {
                                selectedJob?.let { loadZoneParts(it) }
                            },
                            contentAlignment = Alignment.Center
                        ) { Text(text = "โหลดรายการไม่สำเร็จ (แตะเพื่อลองใหม่)", color = Ink) }

                        else -> AddressDetailScreen(
                            zoneCode = selectedJob?.code ?: "",
                            parts = zoneParts,
                            onMatch = { row ->
                                scope.launch { delay(150); selectedPart = row; currentScreen = "InputStock" }
                            },
                            onBack = { scope.launch { delay(120); currentScreen = "PartList" } }
                        )
                    }

                    "InputStock" -> InputStockScreen(
                        zoneCode = selectedJob?.code ?: "",
                        addressCode = selectedPart?.address ?: "",
                        scan = scanResult,
                        isEdit = selectedPart?.counted == true,
                        initialBox = selectedPart?.countedBox ?: "",
                        initialPcs = selectedPart?.countedPcs ?: "",
                        initialSeq = selectedPart?.countedSeq ?: "1",
                        onNotFound = {
                            val job = selectedJob; val row = selectedPart; val batchId = currentBatchId
                            scope.launch {
                                if (job != null && row != null && batchId != null) {
                                    Api.submitCount(
                                        batchId = batchId, deviceId = deviceCode, pic = job.pic, shortAddr = job.code, addr = row.address,
                                        kbn = row.kbn, partNo = row.partNo, partName = row.partName, supplier = row.supplier,
                                        shop = row.shop, dock = row.dock, sPlant = row.sPlant, sDock = row.sDock,
                                        // Not Found → qty forced to 0, but flagged separately from a
                                        // genuine confirmed-zero count via notFound = true.
                                        qty = 0, box = "", pcs = "", seq = "", notFound = true,
                                        employeeName = employeeName, employeePhone = employeePhone
                                    )
                                }
                                delay(150)
                                currentScreen = "AddressDetail"
                                job?.let { loadZoneParts(it) }
                            }
                        },
                        onBack = { scope.launch { delay(120); currentScreen = "AddressDetail" } },
                        onSend = { box, pcs, seq ->
                            val job = selectedJob; val row = selectedPart; val batchId = currentBatchId
                            scope.launch {
                                if (job != null && row != null && batchId != null) {
                                    Api.submitCount(
                                        batchId = batchId, deviceId = deviceCode, pic = job.pic, shortAddr = job.code, addr = row.address,
                                        kbn = row.kbn, partNo = row.partNo, partName = row.partName, supplier = row.supplier,
                                        shop = row.shop, dock = row.dock, sPlant = row.sPlant, sDock = row.sDock,
                                        qty = scanResult.qty, box = box, pcs = pcs, seq = seq, notFound = false,
                                        employeeName = employeeName, employeePhone = employeePhone
                                    )
                                }
                                delay(150)
                                currentScreen = "AddressDetail"
                                job?.let { loadZoneParts(it) }
                            }
                        }
                    )

                    "FreeZone" -> FreeZoneScreen(
                        onSend = { items ->
                            scope.launch {
                                val batchId = currentBatchId ?: Api.fetchCurrentBatchId()
                                if (batchId != null) {
                                    Api.submitFreeZone(
                                        batchId = batchId, deviceId = deviceCode, employeeName = employeeName,
                                        items = items.map { it.barcode to it.boxCount }
                                    )
                                }
                                delay(150)
                                currentScreen = "Home"
                            }
                        },
                        onBack = { scope.launch { delay(120); currentScreen = "Home" } }
                    )
                }
            }
        }
    }
}
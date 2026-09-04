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
                // Captured when the part is matched (scanned/tapped) — see
                // AddressDetailScreen's onMatch. qtyPerBox comes from the
                // Kanban QR itself; initialBox is 1 if that match WAS a real
                // scan (this scan already counts as box #1), or 0 if it was
                // just a list tap (no box confirmed yet, waiting on a real
                // scan on Input Stock).
                var selectedQtyPerBox by remember { mutableStateOf(0) }
                var selectedInitialBox by remember { mutableStateOf(0) }
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
                // on Input Stock.
                val scanResult = remember(selectedPart, selectedQtyPerBox) {
                    val row = selectedPart
                    ScanResult(
                        kbn = row?.kbn ?: "",
                        address = row?.address ?: "",
                        partName = row?.partName ?: "",
                        qtyPerBox = selectedQtyPerBox
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
                            onMatch = { row, parsed ->
                                scope.launch {
                                    delay(150)
                                    selectedPart = row
                                    // qtyPerBox comes from this scan if we have one, otherwise
                                    // falls back to the batch's own expected qty for this part
                                    // (there's no reliable way to recover it from a past
                                    // submission's total alone, since that total already mixes
                                    // in Pcs too).
                                    selectedQtyPerBox = parsed?.qtyPerBox ?: row.qty.toIntOrNull() ?: 0
                                    selectedInitialBox = when {
                                        row.counted -> row.countedBox?.toIntOrNull() ?: 1
                                        parsed != null -> 1 // this scan already counted as box #1
                                        else -> 0 // opened via list tap — no box confirmed yet
                                    }
                                    currentScreen = "InputStock"
                                }
                            },
                            onBack = { scope.launch { delay(120); currentScreen = "PartList" } }
                        )
                    }

                    "InputStock" -> InputStockScreen(
                        zoneCode = selectedJob?.code ?: "",
                        addressCode = selectedPart?.address ?: "",
                        scan = scanResult,
                        isEdit = selectedPart?.counted == true,
                        initialBox = selectedInitialBox,
                        initialPcs = selectedPart?.countedPcs ?: "",
                        initialSeq = selectedPart?.countedSeq ?: "",
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
                        onSend = { qty, box, pcs, seq ->
                            val job = selectedJob; val row = selectedPart; val batchId = currentBatchId
                            scope.launch {
                                if (job != null && row != null && batchId != null) {
                                    Api.submitCount(
                                        batchId = batchId, deviceId = deviceCode, pic = job.pic, shortAddr = job.code, addr = row.address,
                                        kbn = row.kbn, partNo = row.partNo, partName = row.partName, supplier = row.supplier,
                                        shop = row.shop, dock = row.dock, sPlant = row.sPlant, sDock = row.sDock,
                                        // qty = Box × Qty-per-Box (from the QR) + Pcs — computed
                                        // on the Input Stock screen itself, see InputStockScreen.kt.
                                        qty = qty, box = box.toString(), pcs = pcs, seq = seq, notFound = false,
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
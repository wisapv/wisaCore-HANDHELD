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
                var selectedAddress by remember { mutableStateOf<AddressRow?>(null) }
                var selectedKbnRow by remember { mutableStateOf<KbnRow?>(null) }
                var currentBatchId by remember { mutableStateOf<String?>(null) }

                val scope = rememberCoroutineScope()

                // "idle" / "loading" / "ready" / "error" for each fetch below.
                var jobsStatus by remember { mutableStateOf("idle") }
                var jobs by remember { mutableStateOf<List<ZoneJob>>(emptyList()) }

                var addressesStatus by remember { mutableStateOf("idle") }
                var addresses by remember { mutableStateOf<List<AddressRow>>(emptyList()) }

                var addressDetailStatus by remember { mutableStateOf("idle") }
                var kbnRows by remember { mutableStateOf<List<KbnRow>>(emptyList()) }

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

                fun loadAddresses(job: ZoneJob) {
                    scope.launch {
                        addressesStatus = "loading"
                        val batchId = currentBatchId ?: Api.fetchCurrentBatchId()
                        if (batchId == null) {
                            addressesStatus = "error"
                            return@launch
                        }
                        currentBatchId = batchId
                        val result = Api.fetchJobAddresses(batchId, deviceCode, job.pic, job.code)
                        if (result == null) {
                            // A real request failure (timeout/connection reset/etc.) —
                            // NOT the same as "this zone genuinely has 0 addresses".
                            // Must not silently show as empty, or a transient network
                            // hiccup looks identical to real data being gone.
                            addressesStatus = "error"
                            return@launch
                        }
                        // remain/done now come from the backend's real
                        // handheld_stock_counts (submit-count), not a guess.
                        addresses = result.map {
                            AddressRow(code = it.addr, status = if (it.done) AddressStatus.DONE else AddressStatus.PENDING, remain = it.remain)
                        }
                        addressesStatus = "ready"
                    }
                }

                fun loadAddressDetail(job: ZoneJob, address: AddressRow) {
                    scope.launch {
                        addressDetailStatus = "loading"
                        val batchId = currentBatchId ?: Api.fetchCurrentBatchId()
                        if (batchId == null) {
                            addressDetailStatus = "error"
                            return@launch
                        }
                        currentBatchId = batchId
                        val result = Api.fetchJobAddressDetail(batchId, deviceCode, job.pic, job.code, address.code)
                        if (result == null) {
                            addressDetailStatus = "error"
                            return@launch
                        }
                        kbnRows = result.map {
                            KbnRow(
                                supplier = it.supplier, kbn = it.kbn, address = it.address, partName = it.partName,
                                partNo = it.partNo, shop = it.shop, dock = it.dock, sPlant = it.sPlant, sDock = it.sDock, qty = it.qty
                            )
                        }
                        addressDetailStatus = "ready"
                    }
                }

                val remainingCount = jobs.sumOf { it.itemCount } // itemCount is already "remaining" — see my-jobs backend

                // The row tapped in Address Detail becomes the "scanned"
                // result on Input Stock — no real barcode hardware wired in
                // yet (see DataWedge TODO), so this stands in for a scan.
                // Q'ty defaults to 1 (not tracked by the backend yet either).
                val scanResult = remember(selectedKbnRow) {
                    val row = selectedKbnRow
                    ScanResult(
                        kbn = row?.kbn ?: "",
                        address = row?.address ?: (selectedAddress?.code ?: ""),
                        partName = row?.partName ?: "",
                        qty = 1
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
                                    currentScreen = "SelectAddress"
                                    loadAddresses(job)
                                }
                            },
                            onBack = { scope.launch { delay(120); currentScreen = "Home" } }
                        )
                    }

                    "SelectAddress" -> when (addressesStatus) {
                        "loading", "idle" -> Box(
                            modifier = Modifier.fillMaxSize().background(Canvas),
                            contentAlignment = Alignment.Center
                        ) { Text(text = "กำลังโหลด address…", color = Ink) }

                        "error" -> Box(
                            modifier = Modifier.fillMaxSize().background(Canvas).clickable { selectedJob?.let { loadAddresses(it) } },
                            contentAlignment = Alignment.Center
                        ) { Text(text = "โหลด address ไม่สำเร็จ (แตะเพื่อลองใหม่)", color = Ink) }

                        else -> SelectAddressScreen(
                            zoneCode = selectedJob?.code ?: "",
                            employeeName = employeeName,
                            addresses = addresses,
                            onSelectAddress = { addr ->
                                scope.launch {
                                    delay(150)
                                    selectedAddress = addr
                                    currentScreen = "AddressDetail"
                                    selectedJob?.let { loadAddressDetail(it, addr) }
                                }
                            },
                            onBack = { scope.launch { delay(120); currentScreen = "PartList" } }
                        )
                    }

                    "AddressDetail" -> when (addressDetailStatus) {
                        "loading", "idle" -> Box(
                            modifier = Modifier.fillMaxSize().background(Canvas),
                            contentAlignment = Alignment.Center
                        ) { Text(text = "กำลังโหลดรายการ…", color = Ink) }

                        "error" -> Box(
                            modifier = Modifier.fillMaxSize().background(Canvas).clickable {
                                val job = selectedJob; val address = selectedAddress
                                if (job != null && address != null) loadAddressDetail(job, address)
                            },
                            contentAlignment = Alignment.Center
                        ) { Text(text = "โหลดรายการไม่สำเร็จ (แตะเพื่อลองใหม่)", color = Ink) }

                        else -> AddressDetailScreen(
                            zoneCode = selectedJob?.code ?: "",
                            addressCode = selectedAddress?.code ?: "",
                            remain = kbnRows.size,
                            rows = kbnRows,
                            onSelectRow = { row ->
                                scope.launch { delay(150); selectedKbnRow = row; currentScreen = "InputStock" }
                            },
                            onBack = { scope.launch { delay(120); currentScreen = "SelectAddress" } },
                            onEdit = { /* TODO: manual edit mode for this address's counts */ }
                        )
                    }

                    "InputStock" -> InputStockScreen(
                        zoneCode = selectedJob?.code ?: "",
                        addressCode = selectedAddress?.code ?: "",
                        scan = scanResult,
                        onNotFound = {
                            val job = selectedJob; val address = selectedAddress; val row = selectedKbnRow; val batchId = currentBatchId
                            scope.launch {
                                if (job != null && address != null && row != null && batchId != null) {
                                    Api.submitCount(
                                        batchId = batchId, deviceId = deviceCode, pic = job.pic, shortAddr = job.code, addr = address.code,
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
                                if (job != null && address != null) loadAddressDetail(job, address)
                            }
                        },
                        onBack = { scope.launch { delay(120); currentScreen = "AddressDetail" } },
                        onSend = { box, pcs, seq ->
                            val job = selectedJob; val address = selectedAddress; val row = selectedKbnRow; val batchId = currentBatchId
                            scope.launch {
                                if (job != null && address != null && row != null && batchId != null) {
                                    Api.submitCount(
                                        batchId = batchId, deviceId = deviceCode, pic = job.pic, shortAddr = job.code, addr = address.code,
                                        kbn = row.kbn, partNo = row.partNo, partName = row.partName, supplier = row.supplier,
                                        shop = row.shop, dock = row.dock, sPlant = row.sPlant, sDock = row.sDock,
                                        qty = scanResult.qty, box = box, pcs = pcs, seq = seq, notFound = false,
                                        employeeName = employeeName, employeePhone = employeePhone
                                    )
                                }
                                delay(150)
                                currentScreen = "AddressDetail"
                                if (job != null && address != null) loadAddressDetail(job, address)
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
package com.example.wisahandheld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                var currentScreen by remember { mutableStateOf("Overview") }
                var deviceCode by remember { mutableStateOf("") }
                var employeeName by remember { mutableStateOf("") }
                var employeePhone by remember { mutableStateOf("") }
                var selectedJob by remember { mutableStateOf<ZoneJob?>(null) }
                var selectedAddress by remember { mutableStateOf<AddressRow?>(null) }

                val scope = rememberCoroutineScope()

                // 🌟 Dummy data — replace with a real fetch once the backend
                // has an endpoint for "what is device X assigned to for
                // batch Y" (see wisaCore-DASHINV: AssignHandheld.jsx +
                // the not-yet-built Send to Handheld persistence).
                val jobs = remember {
                    listOf(
                        ZoneJob("FN4-01", "สมชาย", 46),
                        ZoneJob("SQ-03", "สมชาย", 40),
                        ZoneJob("TR2-02", "มานะ", 53)
                    )
                }
                val totalCount = jobs.sumOf { it.itemCount }
                val scannedCount = 12

                val addressesForJob = remember(selectedJob) {
                    listOf(
                        AddressRow("A-11", AddressStatus.DONE, 0),
                        AddressRow("A-12", AddressStatus.CURRENT, 2),
                        AddressRow("A-14", AddressStatus.PENDING, 5)
                    )
                }
                val kbnRowsForAddress = remember(selectedAddress) {
                    listOf(
                        KbnRow("Denso Co.", "A001", "A-12"),
                        KbnRow("Denso Co.", "A001", "A-12")
                    )
                }
                val dummyScan = remember(selectedAddress) {
                    ScanResult(
                        kbn = "A001",
                        address = selectedAddress?.code ?: "A-12",
                        partName = "Radiator Hose Upper",
                        qty = 4
                    )
                }

                when (currentScreen) {
                    "Overview" -> OverviewScreen(onGetStarted = {
                        scope.launch { delay(150); currentScreen = "Login" }
                    })

                    "Login" -> LoginScreen(onLoggedIn = { code ->
                        scope.launch { delay(150); deviceCode = code; currentScreen = "CheckIn" }
                    })

                    "CheckIn" -> EmployeeCheckInScreen(
                        deviceCode = deviceCode,
                        onCheckedIn = { id, phone ->
                            scope.launch {
                                delay(150)
                                employeeName = id
                                employeePhone = phone
                                currentScreen = "Home"
                            }
                        }
                    )

                    "Home" -> HomeScreen(
                        deviceCode = deviceCode,
                        employeeName = employeeName,
                        phone = employeePhone,
                        zonesToday = jobs.size,
                        scannedCount = scannedCount,
                        totalCount = totalCount,
                        onOpenPartList = { scope.launch { delay(150); currentScreen = "PartList" } },
                        onOpenFreeZone = { scope.launch { delay(150); currentScreen = "FreeZone" } },
                        onChangePerson = { scope.launch { delay(150); currentScreen = "CheckIn" } }
                    )

                    "PartList" -> PartListScreen(
                        deviceCode = deviceCode,
                        jobs = jobs,
                        onSelectJob = { job ->
                            scope.launch { delay(150); selectedJob = job; currentScreen = "SelectAddress" }
                        }
                    )

                    "SelectAddress" -> SelectAddressScreen(
                        zoneCode = selectedJob?.code ?: "",
                        employeeName = employeeName,
                        addresses = addressesForJob,
                        onSelectAddress = { addr ->
                            scope.launch { delay(150); selectedAddress = addr; currentScreen = "AddressDetail" }
                        },
                        onBack = { scope.launch { delay(120); currentScreen = "PartList" } }
                    )

                    "AddressDetail" -> AddressDetailScreen(
                        zoneCode = selectedJob?.code ?: "",
                        addressCode = selectedAddress?.code ?: "",
                        remain = selectedAddress?.remain ?: 0,
                        rows = kbnRowsForAddress,
                        onSelectRow = { scope.launch { delay(150); currentScreen = "InputStock" } },
                        onBack = { scope.launch { delay(120); currentScreen = "SelectAddress" } },
                        onEdit = { /* TODO: manual edit mode for this address's counts */ }
                    )

                    "InputStock" -> InputStockScreen(
                        zoneCode = selectedJob?.code ?: "",
                        addressCode = selectedAddress?.code ?: "",
                        scan = dummyScan,
                        onNotFound = {
                            // TODO: POST qty=0 tagged notFound=true — kept
                            // separate from a genuine confirmed-zero count.
                            scope.launch { delay(150); currentScreen = "AddressDetail" }
                        },
                        onBack = { scope.launch { delay(120); currentScreen = "AddressDetail" } },
                        onSend = { _, _, _ ->
                            // TODO: POST the confirmed count to the backend,
                            // then return to the address's remaining list.
                            scope.launch { delay(150); currentScreen = "AddressDetail" }
                        }
                    )

                    "FreeZone" -> Box(
                        modifier = Modifier.fillMaxSize().background(Canvas),
                        contentAlignment = Alignment.Center
                    ) {
                        // TODO: Free Zone screen — open scan, no list, no assignment.
                        Text(text = "Free Zone — ยังไม่ได้ออกแบบ", color = Ink)
                    }
                }
            }
        }
    }
}

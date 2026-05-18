package com.example.wisahandheld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.wisahandheld.ui.screens.*
import com.example.wisahandheld.ui.theme.WISAHANDHELDTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.wisahandheld.ui.screens.SelectPicScreen
import com.example.wisahandheld.ui.screens.SelectAreaScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WISAHANDHELDTheme {
                var currentScreen by remember { mutableStateOf("Welcome") }
                var selectedShop by remember { mutableStateOf("") }
                var selectedPic by remember { mutableStateOf("") }

                val scope = rememberCoroutineScope()

                when (currentScreen) {
                    "Welcome" -> WelcomeScreen(onStartClick = {
                        scope.launch { delay(250); currentScreen = "Shop" }
                    })

                    "Shop" -> ShopSelectionScreen(onShopSelected = { shop ->
                        scope.launch { delay(250); selectedShop = shop; currentScreen = "Pic" }
                    })

                    "Pic" -> {
                        // 🌟 จำลองข้อมูล PIC ที่ได้จากระบบ (ในอนาคตดึงจาก Web)
                        val dummyPics = listOf("A", "B", "C", "D", "E", "QC")
                        SelectPicScreen(
                            shopName = selectedShop,
                            picList = dummyPics,
                            onPicSelected = { pic ->
                                scope.launch { delay(250); selectedPic = pic; currentScreen = "Area" }
                            },
                            onBackClick = { scope.launch { delay(150); currentScreen = "Shop" } }
                        )
                    }

                    // ใน MainActivity.kt เพิ่มเคส "Area"
                    "Area" -> {
                        val dummyAreas = listOf("SQR", "FN3", "FN2", "FA1", "IP1", "ALS", "PLT", "WHS")
                        SelectAreaScreen(
                            picName = selectedPic,
                            areaList = dummyAreas,
                            onNextClick = { selectedList ->
                                // เก็บข้อมูลรายการที่เลือก (selectedList) แล้วไปหน้าต่อไป
                                scope.launch { delay(250); currentScreen = "NextProcess" }
                            },
                            onBackClick = { scope.launch { delay(150); currentScreen = "Pic" } }
                        )
                    }
                }
            }
        }
    }
}
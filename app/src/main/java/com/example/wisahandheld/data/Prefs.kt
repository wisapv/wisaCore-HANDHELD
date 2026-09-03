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

    fun saveDeviceCode(context: Context, code: String) {
        prefs(context).edit().putString(KEY_DEVICE_CODE, code).apply()
    }

    fun saveEmployee(context: Context, name: String, phone: String) {
        prefs(context).edit()
            .putString(KEY_EMPLOYEE_NAME, name)
            .putString(KEY_EMPLOYEE_PHONE, phone)
            .apply()
    }

    fun loadDeviceCode(context: Context): String? = prefs(context).getString(KEY_DEVICE_CODE, null)
    fun loadEmployeeName(context: Context): String? = prefs(context).getString(KEY_EMPLOYEE_NAME, null)
    fun loadEmployeePhone(context: Context): String? = prefs(context).getString(KEY_EMPLOYEE_PHONE, null)

    private fun prefs(context: Context) = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}

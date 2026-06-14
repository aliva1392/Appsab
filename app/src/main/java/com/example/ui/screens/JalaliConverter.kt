package com.example.ui.screens

import java.util.Calendar

class ShamsiDate(var year: Int, var month: Int, var date: Int)

fun getShamsiDate(timeMillis: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timeMillis
    
    val gy = cal.get(Calendar.YEAR)
    val gm = cal.get(Calendar.MONTH) + 1
    val gd = cal.get(Calendar.DAY_OF_MONTH)
    
    val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    var jy: Int = if (gy <= 1600) 0 else 979
    var gy2 = gy - if (gy <= 1600) 621 else 1600
    
    var gy3 = if (gm > 2) gy2 + 1 else gy2
    var days = (365 * gy2) + ((gy3 + 3) / 4) - ((gy3 + 99) / 100) + ((gy3 + 399) / 400) - 80 + gd + g_d_m[gm - 1]
    
    jy += 33 * (days / 12053)
    days %= 12053
    jy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        jy += ((days - 1) / 365)
        days = (days - 1) % 365
    }
    
    val jm = if (days < 186) 1 + (days / 31) else 7 + ((days - 186) / 30)
    val jd = 1 + if (days < 186) (days % 31) else ((days - 186) % 30)
    
    return "${jy}/${jm}/${jd}"
}

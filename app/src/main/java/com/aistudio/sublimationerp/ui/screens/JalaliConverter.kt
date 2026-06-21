package com.aistudio.sublimationerp.ui.screens

import java.util.Calendar

fun getShamsiDate(timestamp: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    
    return gregorianToJalali(year, month, day)
}

fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): String {
    val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

    var gy = gYear - 1600
    var gm = gMonth - 1
    var gd = gDay - 1

    var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400

    for (i in 0 until gm) {
        gDayNo += gDaysInMonth[i]
    }
    if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
        gDayNo++
    }
    gDayNo += gd

    var jDayNo = gDayNo - 79

    val jNp = jDayNo / 12053
    jDayNo %= 12053

    var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
    jDayNo %= 1461

    if (jDayNo >= 366) {
        jy += (jDayNo - 1) / 365
        jDayNo = (jDayNo - 1) % 365
    }

    var jm = 0
    for (i in 0..11) {
        if (jDayNo < jDaysInMonth[i]) {
            jm = i
            break
        }
        jDayNo -= jDaysInMonth[i]
    }

    val jd = jDayNo + 1
    val finalMonth = jm + 1

    return "$jy/${finalMonth.toString().padStart(2, '0')}/${jd.toString().padStart(2, '0')}"
}

fun shamsiToTimestamp(jalali: String): Long? {
    val parts = jalali.split("/")
    if (parts.size != 3) return null
    val jy = parts[0].toIntOrNull() ?: return null
    val jm = parts[1].toIntOrNull() ?: return null
    val jd = parts[2].toIntOrNull() ?: return null
    
    val (gy, gm, gd) = jalaliToGregorian(jy, jm, jd)
    val cal = java.util.Calendar.getInstance()
    cal.set(gy, gm - 1, gd, 0, 0, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun jalaliToGregorian(jYear: Int, jMonth: Int, jDay: Int): Triple<Int, Int, Int> {
    var jy = jYear - 979
    var jm = jMonth - 1
    var jd = jDay - 1

    var jDayNo = 365 * jy + (jy / 33) * 8 + ((jy % 33) + 3) / 4
    for (i in 0 until jm) {
        jDayNo += if (i < 6) 31 else 30
    }
    jDayNo += jd

    var gDayNo = jDayNo + 79

    var gy = 1600 + 400 * (gDayNo / 146097)
    gDayNo %= 146097

    var leap = true
    if (gDayNo >= 36525) {
        gDayNo--
        gy += 100 * (gDayNo / 36524)
        gDayNo %= 36524

        if (gDayNo >= 365) {
            gDayNo++
        } else {
            leap = false
        }
    }

    gy += 4 * (gDayNo / 1461)
    gDayNo %= 1461

    if (gDayNo >= 366) {
        leap = false
        gDayNo--
        gy += gDayNo / 365
        gDayNo %= 365
    }

    var gm = 0
    val gDaysInMonth = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    for (i in 0..11) {
        if (gDayNo < gDaysInMonth[i]) {
            gm = i
            break
        }
        gDayNo -= gDaysInMonth[i]
    }

    val gd = gDayNo + 1
    return Triple(gy, gm + 1, gd)
}

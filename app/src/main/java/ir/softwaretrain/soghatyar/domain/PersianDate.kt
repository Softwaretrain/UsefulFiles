package ir.softwaretrain.soghatyar.domain

import java.util.Calendar
import java.util.GregorianCalendar

/**
 * تبدیل و قالب‌بندی تاریخ جلالی (شمسی).
 * بر پایه الگوریتم jalaali (با تقسیم صحیحِ برش به‌سمت صفر که در Kotlin پیش‌فرض است).
 */
object PersianDate {

    private val monthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    // Calendar.DAY_OF_WEEK: SUNDAY=1 ... SATURDAY=7
    private val weekDays = arrayOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
    )

    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
        1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    data class Jalali(val year: Int, val month: Int, val day: Int)

    private data class Cal(val leap: Int, val gy: Int, val march: Int)

    private fun jalCal(jy: Int): Cal {
        val gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jump = 0
        for (j in 1 until breaks.size) {
            val jm = breaks[j]
            jump = jm - jp
            if (jy < jm) break
            leapJ += (jump / 33) * 8 + (jump % 33) / 4
            jp = jm
        }
        var n = jy - jp
        leapJ += (n / 33) * 8 + ((n % 33) + 3) / 4
        if (jump % 33 == 4 && jump - n == 4) leapJ += 1
        val leapG = gy / 4 - ((gy / 100 + 1) * 3) / 4 - 150
        val march = 20 + leapJ - leapG
        if (jump - n < 6) n = n - jump + (jump + 4) / 33 * 33
        var leap = ((((n + 1) % 33) - 1) % 4)
        if (leap == -1) leap = 4
        return Cal(leap, gy, march)
    }

    /** میلادی به شماره روز (JDN مطابق jalaali) */
    private fun g2d(gy: Int, gm: Int, gd: Int): Int {
        var d = ((gy + (gm - 8) / 6 + 100100) * 1461) / 4 +
                (153 * ((gm + 9) % 12) + 2) / 5 + gd - 34840408
        d = d - ((gy + 100100 + (gm - 8) / 6) / 100 * 3) / 4 + 752
        return d
    }

    private fun d2g(jdn: Int): IntArray {
        var j = 4 * jdn + 139361631
        j += (((4 * jdn + 183187720) / 146097) * 3) / 4 * 4 - 3908
        val i = (j % 1461) / 4 * 5 + 308
        val gd = (i % 153) / 5 + 1
        val gm = (i / 153) % 12 + 1
        val gy = j / 1461 - 100100 + (8 - gm) / 6
        return intArrayOf(gy, gm, gd)
    }

    private fun d2j(jdn: Int): Jalali {
        val gy = d2g(jdn)[0]
        var jy = gy - 621
        val r = jalCal(jy)
        val jdn1f = g2d(gy, 3, r.march)
        var k = jdn - jdn1f
        if (k >= 0) {
            if (k <= 185) return Jalali(jy, 1 + k / 31, k % 31 + 1)
            k -= 186
        } else {
            jy -= 1
            k += 179
            if (r.leap == 1) k += 1
        }
        return Jalali(jy, 7 + k / 30, k % 30 + 1)
    }

    fun toJalali(millis: Long): Jalali {
        val c = GregorianCalendar().apply { timeInMillis = millis }
        val jdn = g2d(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        return d2j(jdn)
    }

    /** مثل «دوشنبه ۲۰ مهر» */
    fun formatShort(millis: Long): String {
        val j = toJalali(millis)
        val c = GregorianCalendar().apply { timeInMillis = millis }
        val wd = weekDays[c.get(Calendar.DAY_OF_WEEK) - 1]
        return "$wd ${toFa(j.day)} ${monthNames[j.month - 1]}"
    }

    /** مثل «۲۰ مهر ۱۴۰۵» */
    fun formatFull(millis: Long): String {
        val j = toJalali(millis)
        return "${toFa(j.day)} ${monthNames[j.month - 1]} ${toFa(j.year)}"
    }

    /** مثل «۱۴۰۵/۰۷/۲۰» */
    fun formatNumeric(millis: Long): String {
        val j = toJalali(millis)
        val mm = j.month.toString().padStart(2, '0')
        val dd = j.day.toString().padStart(2, '0')
        return toFa("${j.year}/$mm/$dd")
    }

    private fun toFa(n: Int): String = toFa(n.toString())
    private fun toFa(s: String): String {
        val fa = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder()
        for (ch in s) sb.append(if (ch in '0'..'9') fa[ch - '0'] else ch)
        return sb.toString()
    }
}

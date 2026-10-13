package ir.softwaretrain.soghatyar.domain

/** قالب‌بندی مبلغ (تومان) با جداکننده هزارگان و ارقام فارسی */
object Money {
    private val fa = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun faDigits(s: String): String {
        val sb = StringBuilder()
        for (ch in s) sb.append(if (ch in '0'..'9') fa[ch - '0'] else ch)
        return sb.toString()
    }

    fun grouped(value: Long): String {
        val neg = value < 0
        val digits = kotlin.math.abs(value).toString()
        val sb = StringBuilder()
        var count = 0
        for (i in digits.length - 1 downTo 0) {
            sb.append(digits[i])
            count++
            if (count % 3 == 0 && i != 0) sb.append('٬')
        }
        val grouped = sb.reverse().toString()
        return (if (neg) "-" else "") + faDigits(grouped)
    }

    /** مثل «۲٬۹۵۰٬۰۰۰ تومان» */
    fun toman(value: Long): String = grouped(value) + " تومان"

    /** مبلغ کوتاه برای کارت‌ها، مثل «۳۲ م.ت» (میلیون تومان) */
    fun short(value: Long): String {
        return when {
            value >= 1_000_000_000 -> faDigits("%.1f".format(value / 1_000_000_000.0)) + " م‌د.ت"
            value >= 1_000_000 -> faDigits((value / 1_000_000).toString()) + " م.ت"
            value >= 1_000 -> faDigits((value / 1_000).toString()) + " ه.ت"
            else -> grouped(value)
        }
    }
}

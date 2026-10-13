package ir.softwaretrain.soghatyar.domain

import java.util.Calendar
import java.util.GregorianCalendar

/** ساخت جدول اقساط ماهانه از مبلغ تأمین‌شده */
object InstallmentPlanner {

    data class Row(val index: Int, val dueMillis: Long, val amount: Long)

    /**
     * مبلغ را بین [count] قسط تقسیم می‌کند؛ هر قسط به نزدیک‌ترین ۱۰۰۰ تومان گرد می‌شود
     * و باقی‌مانده به قسط آخر اضافه می‌شود. سررسید هر قسط یک ماه بعد از قبلی است.
     */
    fun build(financed: Long, count: Int, firstDueMillis: Long): List<Row> {
        if (count <= 0 || financed <= 0) return emptyList()
        val base = ((financed / count) / 1000L) * 1000L
        val rows = ArrayList<Row>(count)
        var allocated = 0L
        for (i in 0 until count) {
            val amount = if (i == count - 1) financed - allocated else base
            allocated += amount
            rows.add(Row(i + 1, addMonths(firstDueMillis, i), amount))
        }
        return rows
    }

    fun addMonths(millis: Long, months: Int): Long {
        val c = GregorianCalendar().apply { timeInMillis = millis }
        c.add(Calendar.MONTH, months)
        return c.timeInMillis
    }
}

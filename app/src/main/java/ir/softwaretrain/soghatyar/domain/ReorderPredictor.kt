package ir.softwaretrain.soghatyar.domain

/** پیش‌بینی زمان سفارش بعدی مشتری بر اساس فاصله خریدها */
object ReorderPredictor {

    private const val DAY = 24L * 60 * 60 * 1000

    /**
     * اگر [reorderDays] مشخص باشد از آن استفاده می‌شود؛ وگرنه فاصله پیش‌فرض ۴۲ روز (۶ هفته).
     * خروجی: میلی‌ثانیهٔ سررسید سفارش بعدی، یا null اگر خریدی ثبت نشده.
     */
    fun nextOrderMillis(lastSaleMillis: Long?, reorderDays: Int): Long? {
        if (lastSaleMillis == null) return null
        val days = if (reorderDays > 0) reorderDays else 42
        return lastSaleMillis + days * DAY
    }

    /** آیا الان زمان یادآوری سفارش است؟ (از [leadDays] روز قبل سررسید) */
    fun isDueSoon(lastSaleMillis: Long?, reorderDays: Int, now: Long, leadDays: Int = 3): Boolean {
        val next = nextOrderMillis(lastSaleMillis, reorderDays) ?: return false
        return now >= next - leadDays * DAY
    }

    fun daysSince(millis: Long?, now: Long): Int? {
        if (millis == null) return null
        return ((now - millis) / DAY).toInt()
    }
}

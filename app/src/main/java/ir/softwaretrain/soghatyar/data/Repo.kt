package ir.softwaretrain.soghatyar.data

import ir.softwaretrain.soghatyar.domain.InstallmentPlanner
import kotlinx.coroutines.flow.Flow

/** مخزن داده: تنها نقطه دسترسی لایه UI به دیتابیس */
class Repo(private val db: AppDatabase) {

    val customerDao = db.customerDao()
    val productDao = db.productDao()
    val saleDao = db.saleDao()
    val installmentDao = db.installmentDao()
    val paymentDao = db.paymentDao()
    val messageDao = db.messageDao()

    // مشتری‌ها
    fun customersWithBalance(): Flow<List<CustomerWithBalance>> = customerDao.withBalance()
    fun customers(): Flow<List<Customer>> = customerDao.all()
    fun customer(id: Long): Flow<Customer?> = customerDao.byIdFlow(id)
    fun balance(id: Long): Flow<Long> = customerDao.balanceFlow(id)
    suspend fun saveCustomer(c: Customer): Long =
        if (c.id == 0L) customerDao.insert(c) else { customerDao.update(c); c.id }

    // کالاها
    fun activeProducts(): Flow<List<Product>> = productDao.active()
    fun allProducts(): Flow<List<Product>> = productDao.all()
    fun lowStockProducts(): Flow<List<Product>> = productDao.lowStock()
    suspend fun saveProduct(p: Product): Long =
        if (p.id == 0L) productDao.insert(p) else { productDao.update(p); p.id }

    // فروش و اقساط
    fun salesOf(cid: Long): Flow<List<Sale>> = saleDao.salesOf(cid)
    fun installmentsOf(cid: Long): Flow<List<Installment>> = installmentDao.ofCustomer(cid)
    fun paymentsOf(cid: Long): Flow<List<Payment>> = paymentDao.ofCustomer(cid)

    /**
     * ثبت یک فروش کامل: اقلام را ذخیره می‌کند، موجودی را کم می‌کند و در صورت
     * اقساطی‌بودن، جدول اقساط را از مبلغ باقی‌مانده (total - downPayment) می‌سازد.
     */
    suspend fun createSale(
        sale: Sale,
        items: List<SaleItem>,
        installmentCount: Int,
        firstDueMillis: Long
    ): Long {
        val saleId = saleDao.insertSale(sale)
        saleDao.insertItems(items.map { it.copy(saleId = saleId) })
        for (it in items) {
            productDao.decreaseStock(it.productId, it.qty)
        }
        if (sale.paymentType == PaymentType.INSTALLMENT && installmentCount > 0) {
            val financed = (sale.total - sale.downPayment).coerceAtLeast(0)
            val plan = InstallmentPlanner.build(financed, installmentCount, firstDueMillis)
            saleDao.insertInstallments(plan.map {
                Installment(
                    saleId = saleId,
                    customerId = sale.customerId,
                    dueMillis = it.dueMillis,
                    amount = it.amount
                )
            })
        }
        return saleId
    }

    /** ثبت دریافت وجه از مشتری (کاهش مانده) */
    suspend fun recordPayment(payment: Payment): Long = paymentDao.insert(payment)

    /** علامت‌زدن یک قسط به‌عنوان پرداخت‌شده + ثبت دریافت متناظر */
    suspend fun payInstallment(installmentId: Long, method: PayMethod, whenMillis: Long) {
        val inst = installmentDao.byId(installmentId) ?: return
        if (inst.paidMillis != null) return
        installmentDao.update(inst.copy(paidMillis = whenMillis))
        paymentDao.insert(
            Payment(
                customerId = inst.customerId,
                saleId = inst.saleId,
                installmentId = inst.id,
                amount = inst.amount,
                dateMillis = whenMillis,
                method = method,
                note = "پرداخت قسط"
            )
        )
    }

    // داشبورد
    fun salesSince(from: Long): Flow<Long?> = saleDao.totalSince(from)
    fun paymentsSince(from: Long): Flow<Long?> = paymentDao.totalSince(from)
    fun dueInstallments(until: Long): Flow<List<Installment>> = installmentDao.dueUntil(until)

    // پیام‌ها
    fun recentMessages(): Flow<List<MessageLog>> = messageDao.recent()
    suspend fun logMessage(m: MessageLog): Long = messageDao.insert(m)

    suspend fun customerById(id: Long): Customer? = customerDao.byId(id)
}

package ir.softwaretrain.soghatyar.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/** مشتری به‌همراه مانده حساب و آخرین خرید (نتیجه کوئری) */
data class CustomerWithBalance(
    val id: Long,
    val name: String,
    val phone: String,
    val tags: String,
    val reorderDays: Int,
    val balance: Long,
    val lastSaleMillis: Long?
)

@Dao
interface CustomerDao {
    @Insert suspend fun insert(c: Customer): Long
    @Update suspend fun update(c: Customer)
    @Delete suspend fun delete(c: Customer)

    @Query("SELECT * FROM customers ORDER BY name")
    fun all(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name")
    suspend fun allOnce(): List<Customer>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun byId(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE id = :id")
    fun byIdFlow(id: Long): Flow<Customer?>

    /**
     * مانده هر مشتری = (مجموع total منهای downPayment فروش‌ها) منهای مجموع پرداخت‌ها.
     * lastSale = آخرین تاریخ فروش.
     */
    @Query(
        """
        SELECT c.id AS id, c.name AS name, c.phone AS phone, c.tags AS tags,
               c.reorderDays AS reorderDays,
               (IFNULL((SELECT SUM(s.total - s.downPayment) FROM sales s WHERE s.customerId = c.id), 0)
                - IFNULL((SELECT SUM(p.amount) FROM payments p WHERE p.customerId = c.id), 0)) AS balance,
               (SELECT MAX(s2.dateMillis) FROM sales s2 WHERE s2.customerId = c.id) AS lastSaleMillis
        FROM customers c
        ORDER BY c.name
        """
    )
    fun withBalance(): Flow<List<CustomerWithBalance>>

    @Query(
        """
        SELECT c.id AS id, c.name AS name, c.phone AS phone, c.tags AS tags,
               c.reorderDays AS reorderDays,
               (IFNULL((SELECT SUM(s.total - s.downPayment) FROM sales s WHERE s.customerId = c.id), 0)
                - IFNULL((SELECT SUM(p.amount) FROM payments p WHERE p.customerId = c.id), 0)) AS balance,
               (SELECT MAX(s2.dateMillis) FROM sales s2 WHERE s2.customerId = c.id) AS lastSaleMillis
        FROM customers c
        ORDER BY c.name
        """
    )
    suspend fun balancesOnce(): List<CustomerWithBalance>

    @Query(
        """
        SELECT (IFNULL((SELECT SUM(s.total - s.downPayment) FROM sales s WHERE s.customerId = :id), 0)
                - IFNULL((SELECT SUM(p.amount) FROM payments p WHERE p.customerId = :id), 0))
        """
    )
    fun balanceFlow(id: Long): Flow<Long>
}

@Dao
interface ProductDao {
    @Insert suspend fun insert(p: Product): Long
    @Update suspend fun update(p: Product)
    @Delete suspend fun delete(p: Product)

    @Query("SELECT * FROM products WHERE active = 1 ORDER BY name")
    fun active(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name")
    fun all(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name")
    suspend fun allOnce(): List<Product>

    @Query("SELECT * FROM products WHERE active = 1 AND stock <= lowStock AND lowStock > 0 ORDER BY name")
    fun lowStock(): Flow<List<Product>>

    @Query("UPDATE products SET stock = stock - :qty WHERE id = :id")
    suspend fun decreaseStock(id: Long, qty: Double)
}

@Dao
interface SaleDao {
    @Insert suspend fun insertSale(s: Sale): Long
    @Insert suspend fun insertItems(items: List<SaleItem>)
    @Insert suspend fun insertInstallments(items: List<Installment>)

    @Query("SELECT * FROM sales WHERE customerId = :cid ORDER BY dateMillis DESC")
    fun salesOf(cid: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sale_items WHERE saleId = :sid")
    suspend fun itemsOf(sid: Long): List<SaleItem>

    @Query("SELECT SUM(total) FROM sales WHERE dateMillis >= :from")
    fun totalSince(from: Long): Flow<Long?>
}

@Dao
interface InstallmentDao {
    @Insert suspend fun insert(i: Installment): Long
    @Update suspend fun update(i: Installment)

    @Query("SELECT * FROM installments WHERE customerId = :cid ORDER BY dueMillis")
    fun ofCustomer(cid: Long): Flow<List<Installment>>

    @Query("SELECT * FROM installments WHERE saleId = :sid ORDER BY dueMillis")
    suspend fun ofSale(sid: Long): List<Installment>

    /** اقساط سررسیده پرداخت‌نشده تا تاریخ معین */
    @Query("SELECT * FROM installments WHERE paidMillis IS NULL AND dueMillis <= :until ORDER BY dueMillis")
    fun dueUntil(until: Long): Flow<List<Installment>>

    @Query("SELECT * FROM installments WHERE paidMillis IS NULL AND dueMillis <= :until ORDER BY dueMillis")
    suspend fun dueUntilNow(until: Long): List<Installment>

    @Query("SELECT * FROM installments WHERE id = :id")
    suspend fun byId(id: Long): Installment?
}

@Dao
interface PaymentDao {
    @Insert suspend fun insert(p: Payment): Long

    @Query("SELECT * FROM payments WHERE customerId = :cid ORDER BY dateMillis DESC")
    fun ofCustomer(cid: Long): Flow<List<Payment>>

    @Query("SELECT SUM(amount) FROM payments WHERE dateMillis >= :from")
    fun totalSince(from: Long): Flow<Long?>
}

@Dao
interface MessageDao {
    @Insert suspend fun insert(m: MessageLog): Long

    @Query("SELECT * FROM message_logs ORDER BY dateMillis DESC LIMIT 100")
    fun recent(): Flow<List<MessageLog>>
}

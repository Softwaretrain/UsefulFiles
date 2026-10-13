package ir.softwaretrain.soghatyar.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** نوع پرداخت یک فروش */
enum class PaymentType { CASH, CREDIT, INSTALLMENT }

/** روش دریافت وجه */
enum class PayMethod { CASH, CARD, TRANSFER, OTHER }

/** کانال ارسال پیام */
enum class Channel { SMS, WHATSAPP, TELEGRAM, EITAA }

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    /** برچسب‌ها با کاما جدا می‌شوند، مثل: همکار,خوش‌حساب */
    val tags: String = "",
    val note: String = "",
    /** تعداد روز معمول بین سفارش‌ها (برای پیش‌بینی سفارش بعدی)؛ ۰ یعنی خودکار */
    val reorderDays: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** واحد: کیلو، مثقال، بسته و... */
    val unit: String = "کیلو",
    val buyPrice: Long = 0,
    val sellPrice: Long = 0,
    /** موجودی فعلی (به واحد بالا) */
    val stock: Double = 0.0,
    /** آستانه هشدار کم‌بودن موجودی */
    val lowStock: Double = 0.0,
    val active: Boolean = true
)

@Entity(
    tableName = "sales",
    indices = [Index("customerId")]
)
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val dateMillis: Long = System.currentTimeMillis(),
    val total: Long = 0,
    /** مبلغ پرداخت‌شده هنگام فروش (برای نقد برابر total است) */
    val downPayment: Long = 0,
    val paymentType: PaymentType = PaymentType.CASH,
    val note: String = ""
)

@Entity(
    tableName = "sale_items",
    indices = [Index("saleId")]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val unit: String,
    val unitPrice: Long,
    val qty: Double
)

@Entity(
    tableName = "installments",
    indices = [Index("saleId"), Index("customerId")]
)
data class Installment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val customerId: Long,
    val dueMillis: Long,
    val amount: Long,
    val paidMillis: Long? = null
)

@Entity(
    tableName = "payments",
    indices = [Index("customerId")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val saleId: Long? = null,
    val installmentId: Long? = null,
    val amount: Long,
    val dateMillis: Long = System.currentTimeMillis(),
    val method: PayMethod = PayMethod.CASH,
    val note: String = ""
)

@Entity(tableName = "message_logs")
data class MessageLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val audience: String = "",
    val channel: Channel = Channel.SMS,
    val text: String = "",
    val recipientCount: Int = 0
)

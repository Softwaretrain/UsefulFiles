package ir.softwaretrain.soghatyar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Customer::class,
        Product::class,
        Sale::class,
        SaleItem::class,
        Installment::class,
        Payment::class,
        MessageLog::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun installmentDao(): InstallmentDao
    abstract fun paymentDao(): PaymentDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soghatyar.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}

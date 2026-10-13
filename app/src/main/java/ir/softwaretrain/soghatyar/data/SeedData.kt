package ir.softwaretrain.soghatyar.data

import ir.softwaretrain.soghatyar.domain.InstallmentPlanner

/** داده نمونه برای اولین اجرا تا کاربر با اپ آشنا شود */
object SeedData {

    suspend fun seed(db: AppDatabase) {
        val cDao = db.customerDao()
        if (cDao.allOnce().isNotEmpty()) return

        val pDao = db.productDao()
        val saffron = pDao.insert(Product(name = "زعفران سرگل", unit = "مثقال", buyPrice = 950_000, sellPrice = 1_100_000, stock = 40.0, lowStock = 10.0))
        val zereshk = pDao.insert(Product(name = "زرشک پفکی", unit = "کیلو", buyPrice = 620_000, sellPrice = 750_000, stock = 25.0, lowStock = 5.0))
        pDao.insert(Product(name = "عناب درجه‌یک", unit = "کیلو", buyPrice = 480_000, sellPrice = 600_000, stock = 2.0, lowStock = 5.0))
        pDao.insert(Product(name = "بادام درختی", unit = "کیلو", buyPrice = 1_150_000, sellPrice = 1_400_000, stock = 18.0, lowStock = 6.0))

        val day = 24L * 60 * 60 * 1000
        val now = System.currentTimeMillis()

        val rezaei = cDao.insert(Customer(name = "آقای رضایی", phone = "09120000001", tags = "همکار,خوش‌حساب", reorderDays = 42))
        val mohammadi = cDao.insert(Customer(name = "خانم محمدی", phone = "09120000002", tags = "فامیل", reorderDays = 45))
        cDao.insert(Customer(name = "دکتر کریمی", phone = "09120000003", tags = "دوست"))

        val saleDao = db.saleDao()
        val instDao = db.installmentDao()

        // فروش اقساطی برای آقای رضایی (۳۸ روز پیش)
        val saleDate = now - 38 * day
        val total = 7_500_000L
        val down = 0L
        val sid = saleDao.insertSale(
            Sale(customerId = rezaei, dateMillis = saleDate, total = total, downPayment = down, paymentType = PaymentType.INSTALLMENT)
        )
        saleDao.insertItems(
            listOf(
                SaleItem(saleId = sid, productId = saffron, productName = "زعفران سرگل", unit = "مثقال", unitPrice = 1_100_000, qty = 5.0),
                SaleItem(saleId = sid, productId = zereshk, productName = "زرشک پفکی", unit = "کیلو", unitPrice = 750_000, qty = 2.0)
            )
        )
        val plan = InstallmentPlanner.build(total - down, 3, saleDate + 30 * day)
        val insts = plan.map {
            Installment(saleId = sid, customerId = rezaei, dueMillis = it.dueMillis, amount = it.amount)
        }
        saleDao.insertInstallments(insts)
        // قسط اول پرداخت شده
        val firstInst = instDao.ofSale(sid).firstOrNull()
        if (firstInst != null) {
            instDao.update(firstInst.copy(paidMillis = saleDate + 30 * day))
            db.paymentDao().insert(
                Payment(customerId = rezaei, saleId = sid, installmentId = firstInst.id, amount = firstInst.amount, dateMillis = saleDate + 30 * day, note = "پرداخت قسط")
            )
        }

        // فروش نقدی قدیمی برای خانم محمدی (۴۵ روز پیش)
        val sid2 = saleDao.insertSale(
            Sale(customerId = mohammadi, dateMillis = now - 45 * day, total = 2_200_000, downPayment = 2_200_000, paymentType = PaymentType.CASH)
        )
        saleDao.insertItems(
            listOf(SaleItem(saleId = sid2, productId = saffron, productName = "زعفران سرگل", unit = "مثقال", unitPrice = 1_100_000, qty = 2.0))
        )
    }
}

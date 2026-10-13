package ir.softwaretrain.soghatyar

import android.app.Application
import ir.softwaretrain.soghatyar.data.AppDatabase
import ir.softwaretrain.soghatyar.data.Repo
import ir.softwaretrain.soghatyar.data.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** نگه‌دارنده سراسری مخزن داده (بدون کتابخانه تزریق وابستگی) */
object ServiceLocator {
    lateinit var repo: Repo
        private set

    fun init(app: Application) {
        val db = AppDatabase.get(app)
        repo = Repo(db)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            SeedData.seed(db)
        }
    }
}

class SoghatyarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}

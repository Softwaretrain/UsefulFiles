package ir.softwaretrain.soghatyar.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import ir.softwaretrain.soghatyar.data.AppDatabase
import ir.softwaretrain.soghatyar.data.Customer
import ir.softwaretrain.soghatyar.data.Product
import ir.softwaretrain.soghatyar.domain.Money
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** پشتیبان‌گیری JSON و خروجی CSV از حساب‌ها */
object BackupManager {

    private fun exportsDir(context: Context): File {
        val dir = File(context.filesDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** ساخت فایل بکاپ JSON از کل داده‌ها و بازگرداندن مسیر آن */
    suspend fun exportJson(context: Context, db: AppDatabase): File {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val customers = JSONArray()
        db.customerDao().allOnce().forEach { c ->
            customers.put(
                JSONObject()
                    .put("id", c.id).put("name", c.name).put("phone", c.phone)
                    .put("tags", c.tags).put("note", c.note).put("reorderDays", c.reorderDays)
            )
        }
        root.put("customers", customers)

        val products = JSONArray()
        db.productDao().allOnce().forEach { p ->
            products.put(
                JSONObject()
                    .put("id", p.id).put("name", p.name).put("unit", p.unit)
                    .put("buyPrice", p.buyPrice).put("sellPrice", p.sellPrice)
                    .put("stock", p.stock).put("lowStock", p.lowStock)
            )
        }
        root.put("products", products)

        val file = File(exportsDir(context), "soghatyar-backup.json")
        file.writeText(root.toString(2))
        return file
    }

    /** خروجی CSV از مانده حساب مشتری‌ها */
    suspend fun exportBalancesCsv(context: Context, db: AppDatabase): File {
        val sb = StringBuilder()
        sb.append("﻿") // BOM برای نمایش درست فارسی در اکسل
        sb.append("نام,تلفن,مانده (تومان)\n")
        db.customerDao().balancesOnce().forEach { row ->
            sb.append("\"${row.name}\",\"${row.phone}\",${row.balance}\n")
        }
        val file = File(exportsDir(context), "balances.csv")
        file.writeText(sb.toString())
        return file
    }

    fun share(context: Context, file: File, mime: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "ارسال فایل").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}

package ir.softwaretrain.soghatyar.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import ir.softwaretrain.soghatyar.data.Channel

/** ارسال پیامک از سیم‌کارت گوشی و باز کردن اپ‌های پیام‌رسان */
object SmsSender {

    /** جایگزینی نام مشتری در متن (قالب {نام}) */
    fun personalize(template: String, name: String): String =
        template.replace("{نام}", name).replace("{name}", name)

    @Suppress("DEPRECATION")
    private fun manager(context: Context): SmsManager {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
    }

    /**
     * ارسال مستقیم پیامک از سیم‌کارت. نیازمند مجوز SEND_SMS.
     * خروجی: تعداد پیام‌های ارسال‌شده.
     */
    fun sendSms(context: Context, phone: String, text: String): Boolean {
        if (phone.isBlank()) return false
        return try {
            val sms = manager(context)
            val parts = sms.divideMessage(text)
            sms.sendMultipartTextMessage(normalize(phone), null, parts, null, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** ارسال گروهی مستقیم؛ خروجی: تعداد موفق */
    fun sendBulkSms(context: Context, recipients: List<Pair<String, String>>): Int {
        var ok = 0
        for ((phone, text) in recipients) {
            if (sendSms(context, phone, text)) ok++
        }
        return ok
    }

    private fun normalize(phone: String): String =
        phone.trim().replace(" ", "").replace("-", "")

    /** باز کردن اپ پیام‌رسان برای یک مخاطب (واتس‌اپ/تلگرام/ایتا/پیامک) */
    fun openChannel(context: Context, channel: Channel, phone: String, text: String) {
        val p = normalize(phone)
        val intent = when (channel) {
            Channel.WHATSAPP -> Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/${toIntl(p)}?text=${Uri.encode(text)}")
            )
            Channel.TELEGRAM -> Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://t.me/share/url?url=&text=${Uri.encode(text)}")
            )
            Channel.EITAA -> Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://eitaa.com/")
            )
            Channel.SMS -> Intent(
                Intent.ACTION_VIEW,
                Uri.parse("sms:$p")
            ).apply { putExtra("sms_body", text) }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    /** تبدیل ۰۹۱۲... به 98912... برای لینک واتس‌اپ */
    private fun toIntl(phone: String): String {
        var p = phone
        if (p.startsWith("0")) p = "98" + p.substring(1)
        return p
    }
}

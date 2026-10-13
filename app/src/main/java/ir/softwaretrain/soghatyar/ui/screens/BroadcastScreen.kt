package ir.softwaretrain.soghatyar.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.softwaretrain.soghatyar.data.Channel
import ir.softwaretrain.soghatyar.data.CustomerWithBalance
import ir.softwaretrain.soghatyar.data.MessageLog
import ir.softwaretrain.soghatyar.domain.Money
import ir.softwaretrain.soghatyar.ui.AppViewModel
import ir.softwaretrain.soghatyar.util.SmsSender

private const val DAY = 24L * 60 * 60 * 1000

private data class Audience(val key: String, val label: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BroadcastScreen(vm: AppViewModel) {
    val context = LocalContext.current
    val customers by vm.customers.collectAsState(initial = emptyList())
    val now = System.currentTimeMillis()

    var template by remember { mutableStateOf("new") }
    var audience by remember { mutableStateOf("all") }
    var channel by remember { mutableStateOf(Channel.SMS) }
    var text by remember {
        mutableStateOf("سلام {نام} عزیز\nزعفران سرگل محصول امسال رسید. برای مشتری‌های قدیمی تا آخر هفته ۱۰٪ تخفیف داریم.\nبرای سفارش همین پیام را پاسخ دهید.\nیوسف")
    }

    fun recipients(): List<CustomerWithBalance> = customers.filter { c ->
        c.phone.isNotBlank() && when (audience) {
            "debtors" -> c.balance > 0
            "inactive" -> (c.lastSaleMillis == null) || (now - c.lastSaleMillis!! >= 60 * DAY)
            else -> true
        }
    }

    val smsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val list = recipients().map { it.phone to SmsSender.personalize(text, it.name) }
            val sent = SmsSender.sendBulkSms(context, list)
            vm.logMessage(MessageLog(audience = audience, channel = Channel.SMS, text = text, recipientCount = sent))
            Toast.makeText(context, Money.faDigits("$sent پیامک ارسال شد"), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "برای ارسال پیامک، مجوز لازم است", Toast.LENGTH_LONG).show()
        }
    }

    val templates = listOf(
        "new" to "جنس جدید رسید",
        "discount" to "تخفیف ویژه",
        "balance" to "یادآوری مانده حساب",
        "greet" to "تبریک مناسبت"
    )
    val audiences = listOf(
        Audience("all", "همه"),
        Audience("debtors", "بدهکارها"),
        Audience("inactive", "۶۰ روز خرید نکرده")
    )

    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(Modifier.padding(top = 6.dp))
        Text("اعلام به مشتری‌ها", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)

        Text("چه چیزی اعلام کنی؟", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            templates.forEach { (key, label) ->
                FilterChip(selected = template == key, onClick = {
                    template = key
                    text = templateText(key)
                }, label = { Text(label) })
            }
        }

        Text("برای چه کسانی؟", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            audiences.forEach { a ->
                val count = customers.count { c ->
                    c.phone.isNotBlank() && when (a.key) {
                        "debtors" -> c.balance > 0
                        "inactive" -> (c.lastSaleMillis == null) || (now - c.lastSaleMillis!! >= 60 * DAY)
                        else -> true
                    }
                }
                FilterChip(
                    selected = audience == a.key,
                    onClick = { audience = a.key },
                    label = { Text(Money.faDigits("${a.label} ($count)")) }
                )
            }
        }

        Row {
            Text("متن پیام", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("{نام} جای نام هر نفر می‌نشیند", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth(), minLines = 4)

        Text("ارسال از طریق", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = channel == Channel.SMS, onClick = { channel = Channel.SMS }, label = { Text("پیامک") })
            FilterChip(selected = channel == Channel.WHATSAPP, onClick = { channel = Channel.WHATSAPP }, label = { Text("واتس‌اپ") })
            FilterChip(selected = channel == Channel.TELEGRAM, onClick = { channel = Channel.TELEGRAM }, label = { Text("تلگرام") })
            FilterChip(selected = channel == Channel.EITAA, onClick = { channel = Channel.EITAA }, label = { Text("ایتا") })
        }

        val count = recipients().size
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        if (channel == Channel.SMS) {
                            smsPermission.launch(android.Manifest.permission.SEND_SMS)
                        } else {
                            val first = recipients().firstOrNull()
                            SmsSender.openChannel(context, channel, first?.phone ?: "", SmsSender.personalize(text, first?.name ?: ""))
                            vm.logMessage(MessageLog(audience = audience, channel = channel, text = text, recipientCount = count))
                        }
                    },
                    enabled = count > 0,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(Money.faDigits("ارسال به $count نفر")) }
                if (channel != Channel.SMS) {
                    Text(
                        "برای واتس‌اپ/تلگرام/ایتا، اپ مربوطه باز می‌شود تا پیام را بفرستی.",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        Spacer(Modifier.padding(bottom = 8.dp))
    }
}

private fun templateText(key: String): String = when (key) {
    "discount" -> "سلام {نام} عزیز\nاین هفته روی زعفران و زرشک ۱۰٪ تخفیف ویژه داریم.\nبرای سفارش پیام بدهید.\nیوسف"
    "balance" -> "سلام {نام} عزیز\nیادآوری دوستانه بابت تسویه حساب خرید قبلی. ممنون از همکاری‌تان.\nیوسف"
    "greet" -> "سلام {نام} عزیز\nمناسبت پیش‌رو را تبریک می‌گویم. به امید سلامتی و روزهای خوش.\nیوسف"
    else -> "سلام {نام} عزیز\nزعفران سرگل محصول امسال رسید. برای مشتری‌های قدیمی تا آخر هفته ۱۰٪ تخفیف داریم.\nبرای سفارش همین پیام را پاسخ دهید.\nیوسف"
}

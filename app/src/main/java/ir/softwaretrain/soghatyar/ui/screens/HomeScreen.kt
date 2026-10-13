package ir.softwaretrain.soghatyar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ir.softwaretrain.soghatyar.data.Channel
import ir.softwaretrain.soghatyar.domain.Money
import ir.softwaretrain.soghatyar.domain.PersianDate
import ir.softwaretrain.soghatyar.domain.ReorderPredictor
import ir.softwaretrain.soghatyar.ui.AppViewModel
import ir.softwaretrain.soghatyar.ui.components.StatBox
import ir.softwaretrain.soghatyar.ui.nav.Routes
import ir.softwaretrain.soghatyar.util.SmsSender

private const val DAY = 24L * 60 * 60 * 1000

@Composable
fun HomeScreen(vm: AppViewModel, nav: NavController) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()

    val customers by vm.customers.collectAsState(initial = emptyList())
    val lowStock by vm.lowStock.collectAsState(initial = emptyList())
    val overdue by vm.dueInstallments(now).collectAsState(initial = emptyList())
    val salesMonth by vm.salesSince(now - 30 * DAY).collectAsState(initial = null)

    val totalOutstanding = customers.sumOf { if (it.balance > 0) it.balance else 0L }
    val dueWeek = customers.isNotEmpty().let {
        overdue.count { inst -> inst.dueMillis >= now - 7 * DAY }
    }
    val overduePeople = overdue.map { it.customerId }.distinct().size

    // مشتری‌هایی که احتمالاً وقت سفارش مجددشان است
    val reorderDue = customers.filter {
        ReorderPredictor.isDueSoon(it.lastSaleMillis, it.reorderDays, now)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.padding(top = 6.dp))
            Text(PersianDate.formatShort(now), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("سلام یوسف", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("کل طلب شما از مشتری‌ها", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text(Money.toman(totalOutstanding), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.padding(top = 6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox("قسط این هفته", Money.faDigits("$dueWeek مورد"), Modifier.weight(1f))
                        StatBox("عقب‌افتاده", Money.faDigits("$overduePeople نفر"), Modifier.weight(1f))
                        StatBox("فروش ۳۰ روز", Money.short(salesMonth ?: 0L), Modifier.weight(1f))
                    }
                }
            }
        }

        if (overdue.isNotEmpty() || reorderDue.isNotEmpty()) {
            item { Text("کارهای امروز", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 6.dp)) }
        }

        items(overdue, key = { "inst-${it.id}" }) { inst ->
            val c = customers.firstOrNull { it.id == inst.customerId }
            TaskRow(
                title = c?.name ?: "مشتری",
                subtitle = "قسط سررسیده — ${Money.toman(inst.amount)} · ${PersianDate.formatNumeric(inst.dueMillis)}",
                actionLabel = "یادآوری",
                primary = true
            ) {
                val name = c?.name ?: ""
                val text = SmsSender.personalize("سلام {نام} عزیز، یادآوری می‌کنم قسط ${Money.toman(inst.amount)} سررسید شده. ممنون می‌شوم هماهنگ کنید. یوسف", name)
                SmsSender.openChannel(context, Channel.SMS, c?.phone ?: "", text)
            }
        }

        items(reorderDue, key = { "re-${it.id}" }) { c ->
            val days = ReorderPredictor.daysSince(c.lastSaleMillis, now) ?: 0
            TaskRow(
                title = c.name,
                subtitle = "احتمالاً جنسش رو به اتمام است (${Money.faDigits("$days روز پیش خرید")})",
                actionLabel = "پیشنهاد فروش",
                primary = false
            ) {
                nav.navigate(Routes.sale(c.id))
            }
        }

        items(lowStock, key = { "low-${it.id}" }) { p ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFBEFD3)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "موجودی «${p.name}» کم شده: ${Money.faDigits(p.stock.toString())} ${p.unit}",
                    color = Color(0xFF5E3D00),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        item { Spacer(Modifier.padding(bottom = 8.dp)) }
    }
}

@Composable
private fun TaskRow(
    title: String,
    subtitle: String,
    actionLabel: String,
    primary: Boolean,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (primary) {
                Button(onClick = onAction) { Text(actionLabel) }
            } else {
                OutlinedButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

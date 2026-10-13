package ir.softwaretrain.soghatyar.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ir.softwaretrain.soghatyar.data.Channel
import ir.softwaretrain.soghatyar.data.PayMethod
import ir.softwaretrain.soghatyar.data.Payment
import ir.softwaretrain.soghatyar.domain.Money
import ir.softwaretrain.soghatyar.domain.PersianDate
import ir.softwaretrain.soghatyar.domain.ReorderPredictor
import ir.softwaretrain.soghatyar.ui.AppViewModel
import ir.softwaretrain.soghatyar.ui.components.Chip
import ir.softwaretrain.soghatyar.ui.nav.Routes
import ir.softwaretrain.soghatyar.util.SmsSender

@Composable
fun CustomerDetailScreen(vm: AppViewModel, nav: NavController, customerId: Long) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()

    val customer by vm.customer(customerId).collectAsState(initial = null)
    val balance by vm.balance(customerId).collectAsState(initial = 0L)
    val installments by vm.installmentsOf(customerId).collectAsState(initial = emptyList())
    val sales by vm.salesOf(customerId).collectAsState(initial = emptyList())

    var showPay by remember { mutableStateOf(false) }
    val c = customer

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.padding(top = 6.dp)) }

        item {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(c?.name ?: "...", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    val tagList = (c?.tags ?: "").split(",").map { it.trim() }.filter { it.isNotBlank() }
                    if (tagList.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { tagList.forEach { Chip(it) } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            c?.phone?.let {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            }
                        }, modifier = Modifier.weight(1f)) { Text("تماس") }
                        OutlinedButton(onClick = {
                            SmsSender.openChannel(context, Channel.SMS, c?.phone ?: "", "")
                        }, modifier = Modifier.weight(1f)) { Text("پیامک") }
                        OutlinedButton(onClick = {
                            SmsSender.openChannel(context, Channel.WHATSAPP, c?.phone ?: "", "")
                        }, modifier = Modifier.weight(1f)) { Text("واتس‌اپ") }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("مانده بدهی", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                        Text(Money.grouped(if (balance > 0) balance else 0L), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("سفارش بعدی احتمالی", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val next = ReorderPredictor.nextOrderMillis(c?.let { lastSale(sales) } ?: lastSale(sales), c?.reorderDays ?: 0)
                        Text(if (next != null) PersianDate.formatNumeric(next) else "—", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        val unpaid = installments.filter { it.paidMillis == null }
        if (installments.isNotEmpty()) {
            item { Text("اقساط", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
            items(installments, key = { it.id }) { inst ->
                Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(PersianDate.formatNumeric(inst.dueMillis), fontSize = 13.sp)
                            Text(Money.toman(inst.amount), fontWeight = FontWeight.Bold)
                        }
                        when {
                            inst.paidMillis != null -> Text("پرداخت شد", color = MaterialTheme.colorScheme.tertiary, fontSize = 12.sp)
                            inst.dueMillis < now -> Button(onClick = { vm.payInstallment(inst.id, PayMethod.CASH) }) { Text("ثبت پرداخت") }
                            else -> OutlinedButton(onClick = { vm.payInstallment(inst.id, PayMethod.CASH) }) { Text("پرداخت") }
                        }
                    }
                }
            }
        }

        if (sales.isNotEmpty()) {
            item { Text("خریدهای اخیر", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
            items(sales.take(10), key = { it.id }) { s ->
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(Money.toman(s.total), modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(PersianDate.formatNumeric(s.dateMillis), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedButton(onClick = { showPay = true }, modifier = Modifier.weight(1f)) { Text("ثبت دریافت") }
                Button(onClick = { nav.navigate(Routes.sale(customerId)) }, modifier = Modifier.weight(1f)) { Text("فروش جدید") }
            }
        }
    }

    if (showPay) {
        var amount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPay = false },
            title = { Text("ثبت دریافت وجه") },
            text = {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { s -> amount = s.filter { it.isDigit() } },
                    label = { Text("مبلغ (تومان)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val a = amount.toLongOrNull() ?: 0L
                    if (a > 0) {
                        vm.recordPayment(Payment(customerId = customerId, amount = a, method = PayMethod.CASH))
                        showPay = false
                    }
                }) { Text("ذخیره") }
            },
            dismissButton = { TextButton(onClick = { showPay = false }) { Text("انصراف") } }
        )
    }
}

private fun lastSale(sales: List<ir.softwaretrain.soghatyar.data.Sale>): Long? =
    sales.maxByOrNull { it.dateMillis }?.dateMillis

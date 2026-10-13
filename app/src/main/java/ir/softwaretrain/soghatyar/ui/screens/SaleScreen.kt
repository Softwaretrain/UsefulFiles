package ir.softwaretrain.soghatyar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ir.softwaretrain.soghatyar.data.PaymentType
import ir.softwaretrain.soghatyar.data.Sale
import ir.softwaretrain.soghatyar.data.SaleItem
import ir.softwaretrain.soghatyar.domain.InstallmentPlanner
import ir.softwaretrain.soghatyar.domain.Money
import ir.softwaretrain.soghatyar.domain.PersianDate
import ir.softwaretrain.soghatyar.ui.AppViewModel

private const val DAY = 24L * 60 * 60 * 1000

@Composable
fun SaleScreen(vm: AppViewModel, nav: NavController, customerId: Long) {
    val products by vm.products.collectAsState(initial = emptyList())
    val customers by vm.customers.collectAsState(initial = emptyList())

    var selectedCustomer by remember { mutableStateOf(customerId) }
    var showCustomerPick by remember { mutableStateOf(false) }
    val cart = remember { mutableStateMapOf<Long, Double>() }
    var paymentType by remember { mutableStateOf(PaymentType.CASH) }
    var downText by remember { mutableStateOf("") }
    var installCount by remember { mutableStateOf("2") }

    val total = products.sumOf { (cart[it.id] ?: 0.0) * it.sellPrice }.toLong()
    val customerName = customers.firstOrNull { it.id == selectedCustomer }?.name

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.padding(top = 6.dp)); Text("فروش جدید", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }

        item {
            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable { showCustomerPick = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                    Text(customerName ?: "انتخاب مشتری", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("تغییر", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
            }
        }

        item { Text("کالاها", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
        items(products, key = { it.id }) { p ->
            val qty = cart[p.id] ?: 0.0
            Card(
                shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(p.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("هر ${p.unit}: ${Money.toman(p.sellPrice)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            val n = (qty - 1).coerceAtLeast(0.0); if (n == 0.0) cart.remove(p.id) else cart[p.id] = n
                        }) { Text("−") }
                        Text(Money.faDigits(trim(qty)), fontWeight = FontWeight.Bold)
                        Button(onClick = { cart[p.id] = qty + 1 }) { Text("+") }
                    }
                }
            }
        }

        item { Text("نحوه پرداخت", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = paymentType == PaymentType.CASH, onClick = { paymentType = PaymentType.CASH }, label = { Text("نقد") })
                FilterChip(selected = paymentType == PaymentType.CREDIT, onClick = { paymentType = PaymentType.CREDIT }, label = { Text("نسیه") })
                FilterChip(selected = paymentType == PaymentType.INSTALLMENT, onClick = { paymentType = PaymentType.INSTALLMENT }, label = { Text("اقساط") })
            }
        }

        if (paymentType == PaymentType.INSTALLMENT) {
            item {
                Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = downText, onValueChange = { s -> downText = s.filter { it.isDigit() } },
                            label = { Text("پیش‌پرداخت (تومان)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = installCount, onValueChange = { s -> installCount = s.filter { it.isDigit() } },
                            label = { Text("تعداد قسط ماهانه") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        val down = downText.toLongOrNull() ?: 0L
                        val count = installCount.toIntOrNull() ?: 0
                        val financed = (total - down).coerceAtLeast(0)
                        if (count > 0 && financed > 0) {
                            val plan = InstallmentPlanner.build(financed, count, System.currentTimeMillis() + 30 * DAY)
                            Text("هر قسط حدود ${Money.toman(plan.first().amount)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("اولین سررسید: ${PersianDate.formatNumeric(plan.first().dueMillis)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("جمع کل", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text(Money.toman(total), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.padding(top = 10.dp))
                    Button(
                        onClick = {
                            if (selectedCustomer == 0L || total <= 0) return@Button
                            val down = when (paymentType) {
                                PaymentType.CASH -> total
                                PaymentType.CREDIT -> 0L
                                PaymentType.INSTALLMENT -> downText.toLongOrNull() ?: 0L
                            }
                            val items = products.filter { (cart[it.id] ?: 0.0) > 0 }.map {
                                SaleItem(saleId = 0, productId = it.id, productName = it.name, unit = it.unit, unitPrice = it.sellPrice, qty = cart[it.id] ?: 0.0)
                            }
                            val sale = Sale(customerId = selectedCustomer, total = total, downPayment = down, paymentType = paymentType)
                            val count = if (paymentType == PaymentType.INSTALLMENT) (installCount.toIntOrNull() ?: 0) else 0
                            vm.createSale(sale, items, count, System.currentTimeMillis() + 30 * DAY) {
                                nav.popBackStack()
                            }
                        },
                        enabled = selectedCustomer != 0L && total > 0,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("ثبت فروش") }
                }
            }
            Spacer(Modifier.padding(bottom = 8.dp))
        }
    }

    if (showCustomerPick) {
        AlertDialog(
            onDismissRequest = { showCustomerPick = false },
            title = { Text("انتخاب مشتری") },
            text = {
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(customers, key = { it.id }) { c ->
                        Box(Modifier.fillMaxWidth().clickable { selectedCustomer = c.id; showCustomerPick = false }.padding(vertical = 12.dp)) {
                            Text(c.name, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCustomerPick = false }) { Text("بستن") } }
        )
    }
}

private fun trim(d: Double): String =
    if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()

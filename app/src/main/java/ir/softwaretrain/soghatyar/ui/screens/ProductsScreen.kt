package ir.softwaretrain.soghatyar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.softwaretrain.soghatyar.data.AppDatabase
import ir.softwaretrain.soghatyar.data.Product
import ir.softwaretrain.soghatyar.domain.Money
import ir.softwaretrain.soghatyar.ui.AppViewModel
import ir.softwaretrain.soghatyar.util.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProductsScreen(vm: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val products by vm.allProducts.collectAsState(initial = emptyList())
    var editing by remember { mutableStateOf<Product?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showEditor = true },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("کالای جدید") }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.padding(top = 6.dp)); Text("کالاها و موجودی", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        scope.launch {
                            val db = AppDatabase.get(context)
                            val file = withContext(Dispatchers.IO) { BackupManager.exportJson(context, db) }
                            BackupManager.share(context, file, "application/json")
                        }
                    }, modifier = Modifier.weight(1f)) { Text("پشتیبان‌گیری") }
                    OutlinedButton(onClick = {
                        scope.launch {
                            val db = AppDatabase.get(context)
                            val file = withContext(Dispatchers.IO) { BackupManager.exportBalancesCsv(context, db) }
                            BackupManager.share(context, file, "text/csv")
                        }
                    }, modifier = Modifier.weight(1f)) { Text("خروجی اکسل مانده‌ها") }
                }
            }

            items(products, key = { it.id }) { p ->
                val low = p.lowStock > 0 && p.stock <= p.lowStock
                Card(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().clickable { editing = p; showEditor = true },
                    colors = CardDefaults.cardColors(
                        containerColor = if (low) Color(0xFFFBEFD3) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("فروش: ${Money.toman(p.sellPrice)} · سود: ${Money.grouped(p.sellPrice - p.buyPrice)}",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(Money.faDigits("${trimNum(p.stock)} ${p.unit}"), fontWeight = FontWeight.Bold,
                                color = if (low) Color(0xFF8A5A00) else MaterialTheme.colorScheme.onSurface)
                            if (low) Text("موجودی کم", fontSize = 11.sp, color = Color(0xFF8A5A00))
                        }
                    }
                }
            }
            item { Spacer(Modifier.padding(bottom = 8.dp)) }
        }
    }

    if (showEditor) {
        ProductEditor(
            product = editing,
            onDismiss = { showEditor = false },
            onSave = { p -> vm.saveProduct(p); showEditor = false }
        )
    }
}

@Composable
private fun ProductEditor(product: Product?, onDismiss: () -> Unit, onSave: (Product) -> Unit) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var unit by remember { mutableStateOf(product?.unit ?: "کیلو") }
    var buy by remember { mutableStateOf(product?.buyPrice?.toString() ?: "") }
    var sell by remember { mutableStateOf(product?.sellPrice?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.let { trimNum(it) } ?: "") }
    var low by remember { mutableStateOf(product?.lowStock?.let { trimNum(it) } ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "کالای جدید" else "ویرایش کالا") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام کالا") }, singleLine = true)
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("واحد (کیلو/مثقال/بسته)") }, singleLine = true)
                OutlinedTextField(value = buy, onValueChange = { s -> buy = s.filter { it.isDigit() } }, label = { Text("قیمت خرید") }, singleLine = true)
                OutlinedTextField(value = sell, onValueChange = { s -> sell = s.filter { it.isDigit() } }, label = { Text("قیمت فروش") }, singleLine = true)
                OutlinedTextField(value = stock, onValueChange = { s -> stock = s.filter { it.isDigit() || it == '.' } }, label = { Text("موجودی") }, singleLine = true)
                OutlinedTextField(value = low, onValueChange = { s -> low = s.filter { it.isDigit() || it == '.' } }, label = { Text("آستانه هشدار موجودی") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onSave(
                    (product ?: Product(name = "")).copy(
                        name = name.trim(),
                        unit = unit.trim().ifBlank { "کیلو" },
                        buyPrice = buy.toLongOrNull() ?: 0,
                        sellPrice = sell.toLongOrNull() ?: 0,
                        stock = stock.toDoubleOrNull() ?: 0.0,
                        lowStock = low.toDoubleOrNull() ?: 0.0
                    )
                )
            }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

private fun trimNum(d: Double): String =
    if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()

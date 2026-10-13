package ir.softwaretrain.soghatyar.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ir.softwaretrain.soghatyar.data.Customer
import ir.softwaretrain.soghatyar.domain.Money
import ir.softwaretrain.soghatyar.ui.AppViewModel
import ir.softwaretrain.soghatyar.ui.components.Avatar
import ir.softwaretrain.soghatyar.ui.components.Chip
import ir.softwaretrain.soghatyar.ui.nav.Routes
import ir.softwaretrain.soghatyar.util.ContactsImporter

@Composable
fun CustomersScreen(vm: AppViewModel, nav: NavController) {
    val context = LocalContext.current
    val customers by vm.customers.collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    var importedContacts by remember { mutableStateOf<List<ContactsImporter.Contact>>(emptyList()) }
    var showContacts by remember { mutableStateOf(false) }

    val contactsPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            importedContacts = ContactsImporter.load(context)
            showContacts = true
        }
    }

    val filtered = customers.filter {
        query.isBlank() || it.name.contains(query) || it.phone.contains(query)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("مشتری جدید") }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.padding(top = 6.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("جستجوی مشتری") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.padding(top = 4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    Money.faDigits("${customers.size} مشتری"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    contactsPermission.launch(android.Manifest.permission.READ_CONTACTS)
                }) {
                    Icon(Icons.Filled.ImportContacts, null)
                    Spacer(Modifier.padding(start = 4.dp))
                    Text("از مخاطبین")
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { c ->
                    CustomerRow(
                        name = c.name,
                        phone = c.phone,
                        tags = c.tags,
                        balance = c.balance
                    ) { nav.navigate(Routes.customer(c.id)) }
                }
            }
        }
    }

    if (showAdd) {
        AddCustomerDialog(
            onDismiss = { showAdd = false },
            onSave = { name, phone, tags ->
                vm.saveCustomer(Customer(name = name, phone = phone, tags = tags))
                showAdd = false
            }
        )
    }

    if (showContacts) {
        ContactPickerDialog(
            contacts = importedContacts,
            onDismiss = { showContacts = false },
            onPick = { contact ->
                vm.saveCustomer(Customer(name = contact.name, phone = contact.phone))
                showContacts = false
            }
        )
    }
}

@Composable
private fun CustomerRow(name: String, phone: String, tags: String, balance: Long, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            Avatar(name, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
            Spacer(Modifier.padding(start = 12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                val tagList = tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                if (tagList.isEmpty()) {
                    Text(phone.ifBlank { "بدون شماره" }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        tagList.take(2).forEach { Chip(it) }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (balance > 0) Money.grouped(balance) else "تسویه",
                    fontWeight = FontWeight.Bold,
                    color = if (balance > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
                if (balance > 0) Text("مانده", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AddCustomerDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("مشتری جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("شماره تماس") }, singleLine = true)
                OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text("برچسب‌ها (با کاما)") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim(), phone.trim(), tags.trim()) }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun ContactPickerDialog(
    contacts: List<ContactsImporter.Contact>,
    onDismiss: () -> Unit,
    onPick: (ContactsImporter.Contact) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب از مخاطبین") },
        text = {
            if (contacts.isEmpty()) {
                Text("مخاطبی پیدا نشد.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(contacts) { c ->
                        Box(
                            Modifier.fillMaxWidth().clickable { onPick(c) }.padding(vertical = 12.dp)
                        ) {
                            Column {
                                Text(c.name, fontWeight = FontWeight.Bold)
                                Text(Money.faDigits(c.phone), fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("بستن") } }
    )
}

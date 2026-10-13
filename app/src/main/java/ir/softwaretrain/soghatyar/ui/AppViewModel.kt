package ir.softwaretrain.soghatyar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.softwaretrain.soghatyar.ServiceLocator
import ir.softwaretrain.soghatyar.data.Customer
import ir.softwaretrain.soghatyar.data.MessageLog
import ir.softwaretrain.soghatyar.data.PayMethod
import ir.softwaretrain.soghatyar.data.Payment
import ir.softwaretrain.soghatyar.data.Product
import ir.softwaretrain.soghatyar.data.Repo
import ir.softwaretrain.soghatyar.data.Sale
import ir.softwaretrain.soghatyar.data.SaleItem
import kotlinx.coroutines.launch

/** ویومدل مشترک برنامه؛ هم جریان‌های داده و هم عملیات را فراهم می‌کند */
class AppViewModel(val repo: Repo) : ViewModel() {

    // جریان‌ها
    val customers = repo.customersWithBalance()
    val products = repo.activeProducts()
    val allProducts = repo.allProducts()
    val lowStock = repo.lowStockProducts()
    val messages = repo.recentMessages()

    fun customer(id: Long) = repo.customer(id)
    fun balance(id: Long) = repo.balance(id)
    fun salesOf(id: Long) = repo.salesOf(id)
    fun installmentsOf(id: Long) = repo.installmentsOf(id)
    fun paymentsOf(id: Long) = repo.paymentsOf(id)
    fun salesSince(from: Long) = repo.salesSince(from)
    fun paymentsSince(from: Long) = repo.paymentsSince(from)
    fun dueInstallments(until: Long) = repo.dueInstallments(until)

    // عملیات
    fun saveCustomer(c: Customer, onDone: (Long) -> Unit = {}) = viewModelScope.launch {
        onDone(repo.saveCustomer(c))
    }

    fun saveProduct(p: Product, onDone: (Long) -> Unit = {}) = viewModelScope.launch {
        onDone(repo.saveProduct(p))
    }

    fun createSale(
        sale: Sale,
        items: List<SaleItem>,
        installmentCount: Int,
        firstDueMillis: Long,
        onDone: (Long) -> Unit = {}
    ) = viewModelScope.launch {
        onDone(repo.createSale(sale, items, installmentCount, firstDueMillis))
    }

    fun recordPayment(p: Payment, onDone: () -> Unit = {}) = viewModelScope.launch {
        repo.recordPayment(p)
        onDone()
    }

    fun payInstallment(id: Long, method: PayMethod, onDone: () -> Unit = {}) = viewModelScope.launch {
        repo.payInstallment(id, method, System.currentTimeMillis())
        onDone()
    }

    fun logMessage(m: MessageLog) = viewModelScope.launch { repo.logMessage(m) }

    suspend fun customerById(id: Long) = repo.customerById(id)

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppViewModel(ServiceLocator.repo) as T
            }
        }
    }
}

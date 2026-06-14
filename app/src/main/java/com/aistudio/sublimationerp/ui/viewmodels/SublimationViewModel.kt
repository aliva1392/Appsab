package com.aistudio.sublimationerp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aistudio.sublimationerp.data.db.entity.Customer
import com.aistudio.sublimationerp.data.db.entity.Fabric
import com.aistudio.sublimationerp.data.db.entity.Order
import com.aistudio.sublimationerp.data.db.entity.Expense
import com.aistudio.sublimationerp.data.db.entity.Payment
import com.aistudio.sublimationerp.data.db.entity.Cheque
import com.aistudio.sublimationerp.data.repository.SublimationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

class SublimationViewModel(private val repository: SublimationRepository) : ViewModel() {

    val customers: StateFlow<List<Customer>> = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _reportStartDate = MutableStateFlow<Long>(0L)
    private val _reportEndDate = MutableStateFlow<Long>(System.currentTimeMillis())
    
    fun setDateRange(start: Long, end: Long) {
        _reportStartDate.value = start
        _reportEndDate.value = end
    }

    val orders: StateFlow<List<Order>> = combine(_reportStartDate, _reportEndDate) { start, end -> Pair(start, end) }
        .flatMapLatest { (start, end) ->
            if (start == 0L) repository.allOrders else repository.getOrdersByDateRange(start, end)
        }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val fabrics: StateFlow<List<Fabric>> = repository.allFabrics.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val expenses: StateFlow<List<Expense>> = combine(_reportStartDate, _reportEndDate) { start, end -> Pair(start, end) }
        .flatMapLatest { (start, end) ->
            if (start == 0L) repository.allExpenses else repository.getExpensesByDateRange(start, end)
        }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())
    
    val payments: StateFlow<List<Payment>> = repository.allPayments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val cheques: StateFlow<List<Cheque>> = repository.allCheques.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val totalCustomerDebt = repository.totalCustomerDebt.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0
    )
    
    val readyOrdersCount = repository.readyOrdersCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )
    
    val lowStockFabricsCount = repository.getLowStockFabricsCount(com.aistudio.sublimationerp.data.AppConstants.LOW_STOCK_THRESHOLD_METERS).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    val todayOrdersCount = repository.getTodayOrdersCount(getStartOfDay()).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )
    
    val todaySalesAmount = repository.getTodaySalesAmount(getStartOfDay()).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0
    )

    val totalExpenses = expenses.map { list -> list.sumOf { it.amount } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val grossProfit = combine(orders, fabrics) { orderList, fabricList ->
        var gross = 0.0
        var cost = 0.0
        for (order in orderList) {
            gross += order.totalAmount
            val fabric = fabricList.find { it.id == order.fabricId }
            if (fabric != null && order.length != null) {
                val qty = if(order.quantity > 0) order.quantity else 1
                cost += (order.length * qty) * fabric.purchasePrice
            }
        }
        gross - cost
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val netProfit = combine(grossProfit, totalExpenses) { gross, expenses ->
        gross - expenses
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun updateCustomer(customer: Customer) = viewModelScope.launch { repository.updateCustomer(customer) }
    fun deleteCustomer(customer: Customer) = viewModelScope.launch { repository.deleteCustomer(customer) }

    fun updateOrder(order: Order) = viewModelScope.launch { repository.updateOrder(order) }
    fun deleteOrder(order: Order) = viewModelScope.launch { repository.deleteOrder(order) }

    fun updateFabric(fabric: Fabric) = viewModelScope.launch { repository.updateFabric(fabric) }
    fun deleteFabric(fabric: Fabric) = viewModelScope.launch { repository.deleteFabric(fabric) }

    fun updateExpense(expense: Expense) = viewModelScope.launch { repository.updateExpense(expense) }
    fun deleteExpense(expense: Expense) = viewModelScope.launch { repository.deleteExpense(expense) }

    fun updatePayment(payment: Payment) = viewModelScope.launch { repository.updatePayment(payment) }
    fun deletePayment(payment: Payment) = viewModelScope.launch { repository.deletePayment(payment) }

    fun updateCheque(cheque: Cheque) = viewModelScope.launch { repository.updateCheque(cheque) }
    fun deleteCheque(cheque: Cheque) = viewModelScope.launch { repository.deleteCheque(cheque) }

    fun addCustomer(name: String, phone: String, address: String) {
        viewModelScope.launch {
            repository.insertCustomer(Customer(name = name, phone = phone, address = address))
        }
    }

    fun addFabric(name: String, purchasePrice: Double, stock: Double) {
        viewModelScope.launch {
            repository.insertFabric(Fabric(name = name, purchasePrice = purchasePrice, stock = stock))
        }
    }
    
    fun addOrder(order: Order) {
        viewModelScope.launch {
            repository.insertOrder(order)
        }
    }
    
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }
    
    fun addPayment(payment: Payment) {
        viewModelScope.launch {
            repository.insertPayment(payment)
        }
    }
    
    fun addCheque(cheque: Cheque) {
        viewModelScope.launch {
            repository.insertCheque(cheque)
        }
    }
}

class SublimationViewModelFactory(private val repository: SublimationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SublimationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SublimationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

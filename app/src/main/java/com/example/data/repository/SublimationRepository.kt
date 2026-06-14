package com.aistudio.sublimationerp.data.repository

import com.aistudio.sublimationerp.data.db.dao.CustomerDao
import com.aistudio.sublimationerp.data.db.dao.OrderDao
import com.aistudio.sublimationerp.data.db.dao.FabricDao
import com.aistudio.sublimationerp.data.db.dao.ExpenseDao
import com.aistudio.sublimationerp.data.db.dao.PaymentDao
import com.aistudio.sublimationerp.data.db.dao.ChequeDao
import com.aistudio.sublimationerp.data.db.entity.Customer
import com.aistudio.sublimationerp.data.db.entity.Order
import com.aistudio.sublimationerp.data.db.entity.Fabric
import com.aistudio.sublimationerp.data.db.entity.Expense
import com.aistudio.sublimationerp.data.db.entity.Payment
import com.aistudio.sublimationerp.data.db.entity.PaymentType
import com.aistudio.sublimationerp.data.db.entity.Cheque
import kotlinx.coroutines.flow.Flow

class SublimationRepository(
    private val customerDao: CustomerDao,
    private val orderDao: OrderDao,
    private val fabricDao: FabricDao,
    private val expenseDao: ExpenseDao,
    private val paymentDao: PaymentDao,
    private val chequeDao: ChequeDao
) {
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val allFabrics: Flow<List<Fabric>> = fabricDao.getAllFabrics()
    
    val totalCustomerDebt: Flow<Double?> = customerDao.getTotalDebt()
    
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> = expenseDao.getExpensesByDateRange(startDate, endDate)
    fun getOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<Order>> = orderDao.getOrdersByDateRange(startDate, endDate)
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val allCheques: Flow<List<Cheque>> = chequeDao.getAllCheques()
    
    fun getTodayOrdersCount(startOfDay: Long) = orderDao.getTodayOrdersCount(startOfDay)
    fun getTodaySalesAmount(startOfDay: Long) = orderDao.getTodaySalesAmount(startOfDay)
    val readyOrdersCount: Flow<Int> = orderDao.getReadyOrdersCount()
    val lowStockFabricsCount: Flow<Int> = fabricDao.getLowStockFabricsCount()

    suspend fun insertCustomer(customer: Customer) = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    suspend fun insertOrder(order: Order) {
        orderDao.insertOrder(order)
        
        // Update customer balance (add remaining amount as debt)
        if (order.remainingAmount > 0) {
            val customer = customerDao.getCustomerById(order.customerId)
            if (customer != null) {
                customerDao.updateCustomer(customer.copy(balance = customer.balance + order.remainingAmount))
            }
        }
        
        // Deduct fabric stock
        if (order.fabricId != null && order.length != null) {
            val consumedLength = order.length * order.quantity
            deductFabricStock(order.fabricId, consumedLength)
        }
    }
    
    suspend fun deductFabricStock(fabricId: Long, amount: Double) {
         val fabric = fabricDao.getFabricById(fabricId)
         if (fabric != null) {
             val newStock = (fabric.stock - amount).coerceAtLeast(0.0) // prevent negative
             fabricDao.updateFabric(fabric.copy(stock = newStock))
         }
    }
    
    suspend fun restoreFabricStock(fabricId: Long, amount: Double) {
         val fabric = fabricDao.getFabricById(fabricId)
         if (fabric != null) {
             val newStock = fabric.stock + amount
             fabricDao.updateFabric(fabric.copy(stock = newStock))
         }
    }

    suspend fun updateOrder(order: Order) {
        val oldOrder = orderDao.getOrderById(order.id)
        if (oldOrder != null) {
            // Reverse old order
            if (oldOrder.remainingAmount > 0) {
                val customer = customerDao.getCustomerById(oldOrder.customerId)
                if (customer != null) {
                    customerDao.updateCustomer(customer.copy(balance = customer.balance - oldOrder.remainingAmount))
                }
            }
            if (oldOrder.fabricId != null && oldOrder.length != null) {
                val qty = (if(oldOrder.quantity > 0) oldOrder.quantity else 1)
                restoreFabricStock(oldOrder.fabricId, oldOrder.length * qty) // reverse deduction
            }
        }
        
        // Apply new order
        orderDao.updateOrder(order)
        if (order.remainingAmount > 0) {
            val customer = customerDao.getCustomerById(order.customerId)
            if (customer != null) {
                customerDao.updateCustomer(customer.copy(balance = customer.balance + order.remainingAmount))
            }
        }
        if (order.fabricId != null && order.length != null) {
            val qty = (if(order.quantity > 0) order.quantity else 1)
            deductFabricStock(order.fabricId, order.length * qty)
        }
    }

    suspend fun deleteOrder(order: Order) {
        // Reverse order
        if (order.remainingAmount > 0) {
            val customer = customerDao.getCustomerById(order.customerId)
            if (customer != null) {
                customerDao.updateCustomer(customer.copy(balance = customer.balance - order.remainingAmount))
            }
        }
        if (order.fabricId != null && order.length != null) {
            val qty = (if(order.quantity > 0) order.quantity else 1)
            restoreFabricStock(order.fabricId, order.length * qty)
        }
        orderDao.deleteOrder(order)
    }
    
    suspend fun insertFabric(fabric: Fabric) = fabricDao.insertFabric(fabric)
    suspend fun updateFabric(fabric: Fabric) = fabricDao.updateFabric(fabric)
    
    // Expenses
    suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
    
    // Payments
    suspend fun insertPayment(payment: Payment) {
        paymentDao.insertPayment(payment)
        // Update Customer balance
        val customer = customerDao.getCustomerById(payment.customerId)
        if (customer != null) {
            // DEBT increases balance (they owe us more), CREDIT decreases balance (they paid us)
            val newBalance = if (payment.type == PaymentType.DEBT) {
                customer.balance + payment.amount
            } else {
                customer.balance - payment.amount
            }
            customerDao.updateCustomer(customer.copy(balance = newBalance))
        }
    }

    suspend fun updatePayment(payment: Payment) {
        val oldPayment = paymentDao.getPaymentById(payment.id)
        if (oldPayment != null) {
            // Reverse old payment
            val customer = customerDao.getCustomerById(oldPayment.customerId)
            if (customer != null) {
                val reversedBalance = if (oldPayment.type == PaymentType.DEBT) {
                    customer.balance - oldPayment.amount
                } else {
                    customer.balance + oldPayment.amount
                }
                customerDao.updateCustomer(customer.copy(balance = reversedBalance))
            }
        }
        
        paymentDao.updatePayment(payment)
        // Apply new payment
        val customer = customerDao.getCustomerById(payment.customerId)
        if (customer != null) {
            val newBalance = if (payment.type == PaymentType.DEBT) {
                customer.balance + payment.amount
            } else {
                customer.balance - payment.amount
            }
            customerDao.updateCustomer(customer.copy(balance = newBalance))
        }
    }

    suspend fun deletePayment(payment: Payment) {
        // Reverse payment
        val customer = customerDao.getCustomerById(payment.customerId)
        if (customer != null) {
            val reversedBalance = if (payment.type == PaymentType.DEBT) {
                customer.balance - payment.amount
            } else {
                customer.balance + payment.amount
            }
            customerDao.updateCustomer(customer.copy(balance = reversedBalance))
        }
        paymentDao.deletePayment(payment)
    }
    
    // Cheques
    suspend fun insertCheque(cheque: Cheque) = chequeDao.insertCheque(cheque)
    suspend fun updateCheque(cheque: Cheque) = chequeDao.updateCheque(cheque)
    suspend fun deleteCheque(cheque: Cheque) = chequeDao.deleteCheque(cheque)
}

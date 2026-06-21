package com.aistudio.sublimationerp.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aistudio.sublimationerp.data.db.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers")
    fun getAllCustomers(): Flow<List<Customer>>
    
    @Query("SELECT SUM(balance) FROM customers WHERE balance > 0")
    fun getTotalDebt(): Flow<Double?>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)
}

@Dao
interface FabricDao {
    @Query("SELECT * FROM fabrics")
    fun getAllFabrics(): Flow<List<Fabric>>

    @Query("SELECT COUNT(*) FROM fabrics WHERE stock < :threshold")
    fun getLowStockFabricsCount(threshold: Double): Flow<Int>

    @Query("SELECT * FROM fabrics WHERE id = :id LIMIT 1")
    suspend fun getFabricById(id: Long): Fabric?

    @Insert
    suspend fun insertFabric(fabric: Fabric): Long

    @Update
    suspend fun updateFabric(fabric: Fabric)

    @Delete
    suspend fun deleteFabric(fabric: Fabric)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE date >= :startDate AND date <= :endDate")
    fun getOrdersByDateRange(startDate: Long, endDate: Long): Flow<List<Order>>

    @Query("SELECT COUNT(*) FROM orders WHERE date >= :startOfDay")
    fun getTodayOrdersCount(startOfDay: Long): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM orders WHERE date >= :startOfDay")
    fun getTodaySalesAmount(startOfDay: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'READY'")
    fun getReadyOrdersCount(): Flow<Int>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): Order?

    @Insert
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): Flow<List<Expense>>
    
    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>

    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    suspend fun getPaymentById(id: Long): Payment?

    @Insert
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)
}

@Dao
interface ChequeDao {
    @Query("SELECT * FROM cheques")
    fun getAllCheques(): Flow<List<Cheque>>

    @Insert
    suspend fun insertCheque(cheque: Cheque): Long

    @Update
    suspend fun updateCheque(cheque: Cheque)

    @Delete
    suspend fun deleteCheque(cheque: Cheque)
}

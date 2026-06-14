package com.aistudio.sublimationerp.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aistudio.sublimationerp.data.db.entity.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY date DESC")
    fun getOrdersForCustomer(customerId: Long): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun getOrdersByDateRange(from: Long, to: Long): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)
    
    @Query("SELECT COUNT(*) FROM orders WHERE date >= :startOfDay")
    fun getTodayOrdersCount(startOfDay: Long): Flow<Int>
    
    @Query("SELECT SUM(totalAmount) FROM orders WHERE date >= :startOfDay")
    fun getTodaySalesAmount(startOfDay: Long): Flow<Double?>
    
    @Query("SELECT COUNT(*) FROM orders WHERE status = 'READY'")
    fun getReadyOrdersCount(): Flow<Int>
}

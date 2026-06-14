package com.aistudio.sublimationerp.data.db.dao

import androidx.room.*
import com.aistudio.sublimationerp.data.db.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY date DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): Payment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long
    
    @Update
    suspend fun updatePayment(payment: Payment)
    
    @Delete
    suspend fun deletePayment(payment: Payment)
}

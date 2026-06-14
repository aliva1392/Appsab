package com.example.data.db.dao

import androidx.room.*
import com.example.data.db.entity.Cheque
import kotlinx.coroutines.flow.Flow

@Dao
interface ChequeDao {
    @Query("SELECT * FROM cheques ORDER BY dueDate ASC")
    fun getAllCheques(): Flow<List<Cheque>>

    @Query("SELECT * FROM cheques WHERE status = 'PENDING' AND dueDate <= :date ORDER BY dueDate ASC")
    fun getPendingChequesDueBefore(date: Long): Flow<List<Cheque>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheque(cheque: Cheque): Long

    @Update
    suspend fun updateCheque(cheque: Cheque)

    @Delete
    suspend fun deleteCheque(cheque: Cheque)
}

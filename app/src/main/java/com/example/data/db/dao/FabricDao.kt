package com.aistudio.sublimationerp.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aistudio.sublimationerp.data.db.entity.Fabric
import kotlinx.coroutines.flow.Flow

@Dao
interface FabricDao {
    @Query("SELECT * FROM fabrics ORDER BY name ASC")
    fun getAllFabrics(): Flow<List<Fabric>>

    @Query("SELECT * FROM fabrics WHERE id = :id")
    suspend fun getFabricById(id: Long): Fabric?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFabric(fabric: Fabric): Long
    
    @Update
    suspend fun updateFabric(fabric: Fabric)
    
    @Delete
    suspend fun deleteFabric(fabric: Fabric)
    
    @Query("SELECT COUNT(*) FROM fabrics WHERE stock < :threshold")
    fun getLowStockFabricsCount(threshold: Double): Flow<Int>
}

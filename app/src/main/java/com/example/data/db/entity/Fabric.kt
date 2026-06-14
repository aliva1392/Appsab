package com.aistudio.sublimationerp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fabrics")
data class Fabric(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val purchasePrice: Double,
    val stock: Double // In meters or rolls
)

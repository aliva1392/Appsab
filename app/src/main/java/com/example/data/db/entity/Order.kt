package com.aistudio.sublimationerp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val date: Long = System.currentTimeMillis(),
    val type: OrderType,
    val fabricId: Long?,
    val width: Double?,
    val length: Double?,
    val quantity: Int,
    val unitPrice: Double,
    val totalAmount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val status: OrderStatus,
    val notes: String
)

enum class OrderType(val displayName: String) {
    FLAG("پرچم"),
    BANNER("کتیبه"),
    ROLL("پارچه متری"),
    CUSTOM("چاپ سفارشی"),
    OTHER("سایر")
}

enum class OrderStatus(val displayName: String) {
    REGISTERED("ثبت شده"),
    PRINTING("در حال چاپ"),
    READY("آماده تحویل"),
    DELIVERED("تحویل شده"),
    CANCELLED("لغو شده")
}

package com.aistudio.sublimationerp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cheques")
data class Cheque(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val number: String,
    val bank: String,
    val dueDate: Long,
    val status: ChequeStatus = ChequeStatus.PENDING
)

enum class ChequeStatus(val displayName: String) {
    PENDING("در انتظار"),
    CLEARED("پاس شده"),
    BOUNCED("برگشتی")
}

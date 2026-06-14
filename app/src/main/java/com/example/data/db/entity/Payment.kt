package com.aistudio.sublimationerp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val method: PaymentMethod,
    val type: PaymentType // DEBT (they bought something, increasing debt), CREDIT (they paid us, decreasing debt)
)

enum class PaymentMethod(val displayName: String) {
    CASH("نقدی"),
    CARD_TO_CARD("کارت‌به‌کارت"),
    POS("کارتخوان"),
    CHEQUE("چک")
}

enum class PaymentType {
    DEBT, CREDIT
}

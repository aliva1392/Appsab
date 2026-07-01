package com.aistudio.sublimationerp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String,
    val balance: Double
)

@Entity(tableName = "fabrics")
data class Fabric(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val purchasePrice: Double,
    val stock: Double
)

enum class OrderType(val displayName: String) { FLAG("پرچم"), BANNER("بنر"), SHIRT("تی‌شرت"), MUG("لیوان"), OTHER("سایر"), CUSTOM("دلخواه") }
enum class OrderStatus(val displayName: String) { REGISTERED("ثبت شده"), IN_PROGRESS("در حال انجام"), READY("آماده تحویل"), DELIVERED("تحویل داده شده"), CANCELLED("لغو شده") }

enum class ExpenseType(val displayName: String) { DIRECT("هزینه مستقیم (کسر از سود)"), INDIRECT_CAPITAL("هزینه غیرمستقیم / سرمایه اولیه") }

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val fabricId: Long?,
    val type: OrderType,
    val length: Double?,
    val width: Double?,
    val quantity: Int,
    val unitPrice: Double,
    val status: OrderStatus,
    val totalAmount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val notes: String,
    val date: Long
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amount: Double,
    val type: ExpenseType = ExpenseType.DIRECT,
    val date: Long
)

enum class PaymentMethod { CASH, CARD, TRANSFER }
enum class PaymentType { CREDIT, DEBT }

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val method: PaymentMethod,
    val type: PaymentType,
    val date: Long
)

@Entity(tableName = "cheques")
data class Cheque(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val date: Long,
    val isCleared: Boolean = false
)

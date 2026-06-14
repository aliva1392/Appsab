package com.aistudio.sublimationerp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aistudio.sublimationerp.data.db.dao.CustomerDao
import com.aistudio.sublimationerp.data.db.dao.OrderDao
import com.aistudio.sublimationerp.data.db.dao.FabricDao
import com.aistudio.sublimationerp.data.db.entity.Customer
import com.aistudio.sublimationerp.data.db.entity.Order
import com.aistudio.sublimationerp.data.db.entity.Fabric
import com.aistudio.sublimationerp.data.db.entity.Expense
import com.aistudio.sublimationerp.data.db.entity.Payment
import com.aistudio.sublimationerp.data.db.entity.Cheque

@Database(
    entities = [
        Customer::class, 
        Order::class, 
        Fabric::class, 
        Expense::class, 
        Payment::class, 
        Cheque::class
    ], 
    version = 1, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao
    abstract fun fabricDao(): FabricDao
    abstract fun expenseDao(): com.aistudio.sublimationerp.data.db.dao.ExpenseDao
    abstract fun paymentDao(): com.aistudio.sublimationerp.data.db.dao.PaymentDao
    abstract fun chequeDao(): com.aistudio.sublimationerp.data.db.dao.ChequeDao
}

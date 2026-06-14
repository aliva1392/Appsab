package com.example.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.db.dao.CustomerDao
import com.example.data.db.dao.OrderDao
import com.example.data.db.dao.FabricDao
import com.example.data.db.entity.Customer
import com.example.data.db.entity.Order
import com.example.data.db.entity.Fabric
import com.example.data.db.entity.Expense
import com.example.data.db.entity.Payment
import com.example.data.db.entity.Cheque

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
    abstract fun expenseDao(): com.example.data.db.dao.ExpenseDao
    abstract fun paymentDao(): com.example.data.db.dao.PaymentDao
    abstract fun chequeDao(): com.example.data.db.dao.ChequeDao
}

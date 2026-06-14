package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.db.AppDatabase
import com.example.data.repository.SublimationRepository

class SublimationApp : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: SublimationRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "sublimation_db"
        ).setJournalMode(androidx.room.RoomDatabase.JournalMode.TRUNCATE)
         .build()
        
        repository = SublimationRepository(
            database.customerDao(),
            database.orderDao(),
            database.fabricDao(),
            database.expenseDao(),
            database.paymentDao(),
            database.chequeDao()
        )
    }
}

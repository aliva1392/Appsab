package com.aistudio.sublimationerp

import android.app.Application
import com.aistudio.sublimationerp.data.db.AppDatabase
import com.aistudio.sublimationerp.data.repository.SublimationRepository

class SublimationApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { SublimationRepository(
        database.customerDao(),
        database.orderDao(),
        database.fabricDao(),
        database.expenseDao(),
        database.paymentDao(),
        database.chequeDao()
    ) }
}

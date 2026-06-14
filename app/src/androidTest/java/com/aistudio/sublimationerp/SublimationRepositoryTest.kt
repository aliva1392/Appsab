package com.aistudio.sublimationerp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aistudio.sublimationerp.data.db.AppDatabase
import com.aistudio.sublimationerp.data.db.entity.Customer
import com.aistudio.sublimationerp.data.db.entity.Fabric
import com.aistudio.sublimationerp.data.db.entity.Order
import com.aistudio.sublimationerp.data.db.entity.OrderStatus
import com.aistudio.sublimationerp.data.db.entity.Payment
import com.aistudio.sublimationerp.data.db.entity.PaymentMethod
import com.aistudio.sublimationerp.data.db.entity.PaymentType
import com.aistudio.sublimationerp.data.repository.SublimationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class SublimationRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: SublimationRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SublimationRepository(
            db.customerDao(),
            db.orderDao(),
            db.fabricDao(),
            db.expenseDao(),
            db.paymentDao(),
            db.chequeDao()
        )
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertOrder_deductsFabricStock() = runBlocking {
        val fabricId = repository.insertFabric(Fabric(name = "Test Fabric", stock = 100.0, purchasePrice = 10.0))
        val customerId = repository.insertCustomer(Customer(name = "Test Customer", phone = "123", address = "", balance = 0.0))
        
        repository.insertOrder(Order(
            customerId = customerId,
            fabricId = fabricId,
            type = com.aistudio.sublimationerp.data.db.entity.OrderType.FLAG,
            length = 5.0,
            width = 1.0,
            quantity = 2,
            unitPrice = 100.0,
            status = OrderStatus.REGISTERED,
            totalAmount = 1000.0,
            paidAmount = 1000.0,
            remainingAmount = 0.0,
            notes = "",
            date = System.currentTimeMillis()
        ))

        val updatedFabric = db.fabricDao().getFabricById(fabricId)
        // stock should be 100 - (5.0 * 2) = 90
        assertEquals(90.0, updatedFabric?.stock ?: 0.0, 0.0)
    }

    @Test
    fun insertOrder_withDebt_updatesCustomerBalance() = runBlocking {
        val customerId = repository.insertCustomer(Customer(name = "Test Customer", phone = "123", address = "", balance = 0.0))
        
        repository.insertOrder(Order(
            customerId = customerId,
            fabricId = null,
            type = com.aistudio.sublimationerp.data.db.entity.OrderType.FLAG,
            length = null,
            width = null,
            quantity = 1,
            unitPrice = 1000.0,
            status = OrderStatus.REGISTERED,
            totalAmount = 1000.0,
            paidAmount = 700.0, // 300 debt
            remainingAmount = 300.0,
            notes = "",
            date = System.currentTimeMillis()
        ))

        val updatedCustomer = db.customerDao().getCustomerById(customerId)
        assertEquals(300.0, updatedCustomer?.balance ?: 0.0, 0.0)
    }

    @Test
    fun deleteOrder_reversesDebtAndFabric() = runBlocking {
        val fabricId = repository.insertFabric(Fabric(name = "Test Fabric", stock = 100.0, purchasePrice = 10.0))
        val customerId = repository.insertCustomer(Customer(name = "Test Customer", phone = "123", address = "", balance = 500.0))
        
        val order = Order(
            customerId = customerId,
            fabricId = fabricId,
            type = com.aistudio.sublimationerp.data.db.entity.OrderType.FLAG,
            length = 5.0,
            width = 1.0,
            quantity = 2,
            unitPrice = 100.0,
            status = OrderStatus.REGISTERED,
            totalAmount = 1000.0,
            paidAmount = 400.0, // 600 debt
            remainingAmount = 600.0,
            notes = "",
            date = System.currentTimeMillis()
        )
        repository.insertOrder(order) // balance becomes 1100, stock becomes 90

        // In tests we need the inserted order id
        val insertedOrder = repository.allOrders.first().first()
        
        repository.deleteOrder(insertedOrder)
        
        val updatedCustomer = db.customerDao().getCustomerById(customerId)
        val updatedFabric = db.fabricDao().getFabricById(fabricId)
        
        assertEquals(500.0, updatedCustomer?.balance ?: 0.0, 0.0)
        assertEquals(100.0, updatedFabric?.stock ?: 0.0, 0.0)
    }

    @Test
    fun insertPayment_credit_reducesCustomerDebt() = runBlocking {
        val customerId = repository.insertCustomer(Customer(name = "Test Customer", phone = "123", address = "", balance = 500.0))
        
        repository.insertPayment(Payment(
            customerId = customerId,
            amount = 200.0,
            method = PaymentMethod.CASH,
            type = PaymentType.CREDIT,
            date = System.currentTimeMillis()
        ))

        val updatedCustomer = db.customerDao().getCustomerById(customerId)
        assertEquals(300.0, updatedCustomer?.balance ?: 0.0, 0.0)
    }
    
    @Test
    fun insertPayment_debt_increasesCustomerDebt() = runBlocking {
        val customerId = repository.insertCustomer(Customer(name = "Test Customer", phone = "123", address = "", balance = 500.0))
        
        repository.insertPayment(Payment(
            customerId = customerId,
            amount = 200.0,
            method = PaymentMethod.CASH,
            type = PaymentType.DEBT,
            date = System.currentTimeMillis()
        ))

        val updatedCustomer = db.customerDao().getCustomerById(customerId)
        assertEquals(700.0, updatedCustomer?.balance ?: 0.0, 0.0)
    }
}

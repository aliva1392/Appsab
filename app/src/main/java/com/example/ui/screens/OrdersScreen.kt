package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.entity.Order
import com.example.ui.viewmodels.SublimationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(viewModel: SublimationViewModel, onNavigateToAddOrder: () -> Unit) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddOrder) {
                Icon(Icons.Default.Add, contentDescription = "ثبت سفارش جدید")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "لیست سفارشات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (orders.isEmpty()) {
                Text(
                    text = "سفارشی ثبت نشده است.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(orders) { order ->
                        val customer = customers.find { it.id == order.customerId }
                        OrderItem(order = order, customerName = customer?.name ?: "نامشخص", onClick = { /* TODO */ })
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: Order, customerName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "سفارش #${order.id} - ${order.type.displayName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text(text = order.status.displayName, modifier = Modifier.padding(4.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "مشتری: $customerName", style = MaterialTheme.typography.bodyMedium)
            
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            Text(text = "تاریخ: ${dateFormat.format(Date(order.date))}", style = MaterialTheme.typography.bodySmall)
            
            Text(text = "مبلغ کل: ${String.format("%,.0f", order.totalAmount)} تومان", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

package com.aistudio.sublimationerp.ui.screens

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
import com.aistudio.sublimationerp.data.db.entity.Payment
import com.aistudio.sublimationerp.data.db.entity.PaymentType
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(viewModel: SublimationViewModel, onNavigateToAddPayment: () -> Unit) {
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddPayment) {
                Icon(Icons.Default.Add, contentDescription = "ثبت پرداختی/دریافتی")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "تراکنش‌های مالی",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (payments.isEmpty()) {
                Text(
                    text = "تراکنشی ثبت نشده است.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(payments) { payment ->
                         val customerName = customers.find { it.id == payment.customerId }?.name ?: "نامشخص"
                         PaymentItem(payment = payment, customerName = customerName)
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentItem(payment: Payment, customerName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val isDebt = payment.type == PaymentType.DEBT
            val typeTitle = if (isDebt) "افزایش بدهی (خرید/خدمات)" else "دریافت وجه (تسویه/علی‌الحساب)"
            val color = if (isDebt) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            
            Text(text = typeTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "مشتری: $customerName", style = MaterialTheme.typography.bodyMedium)
            
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            Text(text = "تاریخ: ${dateFormat.format(Date(payment.date))}", style = MaterialTheme.typography.bodySmall)
            
            Text(text = "روش: ${payment.method.displayName}", style = MaterialTheme.typography.bodySmall)
            Text(text = "مبلغ: ${String.format("%,.0f", payment.amount)} تومان", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

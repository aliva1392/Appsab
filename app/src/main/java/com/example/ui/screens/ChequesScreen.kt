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
import com.aistudio.sublimationerp.data.db.entity.Cheque
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChequesScreen(viewModel: SublimationViewModel, onNavigateToAddCheque: () -> Unit) {
    val cheques by viewModel.cheques.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddCheque) {
                Icon(Icons.Default.Add, contentDescription = "ثبت چک جدید")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "مدیریت چک‌ها",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (cheques.isEmpty()) {
                Text(
                    text = "چکی ثبت نشده است.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(cheques) { cheque ->
                         val customerName = customers.find { it.id == cheque.customerId }?.name ?: "نامشخص"
                         ChequeItem(cheque = cheque, customerName = customerName)
                    }
                }
            }
        }
    }
}

@Composable
fun ChequeItem(cheque: Cheque, customerName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val color = if (cheque.status.name == "PENDING") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            Text(text = "چک ${cheque.status.displayName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "مشتری: $customerName", style = MaterialTheme.typography.bodyMedium)
            
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            Text(text = "سررسید: ${dateFormat.format(Date(cheque.dueDate))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            
            Text(text = "شماره: ${cheque.number} | بانک: ${cheque.bank}", style = MaterialTheme.typography.bodySmall)
            Text(text = "مبلغ: ${String.format("%,.0f", cheque.amount)} تومان", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

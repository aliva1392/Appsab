package com.aistudio.sublimationerp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aistudio.sublimationerp.data.db.entity.Cheque
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModel

@Composable
fun ChequesScreen(
    navController: NavController,
    viewModel: SublimationViewModel,
    onNavigateToAddCheque: () -> Unit
) {
    val cheques by viewModel.cheques.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddCheque) {
                Icon(Icons.Default.Add, contentDescription = "چک جدید")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                text = "چک‌ها",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cheques) { cheque ->
                    val customer = customers.find { it.id == cheque.customerId }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "مشتری: ${customer?.name ?: "نامشخص"}", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "سررسید: ${getShamsiDate(cheque.date)}", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "مبلغ: ${formatCurrency(cheque.amount)}", color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                Text("پاس شده", style = MaterialTheme.typography.labelSmall)
                                Checkbox(
                                    checked = cheque.isCleared,
                                    onCheckedChange = { checked ->
                                        viewModel.updateCheque(cheque.copy(isCleared = checked))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChequeScreen(
    navController: NavController,
    viewModel: SublimationViewModel
) {
    var amount by remember { mutableStateOf("") }
    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }
    
    val customers by viewModel.customers.collectAsStateWithLifecycle()

    // For date picking we can just use current + 30 days as stub or a simple text input for days offset.
    // To keep it simple, we use an offset of days
    var daysUntilDue by remember { mutableStateOf("30") } 

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ثبت چک جدید") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            ExposedDropdownMenuBox(
            expanded = customerDropdownExpanded,
            onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
        ) {
            val selectedCustomerName = customers.find { it.id == selectedCustomerId }?.name ?: "مشتری را انتخاب کنید"
            OutlinedTextField(
                value = selectedCustomerName,
                onValueChange = {},
                readOnly = true,
                label = { Text("مشتری") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = customerDropdownExpanded,
                onDismissRequest = { customerDropdownExpanded = false }
            ) {
                customers.forEach { customer ->
                    DropdownMenuItem(
                        text = { Text(customer.name) },
                        onClick = {
                            selectedCustomerId = customer.id
                            customerDropdownExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("مبلغ (تومان)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = daysUntilDue,
            onValueChange = { daysUntilDue = it },
            label = { Text("روز تا سررسید (مثلا 30)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                val days = daysUntilDue.toLongOrNull() ?: 0L
                if (selectedCustomerId != null && amt > 0) {
                    viewModel.addCheque(Cheque(
                        customerId = selectedCustomerId!!,
                        amount = amt,
                        date = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000),
                        isCleared = false
                    ))
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ذخیره")
        }
    }
    }
}

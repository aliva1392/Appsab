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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aistudio.sublimationerp.data.db.entity.Payment
import com.aistudio.sublimationerp.data.db.entity.PaymentMethod
import com.aistudio.sublimationerp.data.db.entity.PaymentType
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModel

@Composable
fun PaymentsScreen(
    navController: NavController,
    viewModel: SublimationViewModel,
    onNavigateToAddPayment: () -> Unit
) {
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddPayment) {
                Icon(Icons.Default.Add, contentDescription = "پرداخت جدید")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                text = "پرداختی‌ها / دریافتی‌ها",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(payments) { payment ->
                    val customer = customers.find { it.id == payment.customerId }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "مشتری: ${customer?.name ?: "نامشخص"}", style = MaterialTheme.typography.titleMedium)
                                Text(text = getShamsiDate(payment.date), style = MaterialTheme.typography.bodySmall)
                                Text(text = "روش: ${payment.method.name}", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                val isCredit = payment.type == PaymentType.CREDIT
                                Text(
                                    text = formatCurrency(payment.amount), 
                                    style = MaterialTheme.typography.titleMedium, 
                                    color = if (isCredit) Color(0xFF388E3C) else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (isCredit) "دریافتی" else "پرداختی",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCredit) Color(0xFF388E3C) else MaterialTheme.colorScheme.error
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
fun AddPaymentScreen(
    navController: NavController,
    viewModel: SublimationViewModel
) {
    var amount by remember { mutableStateOf("") }
    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var selectedType by remember { mutableStateOf(PaymentType.CREDIT) }
    
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ثبت پرداختی / دریافتی") },
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

        Spacer(modifier = Modifier.height(16.dp))
        
        Text("نوع تراکنش:", style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            PaymentType.values().forEach { type ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedType == type,
                        onClick = { selectedType = type }
                    )
                    Text(text = if (type == PaymentType.CREDIT) "دریافتی (از مشتری)" else "پرداختی (به مشتری)")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("روش پرداخت:", style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            PaymentMethod.values().forEach { method ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method }
                    )
                    Text(text = when(method) {
                        PaymentMethod.CASH -> "نقد"
                        PaymentMethod.CARD -> "کارت"
                        PaymentMethod.TRANSFER -> "حواله"
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (selectedCustomerId != null && amt > 0) {
                    viewModel.addPayment(Payment(
                        customerId = selectedCustomerId!!,
                        amount = amt,
                        method = selectedMethod,
                        type = selectedType,
                        date = System.currentTimeMillis()
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

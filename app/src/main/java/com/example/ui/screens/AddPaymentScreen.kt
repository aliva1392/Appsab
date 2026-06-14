package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.entity.Customer
import com.example.data.db.entity.Payment
import com.example.data.db.entity.PaymentMethod
import com.example.data.db.entity.PaymentType
import com.example.ui.viewmodels.SublimationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(viewModel: SublimationViewModel, onNavigateBack: () -> Unit) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var selectedType by remember { mutableStateOf(PaymentType.CREDIT) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CARD_TO_CARD) }
    var amount by remember { mutableStateOf("") }

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var methodDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ثبت پرداختی/دریافتی") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Dropdown
            ExposedDropdownMenuBox(
                expanded = customerDropdownExpanded,
                onExpandedChange = { customerDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCustomer?.name ?: "انتخاب مشتری",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = customerDropdownExpanded,
                    onDismissRequest = { customerDropdownExpanded = false }
                ) {
                    customers.forEach { customer ->
                        DropdownMenuItem(
                            text = { Text(customer.name) },
                            onClick = {
                                selectedCustomer = customer
                                customerDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Type Dropdown (CREDIT = received money, DEBT = added debt)
            ExposedDropdownMenuBox(
                expanded = typeDropdownExpanded,
                onExpandedChange = { typeDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = if (selectedType == PaymentType.CREDIT) "دریافت وجه از مشتری (بستانکاری)" else "افزایش بدهی مشتری",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = typeDropdownExpanded,
                    onDismissRequest = { typeDropdownExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("دریافت وجه از مشتری (بستانکاری)") }, onClick = { selectedType = PaymentType.CREDIT; typeDropdownExpanded = false })
                    DropdownMenuItem(text = { Text("افزایش بدهی مشتری") }, onClick = { selectedType = PaymentType.DEBT; typeDropdownExpanded = false })
                }
            }

            // Method Dropdown
            ExposedDropdownMenuBox(
                expanded = methodDropdownExpanded,
                onExpandedChange = { methodDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedMethod.displayName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = methodDropdownExpanded,
                    onDismissRequest = { methodDropdownExpanded = false }
                ) {
                    PaymentMethod.entries.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method.displayName) },
                            onClick = {
                                selectedMethod = method
                                methodDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("مبلغ (تومان)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Button(
                onClick = {
                    if (selectedCustomer != null && amount.isNotBlank()) {
                        viewModel.addPayment(
                            Payment(
                                customerId = selectedCustomer!!.id,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                method = selectedMethod,
                                type = selectedType
                            )
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedCustomer != null && amount.isNotBlank()
            ) {
                Text("ثبت تراکنش")
            }
        }
    }
}

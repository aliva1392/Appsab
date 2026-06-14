package com.aistudio.sublimationerp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(viewModel: SublimationViewModel, customerId: Long? = null, onNavigateBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    val customer = customerId?.let { id -> 
        viewModel.customers.value.find { it.id == id }
    }
    
    LaunchedEffect(customer) {
        if (customer != null) {
            name = customer.name
            phone = customer.phone
            address = customer.address
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (customer != null) "ویرایش مشتری" else "افزودن مشتری جدید") },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("نام مشتری یا شرکت") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("شماره تماس") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("آدرس") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        if (customer != null) {
                            viewModel.updateCustomer(customer.copy(name = name, phone = phone, address = address))
                        } else {
                            viewModel.addCustomer(name, phone, address)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(if (customer != null) "ثبت ویرایش" else "ذخیره")
            }
        }
    }
}

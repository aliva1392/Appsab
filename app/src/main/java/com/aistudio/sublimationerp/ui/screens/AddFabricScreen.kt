package com.aistudio.sublimationerp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFabricScreen(viewModel: SublimationViewModel, fabricId: Long? = null, onNavigateBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    val fabric = fabricId?.let { id -> 
        viewModel.fabrics.value.find { it.id == id }
    }
    
    LaunchedEffect(fabric) {
        if (fabric != null) {
            name = fabric.name
            purchasePrice = fabric.purchasePrice.toString() // Or formatted without decimals
            stock = fabric.stock.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (fabric != null) "ویرایش پارچه" else "افزودن پارچه جدید") },
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
                label = { Text("نوع و نام پارچه") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = purchasePrice,
                onValueChange = { purchasePrice = it },
                label = { Text("قیمت خرید هر متر/طاقه (تومان)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("موجودی اولیه (متر)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Button(
                onClick = {
                    if (name.isNotBlank() && purchasePrice.isNotBlank() && stock.isNotBlank()) {
                        if (fabric != null) {
                            viewModel.updateFabric(fabric.copy(name = name, purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0, stock = stock.toDoubleOrNull() ?: 0.0))
                        } else {
                            viewModel.addFabric(
                                name = name,
                                purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                                stock = stock.toDoubleOrNull() ?: 0.0
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && purchasePrice.isNotBlank() && stock.isNotBlank()
            ) {
                Text(if (fabric != null) "ثبت ویرایش" else "ذخیره پارچه")
            }
        }
    }
}

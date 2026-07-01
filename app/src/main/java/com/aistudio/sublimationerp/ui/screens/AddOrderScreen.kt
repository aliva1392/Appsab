package com.aistudio.sublimationerp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Delete
import com.aistudio.sublimationerp.data.db.entity.Customer
import com.aistudio.sublimationerp.data.db.entity.Fabric
import com.aistudio.sublimationerp.data.db.entity.Order
import com.aistudio.sublimationerp.data.db.entity.OrderStatus
import com.aistudio.sublimationerp.data.db.entity.OrderType
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrderScreen(viewModel: SublimationViewModel, orderId: Long?, onNavigateBack: () -> Unit) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val fabrics by viewModel.fabrics.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val editingOrder = remember(orderId, orders) { orders.find { it.id == orderId } }

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var selectedOrderType by remember { mutableStateOf(OrderType.BANNER) }
    var selectedFabric by remember { mutableStateOf<Fabric?>(null) }

    var width by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var paidAmount by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }
    
    var selectedStatus by remember { mutableStateOf(OrderStatus.REGISTERED) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var fabricDropdownExpanded by remember { mutableStateOf(false) }

    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(editingOrder, customers, fabrics) {
        if (!initialized && editingOrder != null && customers.isNotEmpty()) {
            selectedCustomer = customers.find { it.id == editingOrder.customerId }
            selectedOrderType = editingOrder.type
            selectedFabric = fabrics.find { it.id == editingOrder.fabricId }
            width = editingOrder.width?.toString() ?: ""
            length = editingOrder.length?.toString() ?: ""
            quantity = editingOrder.quantity.toString()
            unitPrice = editingOrder.unitPrice.toLong().toString()
            totalAmount = editingOrder.totalAmount.toLong().toString()
            paidAmount = editingOrder.paidAmount.toLong().toString()
            notes = editingOrder.notes
            selectedStatus = editingOrder.status
            initialized = true
        }
    }

    // Auto calculate
    LaunchedEffect(width, length, quantity, unitPrice) {
        if (!initialized && editingOrder != null) return@LaunchedEffect
        
        // If editing and these fields match the original order, don't auto-calculate (preserves custom total amounts).
        // Once the user changes any of them, auto-calculate resumes.
        if (editingOrder != null && 
            width == (editingOrder.width?.toString() ?: "") &&
            length == (editingOrder.length?.toString() ?: "") &&
            quantity == editingOrder.quantity.toString() &&
            unitPrice == editingOrder.unitPrice.toLong().toString()) {
            return@LaunchedEffect
        }
        
        val w = width.toDoubleOrNull() ?: 1.0
        val l = length.toDoubleOrNull() ?: 1.0
        val q = quantity.toIntOrNull() ?: 1
        val p = unitPrice.toDoubleOrNull() ?: 0.0
        
        if (selectedOrderType in listOf(OrderType.BANNER, OrderType.FLAG, OrderType.CUSTOM)) {
             if(width.isNotBlank() && length.isNotBlank() && unitPrice.isNotBlank()) {
                 val total = (w * l) * p * q
                 totalAmount = total.toLong().toString()
             }
        } else {
             if(unitPrice.isNotBlank()) {
                 val total = p * q
                 totalAmount = total.toLong().toString()
             }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (orderId == null) "ثبت سفارش جدید" else "ویرایش سفارش") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (orderId != null) {
                        IconButton(onClick = {
                            editingOrder?.let { viewModel.deleteOrder(it) }
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف مجدد")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Dropdown
            ExposedDropdownMenuBox(
                expanded = customerDropdownExpanded,
                onExpandedChange = { customerDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCustomer?.name ?: "مشتری معمولی (پیش‌فرض)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("مشتری") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = customerDropdownExpanded,
                    onDismissRequest = { customerDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("مشتری معمولی (پیش‌فرض)") },
                        onClick = {
                            selectedCustomer = null
                            customerDropdownExpanded = false
                        }
                    )
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

            // Order Type Dropdown
            ExposedDropdownMenuBox(
                expanded = typeDropdownExpanded,
                onExpandedChange = { typeDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedOrderType.displayName,
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
                    OrderType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                selectedOrderType = type
                                typeDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            if (orderId != null) {
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedStatus.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("وضعیت سفارش") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        OrderStatus.entries.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.displayName) },
                                onClick = { selectedStatus = s; statusDropdownExpanded = false }
                            )
                        }
                    }
                }
            }

            // Fabric Dropdown
            ExposedDropdownMenuBox(
                expanded = fabricDropdownExpanded,
                onExpandedChange = { fabricDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedFabric?.name ?: "انتخاب پارچه (اختیاری)",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fabricDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = fabricDropdownExpanded,
                    onDismissRequest = { fabricDropdownExpanded = false }
                ) {
                    fabrics.forEach { fabric ->
                        DropdownMenuItem(
                            text = { Text(fabric.name) },
                            onClick = {
                                selectedFabric = fabric
                                fabricDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = width,
                    onValueChange = { width = it },
                    label = { Text("عرض (متر)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = length,
                    onValueChange = { length = it },
                    label = { Text("طول (متر)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("تعداد") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    label = { Text("قیمت واحد/متر (تومان)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            OutlinedTextField(
                value = totalAmount,
                onValueChange = { totalAmount = it },
                label = { Text("مبلغ کل (تومان)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = paidAmount,
                onValueChange = { paidAmount = it },
                label = { Text("مبلغ دریافتی (تومان)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("توضیحات (اختیاری)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        val cId = selectedCustomer?.id ?: viewModel.getOrCreateDefaultCustomer()
                        
                        val q = quantity.toIntOrNull() ?: 0
                        if (q <= 0) {
                            snackbarHostState.showSnackbar("تعداد باید بیشتر از صفر باشد")
                            return@launch
                        }

                        val finalTotal = totalAmount.toDoubleOrNull() ?: -1.0
                        if (finalTotal < 0) {
                            snackbarHostState.showSnackbar("مبلغ کل نامعتبر است")
                            return@launch
                        }

                        val finalPaid = paidAmount.toDoubleOrNull() ?: 0.0
                        if (finalPaid < 0) {
                            snackbarHostState.showSnackbar("مبلغ پرداختی نمی‌تواند منفی باشد")
                            return@launch
                        }

                        if (finalPaid > finalTotal) {
                            snackbarHostState.showSnackbar("مبلغ پرداختی بیشتر از مبلغ کل است")
                            return@launch
                        }

                        if (selectedFabric != null) {
                            val l = length.toDoubleOrNull() ?: 0.0
                            val consumedLength = l * q
                            
                            val effectiveStock = if (orderId != null && editingOrder?.fabricId == selectedFabric?.id) {
                                val oldConsumed = (editingOrder?.length ?: 0.0) * (editingOrder?.quantity?.takeIf { it > 0 } ?: 1)
                                selectedFabric!!.stock + oldConsumed
                            } else {
                                selectedFabric!!.stock
                            }
                            
                            if (consumedLength > effectiveStock) {
                                snackbarHostState.showSnackbar("موجودی پارچه کافی نیست (موجود مؤثر: $effectiveStock متر)")
                                return@launch
                            }
                        }
                        
                        val remaining = finalTotal - finalPaid
                        
                        val newOrder = Order(
                            customerId = cId,
                            type = selectedOrderType,
                            fabricId = selectedFabric?.id,
                            width = width.toDoubleOrNull(),
                            length = length.toDoubleOrNull(),
                            quantity = q,
                            unitPrice = unitPrice.toDoubleOrNull() ?: 0.0,
                            totalAmount = finalTotal,
                            paidAmount = finalPaid,
                            remainingAmount = remaining,
                            status = selectedStatus,
                            notes = notes,
                            date = editingOrder?.date ?: System.currentTimeMillis()
                        )
                        if (orderId == null) {
                            viewModel.addOrder(newOrder)
                        } else {
                            viewModel.updateOrder(newOrder.copy(id = orderId))
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (orderId == null) "ثبت سفارش" else "ویرایش سفارش")
            }
        }
    }
}

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
import com.example.data.db.entity.Fabric
import com.example.ui.viewmodels.SublimationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FabricsScreen(viewModel: SublimationViewModel, onNavigateToAddFabric: () -> Unit) {
    val fabrics by viewModel.fabrics.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddFabric) {
                Icon(Icons.Default.Add, contentDescription = "افزودن پارچه")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "انبار پارچه",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (fabrics.isEmpty()) {
                Text(
                    text = "پارچه‌ای یافت نشد.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(fabrics) { fabric ->
                        FabricItem(fabric = fabric, onClick = { /* TODO */ })
                    }
                }
            }
        }
    }
}

@Composable
fun FabricItem(fabric: Fabric, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = fabric.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "موجودی: ${fabric.stock} متر", style = MaterialTheme.typography.bodyMedium)
            Text(text = "قیمت خرید هر متر: ${String.format("%,.0f", fabric.purchasePrice)} تومان", style = MaterialTheme.typography.bodySmall)
        }
    }
}

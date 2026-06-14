package com.aistudio.sublimationerp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.clickable
import com.aistudio.sublimationerp.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: SublimationViewModel, onNavigate: (String) -> Unit) {
    val todayOrdersCount by viewModel.todayOrdersCount.collectAsStateWithLifecycle()
    val todaySalesAmount by viewModel.todaySalesAmount.collectAsStateWithLifecycle()
    val readyOrdersCount by viewModel.readyOrdersCount.collectAsStateWithLifecycle()
    val lowStockFabricsCount by viewModel.lowStockFabricsCount.collectAsStateWithLifecycle()
    val totalCustomerDebt by viewModel.totalCustomerDebt.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "داشبورد مدیریت کارگاه",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                DashboardCard(
                    title = "سفارش‌های امروز",
                    value = todayOrdersCount.toString(),
                    icon = Icons.Default.ShoppingCart,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }
            item {
                DashboardCard(
                    title = "فروش امروز (تومان)",
                    value = String.format("%,.0f", todaySalesAmount ?: 0.0),
                    icon = Icons.Default.MonetizationOn,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }
            item {
                DashboardCard(
                    title = "سفارش‌های آماده",
                    value = readyOrdersCount.toString(),
                    icon = Icons.Default.List,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
            item {
                DashboardCard(
                    title = "هشدار موجودی پارچه",
                    value = lowStockFabricsCount.toString(),
                    icon = Icons.Default.Warning,
                    color = MaterialTheme.colorScheme.errorContainer
                )
            }
            item {
                DashboardCard(
                    title = "بدهی کل مشتریان",
                    value = String.format("%,.0f", totalCustomerDebt ?: 0.0),
                    icon = Icons.Default.MonetizationOn,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "دسترسی سریع",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item { QuickLinkButton("هزینه‌ها", { onNavigate(Route.EXPENSES.name) }) }
            item { QuickLinkButton("پرداخت‌ها", { onNavigate(Route.PAYMENTS.name) }) }
            item { QuickLinkButton("چک‌ها", { onNavigate(Route.CHEQUES.name) }) }
            item { QuickLinkButton("گزارشات", { onNavigate(Route.REPORTS.name) }) }
            item { QuickLinkButton("تنظیمات / بکاپ", { onNavigate(Route.SETTINGS.name) }) }
        }
    }
}

@Composable
fun QuickLinkButton(title: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
    ) {
        Text(title, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun DashboardCard(title: String, value: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

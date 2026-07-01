package com.aistudio.sublimationerp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    onNavigateToCustomers: () -> Unit,
    onNavigateToFabrics: () -> Unit,
    onNavigateToOrders: () -> Unit,
    viewModel: SublimationViewModel
) {
    val todayOrdersCount by viewModel.todayOrdersCount.collectAsStateWithLifecycle()
    val todaySalesAmount by viewModel.todaySalesAmount.collectAsStateWithLifecycle()
    val readyOrdersCount by viewModel.readyOrdersCount.collectAsStateWithLifecycle()
    val lowStockFabricsCount by viewModel.lowStockFabricsCount.collectAsStateWithLifecycle()
    val totalCustomerDebt by viewModel.totalCustomerDebt.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            kotlinx.coroutines.delay(1000)
            isRefreshing = false
        }
    }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true },
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "داشبورد",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
            item {
                StatCard(
                    title = "سفارشات امروز",
                    value = todayOrdersCount.toString(),
                    onClick = onNavigateToOrders
                )
            }
            item {
                StatCard(
                    title = "فروش امروز",
                    value = formatCurrency(todaySalesAmount ?: 0.0),
                    onClick = onNavigateToOrders
                )
            }
            item {
                StatCard(
                    title = "سفارشات آماده",
                    value = readyOrdersCount.toString(),
                    onClick = onNavigateToOrders
                )
            }
            item {
                StatCard(
                    title = "موجودی کم پارچه‌ها",
                    value = lowStockFabricsCount.toString(),
                    onClick = onNavigateToFabrics
                )
            }
            item {
                StatCard(
                    title = "کل بدهی مشتریان",
                    value = formatCurrency(totalCustomerDebt ?: 0.0),
                    onClick = onNavigateToCustomers
                )
            }
        }
    }
    }
}

@Composable
fun StatCard(title: String, value: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getInstance(Locale("en", "US"))
    return formatter.format(amount) + " تومان"
}

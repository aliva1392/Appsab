package com.aistudio.sublimationerp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aistudio.sublimationerp.data.repository.SublimationRepository
import com.aistudio.sublimationerp.ui.screens.*
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModel

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Dashboard : BottomNavItem(Route.DASHBOARD.name, Icons.Default.Dashboard, "داشبورد")
    object Orders : BottomNavItem(Route.ORDERS.name, Icons.Default.ShoppingCart, "سفارشات")
    object Customers : BottomNavItem(Route.CUSTOMERS.name, Icons.Default.People, "مشتریان")
    object Fabrics : BottomNavItem(Route.FABRICS.name, Icons.Default.Inventory, "پارچه‌ها")
    object Reports : BottomNavItem(Route.REPORTS.name, Icons.Default.BarChart, "گزارشات")
    object Settings : BottomNavItem(Route.SETTINGS.name, Icons.Default.Settings, "تنظیمات")
}

@Composable
fun MainScreen(repository: SublimationRepository, viewModel: SublimationViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Orders,
        BottomNavItem.Customers,
        BottomNavItem.Fabrics,
        BottomNavItem.Reports,
        BottomNavItem.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.DASHBOARD.name,
            modifier = Modifier.padding(padding)
        ) {
            composable(Route.DASHBOARD.name) {
                DashboardScreen(
                    navController = navController,
                    onNavigateToCustomers = { navController.navigate(Route.CUSTOMERS.name) },
                    onNavigateToFabrics = { navController.navigate(Route.FABRICS.name) },
                    onNavigateToOrders = { navController.navigate(Route.ORDERS.name) },
                    viewModel = viewModel
                )
            }
            composable(Route.CUSTOMERS.name) {
                CustomersScreen(
                    viewModel = viewModel, 
                    onNavigateToAddCustomer = { navController.navigate(Route.ADD_CUSTOMER.name) }, 
                    onNavigateToEditCustomer = { id -> navController.navigate("${Route.EDIT_CUSTOMER.name}/$id") }
                )
            }
            composable(Route.ADD_CUSTOMER.name) {
                AddCustomerScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
            }
            composable("${Route.EDIT_CUSTOMER.name}/{customerId}") { backStackEntry ->
                val idStr = backStackEntry.arguments?.getString("customerId")
                val id = idStr?.toLongOrNull()
                AddCustomerScreen(viewModel = viewModel, customerId = id, onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.ORDERS.name) {
                OrdersScreen(
                    navController = navController,
                    onNavigateToAddOrder = { navController.navigate(Route.ADD_ORDER.name) },
                    onNavigateToEditOrder = { id -> navController.navigate("${Route.EDIT_ORDER.name}/$id") },
                    viewModel = viewModel
                )
            }
            composable(Route.ADD_ORDER.name) {
                AddOrderScreen(viewModel = viewModel, orderId = null, onNavigateBack = { navController.popBackStack() })
            }
            composable("${Route.EDIT_ORDER.name}/{orderId}") { backStackEntry ->
                val idStr = backStackEntry.arguments?.getString("orderId")
                val id = idStr?.toLongOrNull()
                AddOrderScreen(viewModel = viewModel, orderId = id, onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.FABRICS.name) {
                FabricsScreen(
                    viewModel = viewModel, 
                    onNavigateToAddFabric = { navController.navigate(Route.ADD_FABRIC.name) }, 
                    onNavigateToEditFabric = { id -> navController.navigate("${Route.EDIT_FABRIC.name}/$id") }
                )
            }
            composable(Route.ADD_FABRIC.name) {
                AddFabricScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
            }
            composable("${Route.EDIT_FABRIC.name}/{fabricId}") { backStackEntry ->
                val idStr = backStackEntry.arguments?.getString("fabricId")
                val id = idStr?.toLongOrNull()
                AddFabricScreen(viewModel = viewModel, fabricId = id, onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.EXPENSES.name) {
                ExpensesScreen(navController = navController, viewModel = viewModel, onNavigateToAddExpense = { navController.navigate(Route.ADD_EXPENSE.name) })
            }
            composable(Route.ADD_EXPENSE.name) {
                AddExpenseScreen(navController = navController, viewModel = viewModel)
            }
            composable(Route.PAYMENTS.name) {
                PaymentsScreen(navController = navController, viewModel = viewModel, onNavigateToAddPayment = { navController.navigate(Route.ADD_PAYMENT.name) })
            }
            composable(Route.ADD_PAYMENT.name) {
                AddPaymentScreen(navController = navController, viewModel = viewModel)
            }
            composable(Route.CHEQUES.name) {
                ChequesScreen(navController = navController, viewModel = viewModel, onNavigateToAddCheque = { navController.navigate(Route.ADD_CHEQUE.name) })
            }
            composable(Route.ADD_CHEQUE.name) {
                AddChequeScreen(navController = navController, viewModel = viewModel)
            }
            composable(Route.REPORTS.name) {
                ReportsScreen(navController = navController, viewModel = viewModel)
            }
            composable(Route.SETTINGS.name) {
                SettingsScreen(navController = navController)
            }
        }
    }
}


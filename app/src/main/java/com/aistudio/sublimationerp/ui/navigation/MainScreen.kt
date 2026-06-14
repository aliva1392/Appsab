package com.aistudio.sublimationerp.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aistudio.sublimationerp.ui.screens.*
import com.aistudio.sublimationerp.ui.viewmodels.SublimationViewModel

@Composable
fun MainScreen(viewModel: SublimationViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Route.DASHBOARD.name

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val showBottomBar = currentRoute in listOf(
            Route.DASHBOARD.name,
            Route.CUSTOMERS.name,
            Route.ORDERS.name,
            Route.FABRICS.name
        )

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "داشبورد") },
                            label = { Text("داشبورد") },
                            selected = currentRoute == Route.DASHBOARD.name,
                            onClick = { navController.navigate(Route.DASHBOARD.name) { launchSingleTop = true; restoreState = true } }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "سفارشات") },
                            label = { Text("سفارشات") },
                            selected = currentRoute == Route.ORDERS.name,
                            onClick = { navController.navigate(Route.ORDERS.name) { launchSingleTop = true; restoreState = true } }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.People, contentDescription = "مشتریان") },
                            label = { Text("مشتریان") },
                            selected = currentRoute == Route.CUSTOMERS.name,
                            onClick = { navController.navigate(Route.CUSTOMERS.name) { launchSingleTop = true; restoreState = true } }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Inventory, contentDescription = "انبار") },
                            label = { Text("انبار") },
                            selected = currentRoute == Route.FABRICS.name,
                            onClick = { navController.navigate(Route.FABRICS.name) { launchSingleTop = true; restoreState = true } }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Route.DASHBOARD.name,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Route.DASHBOARD.name) {
                    DashboardScreen(viewModel, onNavigate = { navController.navigate(it) { launchSingleTop = true; restoreState = true } })
                }
                composable(Route.CUSTOMERS.name) {
                    CustomersScreen(
                        viewModel, 
                        onNavigateToAddCustomer = { navController.navigate(Route.ADD_CUSTOMER.name) },
                        onNavigateToEditCustomer = { id -> navController.navigate("${Route.EDIT_CUSTOMER.name}/$id") }
                    )
                }
                composable(Route.ADD_CUSTOMER.name) {
                    AddCustomerScreen(viewModel, null, onNavigateBack = { navController.popBackStack() })
                }
                composable(Route.EDIT_CUSTOMER.name + "/{customerId}") { backStackEntry ->
                    val customerId = backStackEntry.arguments?.getString("customerId")?.toLongOrNull()
                    AddCustomerScreen(viewModel, customerId, onNavigateBack = { navController.popBackStack() })
                }
                composable(Route.ORDERS.name) {
                    OrdersScreen(viewModel, onNavigateToAddOrder = { navController.navigate(Route.ADD_ORDER.name) }, onNavigateToEditOrder = { id -> navController.navigate("${Route.EDIT_ORDER.name}/$id") })
                }
                composable(Route.ADD_ORDER.name) {
                    AddOrderScreen(viewModel, null, onNavigateBack = { navController.popBackStack() })
                }
                composable(Route.EDIT_ORDER.name + "/{orderId}") { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId")?.toLongOrNull()
                    AddOrderScreen(viewModel, orderId, onNavigateBack = { navController.popBackStack() })
                }
                composable(Route.FABRICS.name) {
                    FabricsScreen(
                        viewModel, 
                        onNavigateToAddFabric = { navController.navigate(Route.ADD_FABRIC.name) },
                        onNavigateToEditFabric = { id -> navController.navigate("${Route.EDIT_FABRIC.name}/$id") }
                    )
                }
                composable(Route.ADD_FABRIC.name) {
                    AddFabricScreen(viewModel, null, onNavigateBack = { navController.popBackStack() })
                }
                composable(Route.EDIT_FABRIC.name + "/{fabricId}") { backStackEntry ->
                    val fabricId = backStackEntry.arguments?.getString("fabricId")?.toLongOrNull()
                    AddFabricScreen(viewModel, fabricId, onNavigateBack = { navController.popBackStack() })
                }
                composable(Route.EXPENSES.name) {
                    ExpensesScreen(viewModel, onNavigateToAddExpense = { navController.navigate(Route.ADD_EXPENSE.name) })
                }
                composable(Route.ADD_EXPENSE.name) {
                    AddExpenseScreen(viewModel, onNavigateBack = { navController.popBackStack() })
                }
                composable(Route.PAYMENTS.name) {
                    PaymentsScreen(viewModel, onNavigateToAddPayment = { navController.navigate(Route.ADD_PAYMENT.name) })
                }
                composable(Route.ADD_PAYMENT.name) {
                    AddPaymentScreen(viewModel, onNavigateBack = { navController.popBackStack() })
                }
                composable(Route.CHEQUES.name) {
                    ChequesScreen(viewModel, onNavigateToAddCheque = { navController.navigate(Route.ADD_CHEQUE.name) })
                }
                composable(Route.ADD_CHEQUE.name) {
                    AddChequeScreen(viewModel, onNavigateBack = { navController.popBackStack() })
                }
                composable(Route.REPORTS.name) {
                    ReportsScreen(viewModel)
                }
                composable(Route.SETTINGS.name) {
                    SettingsScreen()
                }
            }
        }
    }
}

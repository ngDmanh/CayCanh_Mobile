package com.example.caycanh_mobile.ui.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.caycanh_mobile.ui.admin.account.AdminAccountScreen
import com.example.caycanh_mobile.ui.admin.dashboard.AdminDashboardScreen
import com.example.caycanh_mobile.ui.admin.placeholder.AdminPlaceholderScreen

sealed class AdminTab(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : AdminTab("admin_dashboard", "Tổng quan", Icons.Default.Dashboard)
    object Plants : AdminTab("admin_plants", "Cây", Icons.Default.LocalFlorist)
    object Orders : AdminTab("admin_orders", "Đơn hàng", Icons.Default.Receipt)
    object Rentals : AdminTab("admin_rentals", "Cho thuê", Icons.Default.EventAvailable)
    object Account : AdminTab("admin_account", "Tài khoản", Icons.Default.Person)
}

private val adminTabs = listOf(
    AdminTab.Dashboard,
    AdminTab.Plants,
    AdminTab.Orders,
    AdminTab.Rentals,
    AdminTab.Account
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScaffold(
    onLogout: () -> Unit,
    onAddPlant: () -> Unit,
    onEditPlant: (plantId: String) -> Unit,
    onOrderClick: (orderId: String) -> Unit,
    onRentalClick: (rentalId: String) -> Unit,
    onNavigateRevenue: () -> Unit,             // ← thêm
    onNavigateCustomers: () -> Unit,           // ← thêm
    onNavigateLowStock: () -> Unit,            // ← thêm
    onNavigateCategories: () -> Unit,
    onNavigateReturns: () -> Unit
) {
    val tabNavController = rememberNavController()
    val backStack by tabNavController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                adminTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            if (currentRoute != tab.route) {
                                tabNavController.navigate(tab.route) {
                                    popUpTo(AdminTab.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = AdminTab.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(AdminTab.Dashboard.route) {
                AdminDashboardScreen(
                    onNavigateReturns = onNavigateReturns
                )
            }
            composable(AdminTab.Plants.route) {
                com.example.caycanh_mobile.ui.admin.plants.AdminPlantsScreen(
                    onAddPlant = onAddPlant,
                    onEditPlant = onEditPlant
                )
            }
            composable(AdminTab.Orders.route) {
                com.example.caycanh_mobile.ui.admin.orders.AdminOrdersScreen(
                    onOrderClick = onOrderClick
                )
            }
            composable(AdminTab.Rentals.route) {
                com.example.caycanh_mobile.ui.admin.rentals.AdminRentalsScreen(
                    onRentalClick = onRentalClick
                )
            }
            composable(AdminTab.Account.route) {
                com.example.caycanh_mobile.ui.admin.account.AdminAccountScreen(
                    onLogout = onLogout,
                    onNavigateRevenue = onNavigateRevenue,
                    onNavigateCustomers = onNavigateCustomers,
                    onNavigateLowStock = onNavigateLowStock,
                    onNavigateCategories = onNavigateCategories,
                    onNavigateReturns = onNavigateReturns
                )
            }
        }
    }
}
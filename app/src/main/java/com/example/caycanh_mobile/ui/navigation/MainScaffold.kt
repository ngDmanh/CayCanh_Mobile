package com.example.caycanh_mobile.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.caycanh_mobile.ui.customer.home.CustomerHomeScreen
import com.example.caycanh_mobile.ui.customer.cart.CartScreen
import com.example.caycanh_mobile.ui.customer.orders.OrdersScreen
import com.example.caycanh_mobile.ui.customer.categories.CategoriesPlaceholderScreen
import com.example.caycanh_mobile.ui.customer.profile.ProfileScreen

/**
 * 5 tab chính của customer app.
 */
sealed class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : BottomTab("tab_home", "Trang chủ", Icons.Default.Home)
    object Categories : BottomTab("tab_categories", "Danh mục", Icons.Default.GridView)
    object Cart : BottomTab("tab_cart", "Giỏ hàng", Icons.Default.ShoppingCart)
    object Orders : BottomTab("tab_orders", "Đơn hàng", Icons.Default.Inventory2)
    object Profile : BottomTab("tab_profile", "Tài khoản", Icons.Default.Person)
}

private val tabs = listOf(
    BottomTab.Home,
    BottomTab.Categories,
    BottomTab.Cart,
    BottomTab.Orders,
    BottomTab.Profile
)

/**
 * Scaffold chính cho customer — chứa Bottom Nav + nội dung 5 tab.
 *
 * onPlantClick và onLogout được pass từ AppNavGraph xuống đây
 * để khi navigate sang Plant Detail hoặc Login, dùng outer NavController.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMainScaffold(
    onPlantClick: (plantId: String) -> Unit,
    onOrderClick: (orderId: String) -> Unit,
    onRentalClick: (rentalId: String) -> Unit,
    onNavigateCheckout: () -> Unit,
    onProfileEditClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onMyReviewsClick: () -> Unit,
    onLogout: () -> Unit
) {
    // NavController nội bộ cho 5 tab
    val tabNavController = rememberNavController()
    val backStack by tabNavController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != tab.route) {
                                tabNavController.navigate(tab.route) {
                                    // Pop về tab Home để khi back ra app sẽ thoát
                                    popUpTo(BottomTab.Home.route) {
                                        saveState = true
                                    }
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
            startDestination = BottomTab.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomTab.Home.route) {
                CustomerHomeScreen(
                    onPlantClick = onPlantClick,
                    onNotificationClick = onNotificationClick,
                    onLogout = onLogout
                )
            }
            composable(BottomTab.Categories.route) {
                CategoriesPlaceholderScreen(onPlantClick = onPlantClick)
            }
            composable(BottomTab.Cart.route) {
                CartScreen(
                    onCheckout = onNavigateCheckout,
                    onBrowsePlants = {
                        // Chuyển sang tab Home
                        tabNavController.navigate(BottomTab.Home.route) {
                            popUpTo(BottomTab.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomTab.Orders.route) {
                OrdersScreen(
                    onOrderClick = onOrderClick,
                    onRentalClick = onRentalClick
                )
            }
            composable(BottomTab.Profile.route) {
                ProfileScreen(
                    onLogout = onLogout,
                    onEditClick = onProfileEditClick,
                    onMyReviewsClick = onMyReviewsClick
                )
            }
        }
    }
}
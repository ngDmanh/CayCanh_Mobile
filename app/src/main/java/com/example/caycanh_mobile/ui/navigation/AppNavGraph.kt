package com.example.caycanh_mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.caycanh_mobile.ui.auth.SplashScreen
import com.example.caycanh_mobile.ui.auth.login.LoginScreen
import com.example.caycanh_mobile.ui.auth.register.RegisterScreen
import com.example.caycanh_mobile.ui.auth.verifyemail.VerifyEmailScreen
import com.example.caycanh_mobile.ui.customer.checkout.CheckoutScreen
import com.example.caycanh_mobile.ui.customer.checkout.OrderSuccessScreen
import com.example.caycanh_mobile.ui.customer.home.AdminHomeScreen


import com.example.caycanh_mobile.ui.customer.plantdetail.PlantDetailScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.Splash.route) {

        composable(Routes.Splash.route) {
            SplashScreen(
                onCheckDone = { loggedIn, role ->
                    val target = when {
                        !loggedIn -> Routes.Login.route
                        role == "admin" -> Routes.AdminHome.route
                        else -> Routes.CustomerHome.route
                    }
                    navController.navigate(target) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val target = if (role == "admin") Routes.AdminHome.route
                    else Routes.CustomerHome.route
                    navController.navigate(target) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onNavigateRegister = { navController.navigate(Routes.Register.route) },
                onNavigateForgotPassword = { /* TODO sau */ }
            )
        }

        composable(Routes.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { email ->
                    navController.navigate(Routes.VerifyEmail.create(email))
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateLogin = {
                    navController.popBackStack(Routes.Login.route, inclusive = false)
                }
            )
        }

        composable(
            route = Routes.VerifyEmail.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyEmailScreen(
                email = email,
                onVerifySuccess = { role ->
                    val target = if (role == "admin") Routes.AdminHome.route
                    else Routes.CustomerHome.route
                    navController.navigate(target) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CustomerHome.route) {
            CustomerMainScaffold(
                onPlantClick = { plantId ->
                    navController.navigate(Routes.PlantDetail.create(plantId))
                },
                onNavigateCheckout = {
                    navController.navigate(Routes.Checkout.route)
                },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.AdminHome.route) {
            AdminHomeScreen(
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.PlantDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            PlantDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddToCartSuccess  = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.Checkout.route) {
            CheckoutScreen(
                onNavigateBack = { navController.popBackStack() },
                onCheckoutSuccess = {
                    navController.navigate(Routes.OrderSuccess.route) {
                        popUpTo(Routes.CustomerHome.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.OrderSuccess.route) {
            OrderSuccessScreen(
                onViewOrders = {
                    // Quay về Home, vào tab Orders sẽ làm sau ở Nhóm D
                    navController.popBackStack(Routes.CustomerHome.route, inclusive = false)
                },
                onContinueShopping = {
                    navController.popBackStack(Routes.CustomerHome.route, inclusive = false)
                }
            )
        }
    }
}
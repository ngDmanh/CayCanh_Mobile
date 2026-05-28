package com.example.caycanh_mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.caycanh_mobile.ui.auth.SplashScreen
import com.example.caycanh_mobile.ui.auth.forgot.ForgotPasswordScreen
import com.example.caycanh_mobile.ui.auth.forgot.ResetPasswordScreen
import com.example.caycanh_mobile.ui.auth.forgot.VerifyResetOtpScreen
import com.example.caycanh_mobile.ui.auth.login.LoginScreen
import com.example.caycanh_mobile.ui.auth.register.RegisterScreen
import com.example.caycanh_mobile.ui.auth.verifyemail.VerifyEmailScreen
import com.example.caycanh_mobile.ui.customer.checkout.CheckoutScreen
import com.example.caycanh_mobile.ui.customer.checkout.OrderSuccessScreen
import com.example.caycanh_mobile.ui.customer.home.AdminHomeScreen
import com.example.caycanh_mobile.ui.customer.notifications.NotificationScreen
import com.example.caycanh_mobile.ui.customer.orders.OrderDetailScreen
import com.example.caycanh_mobile.ui.customer.plantdetail.PlantDetailScreen
import com.example.caycanh_mobile.ui.customer.profile.ProfileEditScreen
import com.example.caycanh_mobile.ui.customer.review.CreateReviewScreen
import com.example.caycanh_mobile.ui.customer.review.MyReviewsScreen

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
                onNavigateForgotPassword = {
                    navController.navigate(Routes.ForgotPassword.route)
                }
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
                onOrderClick = { orderId ->
                    navController.navigate(Routes.OrderDetail.create(orderId))
                },
                onNavigateCheckout = {
                    navController.navigate(Routes.Checkout.route)
                },
                onProfileEditClick = {
                    navController.navigate(Routes.ProfileEdit.route)
                },
                onNotificationClick = {
                    navController.navigate(Routes.Notifications.route)
                },
                onMyReviewsClick = {
                    navController.navigate(Routes.MyReviews.route)
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
                    navController.popBackStack(Routes.CustomerHome.route, inclusive = false)
                },
                onContinueShopping = {
                    navController.popBackStack(Routes.CustomerHome.route, inclusive = false)
                }
            )
        }
        composable(
            route = Routes.OrderDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            OrderDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onReviewClick = { orderId, plantId, plantName ->      // ← thêm
                    navController.navigate(Routes.CreateReview.create(orderId, plantId, plantName))
                }
            )
        }

        composable(Routes.ProfileEdit.route) {
            ProfileEditScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        composable(Routes.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onSentSuccess = { email ->
                    navController.navigate(Routes.VerifyResetOtp.create(email))
                }
            )
        }

        composable(
            route = Routes.VerifyResetOtp.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) {
            VerifyResetOtpScreen(
                onNavigateBack = { navController.popBackStack() },
                onVerifySuccess = { resetToken, email ->
                    navController.navigate(Routes.ResetPassword.create(resetToken, email)) {
                        // Khi vào màn 3, pop hết các màn forgot trước đó để back về Login luôn
                        popUpTo(Routes.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.ResetPassword.route,
            arguments = listOf(
                navArgument("resetToken") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) {
            ResetPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onResetSuccess = {
                    // Reset xong → về Login, xóa stack forgot
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Login.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.Notifications.route) {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CreateReview.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("plantId") { type = NavType.StringType },
                navArgument("plantName") { type = NavType.StringType }
            )
        ) {
            CreateReviewScreen(
                onNavigateBack = { navController.popBackStack() },
                onReviewSuccess = { navController.popBackStack() }
            )
        }

        composable(Routes.MyReviews.route) {
            MyReviewsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
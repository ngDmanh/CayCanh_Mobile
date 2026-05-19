package com.example.caycanh_mobile.ui.navigation

/**
 * Định nghĩa các route trong app.
 */
sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Login : Routes("login")
    object Register : Routes("register")
    object VerifyEmail : Routes("verify_email/{email}") {
        fun create(email: String) = "verify_email/$email"
    }
    object CustomerHome : Routes("customer_home")
    object AdminHome : Routes("admin_home")
    object PlantDetail : Routes("plant_detail/{id}") {
        fun create(id: String) = "plant_detail/$id"
    }
    object Checkout : Routes("checkout")
    object OrderSuccess : Routes("order_success")
}
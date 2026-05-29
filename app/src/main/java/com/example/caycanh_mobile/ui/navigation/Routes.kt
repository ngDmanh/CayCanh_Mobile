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

    object OrderDetail : Routes("order_detail/{id}") {
        fun create(id: String) = "order_detail/$id"
    }

    object ForgotPassword : Routes("forgot_password")

    object VerifyResetOtp : Routes("verify_reset_otp/{email}") {
        fun create(email: String) = "verify_reset_otp/$email"
    }

    object ResetPassword : Routes("reset_password/{resetToken}/{email}") {
        fun create(resetToken: String, email: String) = "reset_password/$resetToken/$email"
    }

    object ProfileEdit : Routes("profile_edit")

    object Notifications : Routes("notifications")

    object CreateReview : Routes("create_review/{orderId}/{plantId}/{plantName}") {
        fun create(orderId: String, plantId: String, plantName: String): String {
            val encoded = java.net.URLEncoder.encode(plantName, "UTF-8")
            return "create_review/$orderId/$plantId/$encoded"
        }
    }

    object MyReviews : Routes("my_reviews")

    object AdminPlantForm : Routes("admin_plant_form?plantId={plantId}") {
        // plantId rỗng = tạo mới, có giá trị = sửa
        fun create(plantId: String? = null): String =
            if (plantId == null) "admin_plant_form" else "admin_plant_form?plantId=$plantId"
    }

    object AdminOrderDetail : Routes("admin_order_detail/{id}") {
        fun create(id: String) = "admin_order_detail/$id"
    }

    object AdminRentalDetail : Routes("admin_rental_detail/{id}") {
        fun create(id: String) = "admin_rental_detail/$id"
    }

    object CustomerRentalDetail : Routes("rental_detail/{id}") {
        fun create(id: String) = "rental_detail/$id"
    }
    object AdminRevenue : Routes("admin_revenue")
    object AdminCustomers : Routes("admin_customers")
    object AdminLowStock : Routes("admin_low_stock")
    object AdminCategories : Routes("admin_categories")
}
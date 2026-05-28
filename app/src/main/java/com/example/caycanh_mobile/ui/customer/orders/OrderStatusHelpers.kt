package com.example.caycanh_mobile.ui.customer.orders

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.caycanh_mobile.data.remote.dto.order.OrderResponse
import com.example.caycanh_mobile.util.MoneyFormatter

/**
 * Mọi thứ liên quan đến hiển thị status của đơn —
 * label, màu, icon, thông báo cho user, hành động khả dụng.
 */
data class OrderDisplayInfo(
    val statusLabel: String,
    val statusColor: Color,
    val statusIcon: ImageVector,
    val userMessage: String,           // text giải thích cho khách
    val actionRequired: ActionType?,    // hành động khách cần làm
    val actionMessage: String? = null   // chi tiết hành động (vd: "Chuyển X đồng qua Zalo")
)

enum class ActionType {
    Transfer,    // cần chuyển khoản
    Wait,        // chờ admin
    Receive      // sắp nhận hàng
}

fun getOrderDisplayInfo(order: OrderResponse): OrderDisplayInfo {
    return when (order.status) {
        "pending" -> OrderDisplayInfo(
            statusLabel = "Chờ xác nhận",
            statusColor = Color(0xFFFF9800),
            statusIcon = Icons.Default.AccessTime,
            userMessage = "Admin sẽ liên hệ xác nhận đơn trong thời gian sớm nhất.",
            actionRequired = ActionType.Wait
        )

        "awaiting_deposit" -> {
            val deposit = order.totalAmount / 2
            OrderDisplayInfo(
                statusLabel = "Chờ đặt cọc",
                statusColor = Color(0xFFFFC107),
                statusIcon = Icons.Default.AttachMoney,
                userMessage = "Bạn cần hoàn thành đặt cọc trước khi hàng được giao.",
                actionRequired = ActionType.Transfer,
                actionMessage = "Vui lòng chuyển khoản ${MoneyFormatter.format(deposit)} . \nMB bank: 0982699028"
            )
        }

        "awaiting_payment" -> OrderDisplayInfo(
            statusLabel = "Chờ thanh toán",
            statusColor = Color(0xFFFFC107),
            statusIcon = Icons.Default.AttachMoney,
            userMessage = "Đơn thuê cần thanh toán trước khi giao.",
            actionRequired = ActionType.Transfer,
            actionMessage = "Vui lòng chuyển khoản ${MoneyFormatter.format(order.totalAmount)}.\nMB bank: 0982699028"
        )

        "confirmed" -> OrderDisplayInfo(
            statusLabel = "Đã xác nhận",
            statusColor = Color(0xFF2196F3),
            statusIcon = Icons.Default.ThumbUp,
            userMessage = "Đơn đã được xác nhận, đang chuẩn bị giao hàng.",
            actionRequired = ActionType.Wait
        )

        "delivering" -> OrderDisplayInfo(
            statusLabel = "Đang giao",
            statusColor = Color(0xFF9C27B0),
            statusIcon = Icons.Default.LocalShipping,
            userMessage = "Đơn đang trên đường đến bạn. Vui lòng giữ máy.",
            actionRequired = ActionType.Receive
        )

        "completed" -> OrderDisplayInfo(
            statusLabel = "Hoàn thành",
            statusColor = Color(0xFF4CAF50),
            statusIcon = Icons.Default.CheckCircle,
            userMessage = "Đơn đã hoàn thành. Cảm ơn bạn đã mua hàng!",
            actionRequired = null
        )

        "cancelled" -> OrderDisplayInfo(
            statusLabel = "Đã hủy",
            statusColor = Color(0xFF9E9E9E),
            statusIcon = Icons.Default.Cancel,
            userMessage = "Đơn đã bị hủy.",
            actionRequired = null
        )

        "delivery_failed" -> OrderDisplayInfo(
            statusLabel = "Giao thất bại",
            statusColor = Color(0xFFD32F2F),
            statusIcon = Icons.Default.ErrorOutline,
            userMessage = order.failureReason ?: "Đơn giao không thành công.",
            actionRequired = null
        )

        else -> OrderDisplayInfo(
            statusLabel = order.status,
            statusColor = Color.Gray,
            statusIcon = Icons.Default.AccessTime,
            userMessage = "",
            actionRequired = null
        )
    }
}
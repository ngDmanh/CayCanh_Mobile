package com.example.caycanh_mobile.ui.customer.orders

import com.example.caycanh_mobile.data.remote.dto.order.OrderResponse

data class OrderDetailUiState(
    val order: OrderResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Hủy đơn
    val isCancelling: Boolean = false,
    val showCancelDialog: Boolean = false,
    val cancelSuccess: Boolean = false,
    val cancelErrorMessage: String? = null,
    val reviewedPlantIds: Set<String> = emptySet(),
    // orderItemId -> trạng thái yêu cầu trả hàng mới nhất (requested/approved/rejected/completed)
    val returnStatusByItemId: Map<String, String> = emptyMap()
)

/**
 * Các bước trong timeline đơn hàng — dùng để hiển thị dạng stepper.
 */
data class TimelineStep(
    val label: String,
    val sublabel: String? = null,
    val state: StepState
)

enum class StepState {
    Done,      // đã qua
    Active,    // đang ở đây
    Pending,   // sắp tới
    Failed     // thất bại
}

/**
 * Tạo timeline dựa theo status hiện tại của đơn.
 */
fun buildTimeline(order: OrderResponse): List<TimelineStep> {
    val isRental = order.orderType == "rental"
    val needsDeposit = order.status == "awaiting_deposit"
    val needsPayment = order.status == "awaiting_payment"

    return when (order.status) {
        "pending" -> listOf(
            TimelineStep("Đã đặt đơn", sublabel = "Chờ admin xác nhận", state = StepState.Active),
            TimelineStep("Xác nhận đơn", state = StepState.Pending),
            TimelineStep("Đang giao", state = StepState.Pending),
            TimelineStep("Hoàn thành", state = StepState.Pending)
        )

        "awaiting_deposit" -> listOf(
            TimelineStep("Đã đặt đơn", state = StepState.Done),
            TimelineStep(
                "Chờ cọc ",
                sublabel = "Vui lòng chuyển khoản ",
                state = StepState.Active
            ),
            TimelineStep("Xác nhận đơn", state = StepState.Pending),
            TimelineStep("Đang giao", state = StepState.Pending),
            TimelineStep("Hoàn thành", state = StepState.Pending)
        )

        "awaiting_payment" -> listOf(
            TimelineStep("Đã đặt đơn", state = StepState.Done),
            TimelineStep(
                "Chờ thanh toán ",
                sublabel = "Vui lòng chuyển khoản ",
                state = StepState.Active
            ),
            TimelineStep("Xác nhận đơn", state = StepState.Pending),
            TimelineStep("Đang giao", state = StepState.Pending),
            TimelineStep("Hoàn thành", state = StepState.Pending)
        )

        "confirmed" -> {
            val steps = mutableListOf<TimelineStep>()
            steps.add(TimelineStep("Đã đặt đơn", state = StepState.Done))
            if (needsDeposit || isRental) {
                steps.add(TimelineStep(
                    if (isRental) "Đã thanh toán" else "Đã cọc",
                    state = StepState.Done
                ))
            }
            steps.add(TimelineStep("Đã xác nhận", state = StepState.Active))
            steps.add(TimelineStep("Đang giao", state = StepState.Pending))
            steps.add(TimelineStep("Hoàn thành", state = StepState.Pending))
            steps
        }

        "delivering" -> {
            val steps = mutableListOf<TimelineStep>()
            steps.add(TimelineStep("Đã đặt đơn", state = StepState.Done))
            if (isRental || order.totalAmount > 500_000) {
                steps.add(TimelineStep(
                    if (isRental) "Đã thanh toán" else "Đã cọc",
                    state = StepState.Done
                ))
            }
            steps.add(TimelineStep("Đã xác nhận", state = StepState.Done))
            steps.add(TimelineStep("Đang giao", sublabel = "Sắp đến tay bạn", state = StepState.Active))
            steps.add(TimelineStep("Hoàn thành", state = StepState.Pending))
            steps
        }

        "completed" -> {
            val steps = mutableListOf<TimelineStep>()
            steps.add(TimelineStep("Đã đặt đơn", state = StepState.Done))
            if (isRental || order.totalAmount > 500_000) {
                steps.add(TimelineStep("Đã thanh toán", state = StepState.Done))
            }
            steps.add(TimelineStep("Đã xác nhận", state = StepState.Done))
            steps.add(TimelineStep("Đã giao", state = StepState.Done))
            steps.add(TimelineStep("Hoàn thành", state = StepState.Active))
            steps
        }

        "cancelled" -> listOf(
            TimelineStep("Đã đặt đơn", state = StepState.Done),
            TimelineStep("Đã hủy", state = StepState.Failed)
        )

        "delivery_failed" -> listOf(
            TimelineStep("Đã đặt đơn", state = StepState.Done),
            TimelineStep("Đã xác nhận", state = StepState.Done),
            TimelineStep("Giao thất bại", sublabel = order.failureReason, state = StepState.Failed)
        )

        else -> emptyList()
    }
}
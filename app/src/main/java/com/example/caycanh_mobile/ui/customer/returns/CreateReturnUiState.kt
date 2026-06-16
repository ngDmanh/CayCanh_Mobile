package com.example.caycanh_mobile.ui.customer.returns

data class CreateReturnUiState(
    // Tải thông tin cây cần trả (từ order)
    val isLoading: Boolean = false,
    val loadError: String? = null,

    val plantName: String = "",
    val plantImage: String? = null,
    val unitPrice: Long = 0,
    val maxQuantity: Int = 1,

    // Form
    val quantity: Int = 1,
    val reason: String = "",
    val reasonError: String? = null,

    // Gửi yêu cầu
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val isSuccess: Boolean = false
) {
    /** Số tiền hoàn tối đa ước tính = đơn giá × số lượng trả (chỉ để hiển thị) */
    val estimatedRefund: Long get() = unitPrice * quantity
}

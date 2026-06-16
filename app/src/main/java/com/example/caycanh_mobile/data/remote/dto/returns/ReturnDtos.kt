package com.example.caycanh_mobile.data.remote.dto.returns

import kotlinx.serialization.Serializable

/**
 * Request khách gửi yêu cầu trả hàng cho một cây trong đơn đã hoàn thành.
 * Không gửi ảnh qua app — khách gửi ảnh/video bằng chứng qua Zalo của shop.
 */
@Serializable
data class CreateReturnRequest(
    val orderItemId: String,
    val quantity: Int,
    val reason: String
)

/**
 * Một yêu cầu trả hàng đầy đủ — dùng cho cả khách và admin.
 * customer chỉ có giá trị khi admin xem (null khi khách xem yêu cầu của mình).
 */
@Serializable
data class ReturnRequestResponse(
    val id: String,
    val orderId: String,
    val orderItemId: String,
    val plantId: String,
    val plantName: String,
    val plantImage: String? = null,
    val quantity: Int,
    val unitPrice: Long,
    val reason: String,
    val status: String,                 // requested | approved | rejected | completed
    val adminNote: String? = null,
    val refundAmount: Long? = null,
    val restock: Boolean? = null,
    val imageUrls: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String? = null,
    val customer: ReturnCustomerInfo? = null
) {
    val isRequested: Boolean get() = status == "requested"
    val isApproved: Boolean get() = status == "approved"
    val isRejected: Boolean get() = status == "rejected"
    val isCompleted: Boolean get() = status == "completed"
}

@Serializable
data class ReturnCustomerInfo(
    val id: String,
    val fullName: String,
    val email: String? = null,
    val phone: String? = null
)

/** Admin duyệt — chốt số tiền hoàn */
@Serializable
data class ApproveReturnRequest(
    val refundAmount: Long,
    val adminNote: String? = null
)

/** Admin từ chối — bắt buộc nêu lý do */
@Serializable
data class RejectReturnRequest(
    val adminNote: String
)

/** Admin hoàn tất — khai cây có nhập lại kho không */
@Serializable
data class CompleteReturnRequest(
    val restock: Boolean,
    val adminNote: String? = null
)

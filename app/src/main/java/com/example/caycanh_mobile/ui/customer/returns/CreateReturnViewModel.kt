package com.example.caycanh_mobile.ui.customer.returns

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caycanh_mobile.data.repository.OrderRepository
import com.example.caycanh_mobile.data.repository.ReturnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateReturnViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
    private val returnRepository: ReturnRepository
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"]) {
        "Thiếu orderId trong route"
    }
    private val orderItemId: String = checkNotNull(savedStateHandle["orderItemId"]) {
        "Thiếu orderItemId trong route"
    }

    private val _uiState = MutableStateFlow(CreateReturnUiState())
    val uiState: StateFlow<CreateReturnUiState> = _uiState.asStateFlow()

    init {
        loadItem()
    }

    /** Tải lại đơn để lấy chi tiết cây cần trả (tên, giá, số đã mua) */
    fun loadItem() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            orderRepository.getOrderById(orderId)
                .onSuccess { order ->
                    val item = order.items.firstOrNull { it.id == orderItemId }
                    if (item == null) {
                        _uiState.update {
                            it.copy(isLoading = false, loadError = "Không tìm thấy sản phẩm trong đơn")
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                plantName = item.plantName,
                                plantImage = item.primaryImageUrl,
                                unitPrice = item.unitPrice,
                                maxQuantity = item.quantity,
                                quantity = item.quantity   // mặc định trả toàn bộ
                            )
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, loadError = e.message ?: "Không tải được thông tin sản phẩm")
                    }
                }
        }
    }

    fun increaseQuantity() {
        _uiState.update {
            it.copy(quantity = (it.quantity + 1).coerceAtMost(it.maxQuantity))
        }
    }

    fun decreaseQuantity() {
        _uiState.update {
            it.copy(quantity = (it.quantity - 1).coerceAtLeast(1))
        }
    }

    fun onReasonChange(v: String) =
        _uiState.update { it.copy(reason = v, reasonError = null) }

    fun onSubmit() {
        if (!validate()) return

        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }

            returnRepository.createReturn(
                orderItemId = orderItemId,
                quantity = state.quantity,
                reason = state.reason
            )
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, submitError = e.message ?: "Gửi yêu cầu thất bại")
                    }
                }
        }
    }

    fun consumeError() = _uiState.update { it.copy(submitError = null) }

    private fun validate(): Boolean {
        val state = _uiState.value
        if (state.reason.isBlank()) {
            _uiState.update { it.copy(reasonError = "Vui lòng nhập lý do trả hàng") }
            return false
        }
        if (state.reason.trim().length < 10) {
            _uiState.update { it.copy(reasonError = "Lý do tối thiểu 10 ký tự") }
            return false
        }
        return true
    }
}

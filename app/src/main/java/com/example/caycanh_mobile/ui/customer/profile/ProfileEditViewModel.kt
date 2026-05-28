package com.example.caycanh_mobile.ui.customer.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caycanh_mobile.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    init {
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.getMe()
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            email = user.email,
                            initialFullName = user.fullName,
                            initialPhone = user.phone.orEmpty(),
                            fullName = user.fullName,
                            phone = user.phone.orEmpty()
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Không tải được thông tin"
                        )
                    }
                }
        }
    }

    fun onFullNameChange(value: String) {
        if (_uiState.value.isSaving) return
        _uiState.update {
            it.copy(fullName = value, fullNameError = null)
        }
    }

    fun onPhoneChange(value: String) {
        if (_uiState.value.isSaving) return
        val filtered = value.filter { it.isDigit() }.take(11)
        _uiState.update {
            it.copy(phone = filtered, phoneError = null)
        }
    }

    fun onSave() {
        if (_uiState.value.isSaving) return
        val state = _uiState.value

        // Validate client-side
        val fullName = state.fullName.trim()
        if (fullName.isBlank()) {
            _uiState.update { it.copy(fullNameError = "Vui lòng nhập họ tên") }
            return
        }
        if (fullName.length > 100) {
            _uiState.update { it.copy(fullNameError = "Họ tên tối đa 100 ký tự") }
            return
        }

        val phone = state.phone.trim()
        if (phone.isNotEmpty() && !phone.matches(Regex("^0[0-9]{9,10}$"))) {
            _uiState.update {
                it.copy(phoneError = "SĐT không hợp lệ ")
            }
            return
        }

        // Gọi API
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            authRepository.updateMyProfile(
                fullName = fullName,
                phone = phone.ifEmpty { null }
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(isSaving = false, saveSuccess = true)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = e.message ?: "Lỗi cập nhật. Vui lòng thử lại."
                        )
                    }
                }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
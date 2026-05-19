package com.example.caycanh_mobile.ui.auth.verifyemail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caycanh_mobile.data.remote.dto.auth.VerifyEmailRequest
import com.example.caycanh_mobile.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyEmailUiState())
    val uiState: StateFlow<VerifyEmailUiState> = _uiState.asStateFlow()

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onOtpChange(otp: String) {
        // Chỉ cho nhập số, max 6
        val filtered = otp.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(otp = filtered, otpError = null, errorMessage = null) }
    }

    fun onVerifyClick() {
        val state = _uiState.value
        if (state.otp.length != 6) {
            _uiState.update { it.copy(otpError = "Mã OTP gồm 6 chữ số") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.verifyEmail(VerifyEmailRequest(state.email, state.otp))
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isVerifySuccess = true,
                            role = response.user.role
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Xác thực thất bại")
                    }
                }
        }
    }

    fun onResendClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, errorMessage = null, successMessage = null) }
            authRepository.resendOtp(_uiState.value.email)
                .onSuccess {
                    _uiState.update {
                        it.copy(isResending = false, successMessage = "Đã gửi lại mã OTP")
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isResending = false, errorMessage = e.message ?: "Gửi lại thất bại")
                    }
                }
        }
    }

    fun consumeVerifySuccess() = _uiState.update { it.copy(isVerifySuccess = false) }
}
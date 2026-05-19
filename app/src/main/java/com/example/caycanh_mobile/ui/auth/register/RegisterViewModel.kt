package com.example.caycanh_mobile.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caycanh_mobile.data.remote.dto.auth.RegisterRequest
import com.example.caycanh_mobile.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFullNameChange(v: String) =
        _uiState.update { it.copy(fullName = v, fullNameError = null, errorMessage = null) }

    fun onEmailChange(v: String) =
        _uiState.update { it.copy(email = v, emailError = null, errorMessage = null) }

    fun onPhoneChange(v: String) =
        _uiState.update { it.copy(phone = v, phoneError = null, errorMessage = null) }

    fun onPasswordChange(v: String) =
        _uiState.update { it.copy(password = v, passwordError = null, errorMessage = null) }

    fun onConfirmPasswordChange(v: String) =
        _uiState.update {
            it.copy(confirmPassword = v, confirmPasswordError = null, errorMessage = null)
        }

    fun onRegisterClick() {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val state = _uiState.value

            authRepository.register(
                RegisterRequest(
                    fullName = state.fullName.trim(),
                    email = state.email.trim(),
                    phone = state.phone.trim(),
                    password = state.password
                )
            )
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Đăng ký thất bại")
                    }
                }
        }
    }

    fun consumeSuccess() = _uiState.update { it.copy(isRegisterSuccess = false) }

    private fun validate(): Boolean {
        val state = _uiState.value
        var ok = true

        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(fullNameError = "Vui lòng nhập họ tên") }; ok = false
        }
        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Vui lòng nhập email") }; ok = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Email không hợp lệ") }; ok = false
        }
        if (state.phone.isBlank()) {
            _uiState.update { it.copy(phoneError = "Vui lòng nhập số điện thoại") }; ok = false
        } else if (!state.phone.matches(Regex("^0\\d{9,10}$"))) {
            _uiState.update { it.copy(phoneError = "Số điện thoại không hợp lệ") }
            ok = false
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Mật khẩu tối thiểu 6 ký tự") }; ok = false
        }
        if (state.confirmPassword != state.password) {
            _uiState.update { it.copy(confirmPasswordError = "Mật khẩu không trùng khớp") }
            ok = false
        }
        return ok
    }
}
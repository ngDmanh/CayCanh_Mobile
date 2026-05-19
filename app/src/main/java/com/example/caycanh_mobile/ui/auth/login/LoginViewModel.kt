package com.example.caycanh_mobile.ui.auth.login

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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    fun onLoginClick() {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.login(_uiState.value.email.trim(), _uiState.value.password)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccess = true,
                            role = response.user.role
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Đăng nhập thất bại"
                        )
                    }
                }
        }
    }

    /** Reset flag để Screen không navigate 2 lần */
    fun consumeLoginSuccess() {
        _uiState.update { it.copy(isLoginSuccess = false) }
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        var ok = true

        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Vui lòng nhập email") }
            ok = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Email không hợp lệ") }
            ok = false
        }

        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Vui lòng nhập mật khẩu") }
            ok = false
        } else if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Mật khẩu tối thiểu 6 ký tự") }
            ok = false
        }

        return ok
    }
}
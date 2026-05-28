package com.example.caycanh_mobile.ui.auth.forgot

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
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

data class VerifyResetOtpUiState(
    val email: String = "",
    val otp: String = "",
    val otpError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val resetToken: String? = null,
    val isResending: Boolean = false,
    val resendMessage: String? = null
)

@HiltViewModel
class VerifyResetOtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val emailArg: String = checkNotNull(savedStateHandle["email"]) { "Thiếu email" }

    private val _uiState = MutableStateFlow(VerifyResetOtpUiState(email = emailArg))
    val uiState: StateFlow<VerifyResetOtpUiState> = _uiState.asStateFlow()

    fun onOtpChange(v: String) {
        if (_uiState.value.isLoading) return
        val filtered = v.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(otp = filtered, otpError = null) }
    }

    fun onVerify() {
        if (_uiState.value.isLoading) return

        val otp = _uiState.value.otp
        if (otp.length != 6) {
            _uiState.update { it.copy(otpError = "Mã OTP phải đủ 6 chữ số") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.verifyResetOtp(_uiState.value.email, otp)
                .onSuccess { resp ->
                    _uiState.update { it.copy(isLoading = false, resetToken = resp.resetToken) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "OTP không hợp lệ")
                    }
                }
        }
    }

    /** Gửi lại OTP — gọi lại endpoint forgot-password */
    fun onResend() {
        if (_uiState.value.isResending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true) }
            authRepository.forgotPassword(_uiState.value.email)
                .onSuccess {
                    _uiState.update {
                        it.copy(isResending = false, resendMessage = "Đã gửi lại OTP")
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isResending = false, errorMessage = e.message)
                    }
                }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeResendMessage() {
        _uiState.update { it.copy(resendMessage = null) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyResetOtpScreen(
    onNavigateBack: () -> Unit,
    onVerifySuccess: (resetToken: String, email: String) -> Unit,
    viewModel: VerifyResetOtpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.resetToken) {
        uiState.resetToken?.let { token ->
            onVerifySuccess(token, uiState.email)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    LaunchedEffect(uiState.resendMessage) {
        uiState.resendMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeResendMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác thực OTP") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !uiState.isLoading) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Text(
                "Nhập mã OTP",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Mã OTP 6 chữ số đã được gửi đến:\n${uiState.email}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = uiState.otp,
                onValueChange = viewModel::onOtpChange,
                label = { Text("Mã OTP") },
                leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                singleLine = true,
                enabled = !uiState.isLoading,
                readOnly = uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = uiState.otpError != null,
                supportingText = uiState.otpError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::onVerify,
                enabled = !uiState.isLoading && uiState.otp.length == 6,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Đang xác thực...", fontWeight = FontWeight.Medium)
                } else {
                    Text("Xác thực", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = viewModel::onResend,
                enabled = !uiState.isResending && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isResending) "Đang gửi lại..." else "Gửi lại OTP")
            }
        }
    }
}
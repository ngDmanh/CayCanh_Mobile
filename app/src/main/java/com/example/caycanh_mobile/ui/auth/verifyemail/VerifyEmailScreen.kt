package com.example.caycanh_mobile.ui.auth.verifyemail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caycanh_mobile.ui.components.PrimaryButton
import com.example.caycanh_mobile.ui.components.PrimaryTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    email: String,
    onVerifySuccess: (role: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: VerifyEmailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.setEmail(email) }

    LaunchedEffect(uiState.isVerifySuccess) {
        if (uiState.isVerifySuccess && uiState.role != null) {
            onVerifySuccess(uiState.role!!)
            viewModel.consumeVerifySuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác thực email") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "📧 Kiểm tra hộp thư",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Mã OTP đã được gửi đến",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                uiState.email,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(40.dp))

            PrimaryTextField(
                value = uiState.otp,
                onValueChange = viewModel::onOtpChange,
                label = "Mã OTP (6 chữ số)",
                leadingIcon = Icons.Default.Pin,
                keyboardType = KeyboardType.Number,
                errorMessage = uiState.otpError,
                enabled = !uiState.isLoading
            )

            Spacer(Modifier.height(16.dp))

            uiState.errorMessage?.let { msg ->
                Text(
                    msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }
            uiState.successMessage?.let { msg ->
                Text(
                    msg,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }

            PrimaryButton(
                text = "Xác thực",
                onClick = viewModel::onVerifyClick,
                loading = uiState.isLoading
            )

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Không nhận được mã? ", style = MaterialTheme.typography.bodyMedium)
                TextButton(
                    onClick = viewModel::onResendClick,
                    enabled = !uiState.isResending && !uiState.isLoading
                ) {
                    Text(if (uiState.isResending) "Đang gửi..." else "Gửi lại")
                }
            }
        }
    }
}
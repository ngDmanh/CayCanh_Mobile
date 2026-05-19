package com.example.caycanh_mobile.ui.auth.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@androidx.compose.runtime.Composable
fun LoginScreen(
    onLoginSuccess: (role: String) -> Unit,
    onNavigateRegister: () -> Unit,
    onNavigateForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Khi login thành công → trigger callback navigate, sau đó reset flag
    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess && uiState.role != null) {
            onLoginSuccess(uiState.role!!)
            viewModel.consumeLoginSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "🌱 Cây Cảnh",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Đăng nhập để tiếp tục",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email
            PrimaryTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                errorMessage = uiState.emailError,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            PrimaryTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Mật khẩu",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                errorMessage = uiState.passwordError,
                enabled = !uiState.isLoading
            )

            // Forgot password
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onNavigateForgotPassword) {
                    Text("Quên mật khẩu?")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error message (nếu có)
            uiState.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // Login button
            PrimaryButton(
                text = "Đăng nhập",
                onClick = viewModel::onLoginClick,
                loading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign up link
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Chưa có tài khoản? ",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onNavigateRegister) {
                    Text("Đăng ký ngay")
                }
            }
        }
    }
}
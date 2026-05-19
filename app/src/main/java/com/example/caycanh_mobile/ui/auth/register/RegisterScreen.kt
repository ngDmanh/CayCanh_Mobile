package com.example.caycanh_mobile.ui.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun RegisterScreen(
    onRegisterSuccess: (email: String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isRegisterSuccess) {
        if (uiState.isRegisterSuccess) {
            onRegisterSuccess(uiState.email.trim())
            viewModel.consumeSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo tài khoản") },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "Họ và tên",
                leadingIcon = Icons.Default.Person,
                errorMessage = uiState.fullNameError,
                enabled = !uiState.isLoading
            )
            Spacer(Modifier.height(12.dp))
            PrimaryTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                errorMessage = uiState.emailError,
                enabled = !uiState.isLoading
            )
            Spacer(Modifier.height(12.dp))
            PrimaryTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "Số điện thoại",
                leadingIcon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone,
                errorMessage = uiState.phoneError,
                enabled = !uiState.isLoading
            )
            Spacer(Modifier.height(12.dp))
            PrimaryTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Mật khẩu",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                errorMessage = uiState.passwordError,
                enabled = !uiState.isLoading
            )
            Spacer(Modifier.height(12.dp))
            PrimaryTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Nhập lại mật khẩu",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                errorMessage = uiState.confirmPasswordError,
                enabled = !uiState.isLoading
            )

            Spacer(Modifier.height(20.dp))

            uiState.errorMessage?.let { msg ->
                Text(
                    msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }

            PrimaryButton(
                text = "Đăng ký",
                onClick = viewModel::onRegisterClick,
                loading = uiState.isLoading
            )

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Đã có tài khoản? ", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNavigateLogin) { Text("Đăng nhập") }
            }
        }
    }
}
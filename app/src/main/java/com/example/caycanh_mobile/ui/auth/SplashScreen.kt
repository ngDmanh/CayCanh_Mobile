package com.example.caycanh_mobile.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caycanh_mobile.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Splash đơn giản — kiểm tra token, tự navigate sang Home nếu có,
 * sang Login nếu chưa có. Hiển thị logo trong 1 giây cho mượt.
 */
@Composable
fun SplashScreen(
    onCheckDone: (loggedIn: Boolean, role: String?) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        delay(800)  // hiện logo 0.8s
        val (loggedIn, role) = viewModel.checkAuth()
        onCheckDone(loggedIn, role)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🌱",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                "Cây Cảnh",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        }
    }
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: com.example.caycanh_mobile.data.repository.AuthRepository
) : ViewModel() {

    /**
     * Verify token thật sự còn hợp lệ bằng cách gọi /api/me.
     * Nếu fail (token hết hạn hoặc user bị xóa) → clear local + bắt login lại.
     */
    suspend fun checkAuth(): Pair<Boolean, String?> {
        val token = tokenManager.getAccessTokenOnce()
        if (token == null) {
            return false to null
        }

        // Có token — gọi /api/me để verify
        val result = authRepository.getMe()
        return result.fold(
            onSuccess = { user -> true to user.role },
            onFailure = {
                // Token expired/invalid → clear và đi Login
                tokenManager.clearAuth()
                false to null
            }
        )
    }
}
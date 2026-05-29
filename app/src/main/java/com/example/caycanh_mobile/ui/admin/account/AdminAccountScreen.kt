package com.example.caycanh_mobile.ui.admin.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caycanh_mobile.data.local.TokenManager
import com.example.caycanh_mobile.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminAccountUiState(
    val userName: String? = null,
    val showLogoutDialog: Boolean = false
)

@HiltViewModel
class AdminAccountViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminAccountUiState())
    val uiState: StateFlow<AdminAccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val name = tokenManager.getUserNameOnce()
            _uiState.update { it.copy(userName = name) }
        }
    }

    fun onLogoutClick() { _uiState.update { it.copy(showLogoutDialog = true) } }
    fun onLogoutDismiss() { _uiState.update { it.copy(showLogoutDialog = false) } }
    fun onLogoutConfirm(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(showLogoutDialog = false) }
            onDone()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAccountScreen(
    onLogout: () -> Unit,
    onNavigateRevenue: () -> Unit,
    onNavigateCustomers: () -> Unit,
    onNavigateLowStock: () -> Unit,
    onNavigateCategories: () -> Unit,
    viewModel: AdminAccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tài khoản") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header avatar + tên
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        uiState.userName?.firstOrNull()?.uppercase() ?: "A",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    uiState.userName ?: "Admin",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    "Quản trị viên",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Section: Báo cáo & thống kê
            SectionLabel("Báo cáo & thống kê")
            MenuItem(
                icon = Icons.Default.TrendingUp,
                title = "Doanh thu",
                subtitle = "Biểu đồ doanh thu theo tháng, top sản phẩm",
                iconColor = Color(0xFF1976D2),
                onClick = onNavigateRevenue
            )
            MenuItem(
                icon = Icons.Default.Inventory,
                title = "Tồn kho thấp",
                subtitle = "Danh sách cây sắp hết hàng",
                iconColor = Color(0xFFE53935),
                onClick = onNavigateLowStock
            )

            Spacer(Modifier.height(16.dp))

            // Section: Quản lý
            SectionLabel("Quản lý hệ thống")
            MenuItem(
                icon = Icons.Default.People,
                title = "Người dùng",
                subtitle = "Danh sách khách hàng đã đăng ký",
                iconColor = Color(0xFF7B1FA2),
                onClick = onNavigateCustomers
            )
            MenuItem(
                icon = Icons.Default.Category,
                title = "Danh mục",
                subtitle = "Quản lý danh mục cây",
                iconColor = Color(0xFF388E3C),
                onClick = onNavigateCategories
            )

            Spacer(Modifier.height(32.dp))

            // Nút Logout
            OutlinedButton(
                onClick = viewModel::onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Đăng xuất", fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onLogoutDismiss,
            title = { Text("Đăng xuất?") },
            text = { Text("Bạn có chắc muốn đăng xuất?") },
            confirmButton = {
                TextButton(onClick = { viewModel.onLogoutConfirm(onLogout) }) {
                    Text("Đăng xuất", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onLogoutDismiss) { Text("Hủy") }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
package com.example.caycanh_mobile.ui.admin.rentals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.caycanh_mobile.data.remote.dto.admin.AdminRentalResponse
import com.example.caycanh_mobile.data.repository.AdminRepository
import com.example.caycanh_mobile.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminRentalDetailUiState(
    val rental: AdminRentalResponse? = null,
    val isLoading: Boolean = false,
    val isActing: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
    val showCollectDialog: Boolean = false,
    val collectCondition: String = ""
)

@HiltViewModel
class AdminRentalDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val rentalId: String = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(AdminRentalDetailUiState(isLoading = true))
    val uiState: StateFlow<AdminRentalDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // Backend không có GET /api/admin/rentals/{id} riêng → lấy list rồi tìm
            adminRepository.getRentals(null)
                .onSuccess { list ->
                    val r = list.firstOrNull { it.id == rentalId }
                    if (r != null) {
                        _uiState.update { it.copy(isLoading = false, rental = r) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Không tìm thấy rental") }
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    fun markDelivered() {
        viewModelScope.launch {
            _uiState.update { it.copy(isActing = true, errorMessage = null) }
            adminRepository.markRentalDelivered(rentalId)
                .onSuccess { r -> _uiState.update {
                    it.copy(isActing = false, rental = r, actionMessage = "Đã đánh dấu giao cây")
                } }
                .onFailure { e -> _uiState.update { it.copy(isActing = false, errorMessage = e.message) } }
        }
    }

    fun openCollect() { _uiState.update { it.copy(showCollectDialog = true, collectCondition = "") } }
    fun dismissCollect() { _uiState.update { it.copy(showCollectDialog = false) } }
    fun onConditionChange(v: String) { _uiState.update { it.copy(collectCondition = v) } }

    fun confirmCollect() {
        val cond = _uiState.value.collectCondition.trim().ifBlank { "Bình thường" }
        _uiState.update { it.copy(showCollectDialog = false) }
        viewModelScope.launch {
            _uiState.update { it.copy(isActing = true, errorMessage = null) }
            adminRepository.markRentalCollected(rentalId, cond)
                .onSuccess { r -> _uiState.update {
                    it.copy(isActing = false, rental = r, actionMessage = "Đã thu hồi cây")
                } }
                .onFailure { e -> _uiState.update { it.copy(isActing = false, errorMessage = e.message) } }
        }
    }

    fun consumeAction() { _uiState.update { it.copy(actionMessage = null) } }
    fun consumeError() { _uiState.update { it.copy(errorMessage = null) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRentalDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminRentalDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let { snackbarHostState.showSnackbar(it); viewModel.consumeAction() }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.consumeError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết rental") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        val r = uiState.rental
        when {
            uiState.isLoading && r == null -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            r == null -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage ?: "Không tải được", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.load() }) { Text("Thử lại") }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Ảnh + tên + trạng thái
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(10.dp))) {
                            if (r.primaryImageUrl != null) {
                                AsyncImage(model = r.primaryImageUrl, contentDescription = null,
                                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center) { Text("🌿") }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(r.plantName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val st = rentalStatusDisplay(r.status)
                            Spacer(Modifier.height(4.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = st.color.copy(alpha = 0.12f)) {
                                Text(st.label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.bodySmall, color = st.color,
                                    fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    InfoCard(title = "Khách hàng") {
                        Text(r.customer?.fullName ?: "—", fontWeight = FontWeight.Medium)
                        r.customer?.phone?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        r.customer?.email?.let { Text(it, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }

                    Spacer(Modifier.height(12.dp))

                    InfoCard(title = "Thông tin thuê") {
                        InfoRow("Thời hạn", "${r.duration} ${durationUnitLabel(r.durationUnit)}")
                        r.startDate?.let { InfoRow("Bắt đầu", it) }
                        r.endDate?.let { InfoRow("Kết thúc", it) }
                        val days = daysToEnd(r.endDate)
                        if (days != null && r.status != "returned") {
                            val text = when {
                                days < 0 -> "Quá hạn ${-days} ngày"
                                days == 0 -> "Hết hạn hôm nay"
                                else -> "Còn $days ngày"
                            }
                            InfoRow("Còn lại", text)
                        }
                        r.actualReturnDate?.let { InfoRow("Ngày trả thực tế", it) }
                        r.conditionOnReturn?.let { InfoRow("Tình trạng khi trả", it) }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Tổng tiền thuê", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(MoneyFormatter.format(r.totalRentalFee),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Hành động theo trạng thái
                    if (uiState.isActing) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        when (r.status) {
                            "pending_delivery" -> {
                                Button(
                                    onClick = viewModel::markDelivered,
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) { Text("Xác nhận đã giao cây", fontWeight = FontWeight.Medium) }
                            }
                            "active", "overdue" -> {
                                val days = daysToEnd(r.endDate)
                                val canCollect = r.status == "overdue" || (days != null && days <= 0)

                                Button(
                                    onClick = viewModel::openCollect,
                                    enabled = canCollect,
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) { Text("Thu hồi cây", fontWeight = FontWeight.Medium) }

                                if (!canCollect && days != null && days > 0) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Còn $days ngày nữa mới được thu hồi",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    // Dialog ghi chú khi thu hồi
    if (uiState.showCollectDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCollect,
            title = { Text("Thu hồi cây") },
            text = {
                Column {
                    Text("Ghi chú tình trạng cây khi trả:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.collectCondition,
                        onValueChange = viewModel::onConditionChange,
                        placeholder = { Text("VD: Cây khỏe, không hư hỏng") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmCollect) { Text("Xác nhận") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissCollect) { Text("Hủy") }
            }
        )
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
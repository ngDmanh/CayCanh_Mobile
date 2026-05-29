package com.example.caycanh_mobile.ui.admin.orders

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caycanh_mobile.data.remote.dto.admin.AdminOrderResponse
import com.example.caycanh_mobile.data.repository.AdminRepository
import com.example.caycanh_mobile.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminOrderDetailUiState(
    val order: AdminOrderResponse? = null,
    val isLoading: Boolean = false,
    val isActing: Boolean = false,           // đang gọi 1 action
    val errorMessage: String? = null,
    val actionMessage: String? = null,       // snackbar thành công
    val showFailDialog: Boolean = false,
    val failReason: String = ""
)

@HiltViewModel
class AdminOrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(AdminOrderDetailUiState())
    val uiState: StateFlow<AdminOrderDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            adminRepository.getOrderDetail(orderId)
                .onSuccess { o -> _uiState.update { it.copy(isLoading = false, order = o) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    private fun runAction(label: String, call: suspend () -> Result<AdminOrderResponse>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActing = true, errorMessage = null) }
            call()
                .onSuccess { o ->
                    _uiState.update { it.copy(isActing = false, order = o, actionMessage = label) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isActing = false, errorMessage = e.message) }
                }
        }
    }

    fun confirm() = runAction("Đã xác nhận đơn") { adminRepository.confirmOrder(orderId) }
    fun confirmDeposit() = runAction("Đã xác nhận thanh toán") { adminRepository.confirmDeposit(orderId) }
    fun startDelivery() = runAction("Bắt đầu giao hàng") { adminRepository.startDelivery(orderId) }
    fun markPaid() = runAction("Đã ghi nhận thanh toán") { adminRepository.markPaid(orderId) }
    fun complete() = runAction("Đơn đã hoàn thành") { adminRepository.completeOrder(orderId) }

    fun openFailDialog() { _uiState.update { it.copy(showFailDialog = true, failReason = "") } }
    fun dismissFailDialog() { _uiState.update { it.copy(showFailDialog = false) } }
    fun onFailReasonChange(v: String) { _uiState.update { it.copy(failReason = v) } }
    fun confirmFail() {
        val reason = _uiState.value.failReason.trim().ifBlank { "Không rõ lý do" }
        _uiState.update { it.copy(showFailDialog = false) }
        runAction("Đã đánh dấu giao thất bại") { adminRepository.markDeliveryFailed(orderId, reason) }
    }

    fun consumeAction() { _uiState.update { it.copy(actionMessage = null) } }
    fun consumeError() { _uiState.update { it.copy(errorMessage = null) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminOrderDetailViewModel = hiltViewModel()
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
                title = { Text("Chi tiết đơn") },
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
        val order = uiState.order
        when {
            uiState.isLoading && order == null -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            order == null -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage ?: "Không tải được đơn", color = MaterialTheme.colorScheme.error)
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
                    // Trạng thái
                    val st = orderStatusDisplay(order.status)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("#${order.id.take(8)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = st.color.copy(alpha = 0.12f)) {
                            Text(st.label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.bodySmall, color = st.color, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Khách hàng
                    InfoCard(title = "Khách hàng") {
                        Text(order.customer?.fullName ?: "—", fontWeight = FontWeight.Medium)
                        order.customer?.phone?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        order.shippingAddress?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!order.note.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text("Ghi chú: ${order.note}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Sản phẩm
                    InfoCard(title = "Sản phẩm (${order.items.size})") {
                        order.items.forEachIndexed { idx, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.plantName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    val meta = item.rental?.let { r ->
                                        val unitLabel = when (r.durationUnit) {
                                            "day" -> "ngày"
                                            "week" -> "tuần"
                                            "month" -> "tháng"
                                            else -> r.durationUnit
                                        }
                                        if (r.startDate != null && r.endDate != null) {
                                            "Thuê ${r.duration} $unitLabel • ${r.startDate} → ${r.endDate}"
                                        } else {
                                            "Thuê ${r.duration} $unitLabel • Chưa giao cây"
                                        }
                                    } ?: "${item.quantity} × ${MoneyFormatter.format(item.unitPrice)}"
                                    Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(MoneyFormatter.format(item.subtotal), fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                            if (idx < order.items.size - 1) HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Thanh toán
                    InfoCard(title = "Thanh toán") {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Tổng tiền")
                            Text(MoneyFormatter.format(order.totalAmount), fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Trạng thái", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(paymentStatusLabel(order.paymentStatus), fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Nút hành động theo trạng thái ──
                    ActionButtons(
                        order = order,
                        isActing = uiState.isActing,
                        onConfirm = viewModel::confirm,
                        onConfirmDeposit = viewModel::confirmDeposit,
                        onStartDelivery = viewModel::startDelivery,
                        onMarkPaid = viewModel::markPaid,
                        onComplete = viewModel::complete,
                        onFail = viewModel::openFailDialog
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    // Dialog lý do giao thất bại
    if (uiState.showFailDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissFailDialog,
            title = { Text("Giao thất bại") },
            text = {
                OutlinedTextField(
                    value = uiState.failReason,
                    onValueChange = viewModel::onFailReasonChange,
                    label = { Text("Lý do") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmFail) {
                    Text("Xác nhận", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissFailDialog) { Text("Hủy") } }
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
private fun ActionButtons(
    order: AdminOrderResponse,
    isActing: Boolean,
    onConfirm: () -> Unit,
    onConfirmDeposit: () -> Unit,
    onStartDelivery: () -> Unit,
    onMarkPaid: () -> Unit,
    onComplete: () -> Unit,
    onFail: () -> Unit
) {
    if (isActing) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Nút chính theo trạng thái
    when (order.status) {
        "pending" -> {
            PrimaryAction("Xác nhận đơn", onConfirm)
        }
        "awaiting_deposit", "awaiting_payment" -> {
            PrimaryAction("Xác nhận đã nhận tiền", onConfirmDeposit)
        }
        "confirmed" -> {
            if (order.orderType == "rental") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Đơn thuê — đã xác nhận thanh toán",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Vào tab \"Cho thuê\" và bấm \"Xác nhận đã giao cây\" để hoàn tất đơn.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                PrimaryAction("Bắt đầu giao hàng", onStartDelivery)
            }
        }
        "delivering" -> {
            // Nếu chưa paid → cho ghi nhận thanh toán trước
            if (order.paymentStatus != "paid") {
                PrimaryAction("Ghi nhận đã thanh toán", onMarkPaid)
                Spacer(Modifier.height(8.dp))
            }
            // Hoàn thành (backend yêu cầu đã paid mới complete được)
            PrimaryAction("Hoàn thành đơn", onComplete, enabled = order.paymentStatus == "paid")
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onFail,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) { Text("Giao thất bại") }
        }
        else -> {
            // completed / cancelled / delivery_failed → không có hành động
            Text(
                "Đơn đã kết thúc, không có thao tác.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PrimaryAction(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) { Text(text, fontWeight = FontWeight.Medium) }
}
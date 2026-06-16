package com.example.caycanh_mobile.ui.admin.returns

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.caycanh_mobile.data.remote.dto.returns.ReturnRequestResponse
import com.example.caycanh_mobile.data.repository.ReturnRepository
import com.example.caycanh_mobile.ui.components.PrimaryButton
import com.example.caycanh_mobile.ui.customer.returns.ReturnStatusBadge
import com.example.caycanh_mobile.ui.customer.returns.formatReturnDate
import com.example.caycanh_mobile.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminReturnDetailUiState(
    val returnRequest: ReturnRequestResponse? = null,
    val isLoading: Boolean = false,
    val isActing: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
    // Dialog duyệt
    val showApproveDialog: Boolean = false,
    val refundInput: String = "",
    val approveNote: String = "",
    // Dialog từ chối
    val showRejectDialog: Boolean = false,
    val rejectReason: String = "",
    // Dialog hoàn tất
    val showCompleteDialog: Boolean = false,
    val restockChoice: Boolean = true,
    val completeNote: String = ""
)

@HiltViewModel
class AdminReturnDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val returnRepository: ReturnRepository
) : ViewModel() {

    private val id: String = checkNotNull(savedStateHandle["id"]) { "Thiếu id yêu cầu" }

    private val _uiState = MutableStateFlow(AdminReturnDetailUiState())
    val uiState: StateFlow<AdminReturnDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            returnRepository.getReturnById(id)
                .onSuccess { rr -> _uiState.update { it.copy(isLoading = false, returnRequest = rr) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    private fun runAction(label: String, call: suspend () -> Result<ReturnRequestResponse>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActing = true, errorMessage = null) }
            call()
                .onSuccess { rr ->
                    _uiState.update { it.copy(isActing = false, returnRequest = rr, actionMessage = label) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isActing = false, errorMessage = e.message) }
                }
        }
    }

    // ── Duyệt ──
    fun openApproveDialog() {
        val rr = _uiState.value.returnRequest
        val suggested = rr?.let { it.unitPrice * it.quantity } ?: 0L
        _uiState.update {
            it.copy(showApproveDialog = true, refundInput = suggested.toString(), approveNote = "")
        }
    }
    fun dismissApprove() { _uiState.update { it.copy(showApproveDialog = false) } }
    fun onRefundChange(v: String) {
        _uiState.update { it.copy(refundInput = v.filter { c -> c.isDigit() }.take(12)) }
    }
    fun onApproveNoteChange(v: String) { _uiState.update { it.copy(approveNote = v) } }
    fun confirmApprove() {
        val amount = _uiState.value.refundInput.toLongOrNull() ?: 0L
        if (amount <= 0L) {
            _uiState.update { it.copy(errorMessage = "Số tiền hoàn không hợp lệ") }
            return
        }
        val note = _uiState.value.approveNote.trim().ifBlank { null }
        _uiState.update { it.copy(showApproveDialog = false) }
        runAction("Đã duyệt yêu cầu") { returnRepository.approveReturn(id, amount, note) }
    }

    // ── Từ chối ──
    fun openRejectDialog() { _uiState.update { it.copy(showRejectDialog = true, rejectReason = "") } }
    fun dismissReject() { _uiState.update { it.copy(showRejectDialog = false) } }
    fun onRejectReasonChange(v: String) { _uiState.update { it.copy(rejectReason = v) } }
    fun confirmReject() {
        val reason = _uiState.value.rejectReason.trim()
        if (reason.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập lý do từ chối") }
            return
        }
        _uiState.update { it.copy(showRejectDialog = false) }
        runAction("Đã từ chối yêu cầu") { returnRepository.rejectReturn(id, reason) }
    }

    // ── Hoàn tất ──
    fun openCompleteDialog() {
        _uiState.update { it.copy(showCompleteDialog = true, restockChoice = true, completeNote = "") }
    }
    fun dismissComplete() { _uiState.update { it.copy(showCompleteDialog = false) } }
    fun onRestockChange(v: Boolean) { _uiState.update { it.copy(restockChoice = v) } }
    fun onCompleteNoteChange(v: String) { _uiState.update { it.copy(completeNote = v) } }
    fun confirmComplete() {
        val restock = _uiState.value.restockChoice
        val note = _uiState.value.completeNote.trim().ifBlank { null }
        _uiState.update { it.copy(showCompleteDialog = false) }
        runAction("Đã hoàn tất hoàn tiền") { returnRepository.completeReturn(id, restock, note) }
    }

    fun consumeAction() { _uiState.update { it.copy(actionMessage = null) } }
    fun consumeError() { _uiState.update { it.copy(errorMessage = null) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReturnDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminReturnDetailViewModel = hiltViewModel()
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
                title = { Text("Xử lý trả hàng") },
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
        val rr = uiState.returnRequest
        when {
            uiState.isLoading && rr == null -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            rr == null -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage ?: "Không tải được yêu cầu", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.load() }) { Text("Thử lại") }
                    }
                }
            }
            else -> {
                AdminReturnDetailContent(
                    rr = rr,
                    isActing = uiState.isActing,
                    onApprove = viewModel::openApproveDialog,
                    onReject = viewModel::openRejectDialog,
                    onComplete = viewModel::openCompleteDialog,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    // ── Dialog duyệt ──
    if (uiState.showApproveDialog) {
        val rr = uiState.returnRequest
        val maxRefund = rr?.let { it.unitPrice * it.quantity } ?: 0L
        AlertDialog(
            onDismissRequest = viewModel::dismissApprove,
            title = { Text("Duyệt & hoàn tiền") },
            text = {
                Column {
                    Text(
                        "Tối đa: ${MoneyFormatter.format(maxRefund)} (đơn giá × số lượng)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.refundInput,
                        onValueChange = viewModel::onRefundChange,
                        label = { Text("Số tiền hoàn (₫)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.approveNote,
                        onValueChange = viewModel::onApproveNoteChange,
                        label = { Text("Ghi chú (tùy chọn)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = { TextButton(onClick = viewModel::confirmApprove) { Text("Duyệt") } },
            dismissButton = { TextButton(onClick = viewModel::dismissApprove) { Text("Hủy") } }
        )
    }

    // ── Dialog từ chối ──
    if (uiState.showRejectDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissReject,
            title = { Text("Từ chối yêu cầu") },
            text = {
                OutlinedTextField(
                    value = uiState.rejectReason,
                    onValueChange = viewModel::onRejectReasonChange,
                    label = { Text("Lý do từ chối") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmReject) {
                    Text("Từ chối", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissReject) { Text("Hủy") } }
        )
    }

    // ── Dialog hoàn tất ──
    if (uiState.showCompleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissComplete,
            title = { Text("Hoàn tất hoàn tiền") },
            text = {
                Column {
                    Text(
                        "Xác nhận đã chuyển khoản hoàn tiền cho khách.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Nhập lại kho", fontWeight = FontWeight.Medium)
                            Text(
                                "Bật nếu cây trả về còn tốt",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = uiState.restockChoice, onCheckedChange = viewModel::onRestockChange)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.completeNote,
                        onValueChange = viewModel::onCompleteNoteChange,
                        label = { Text("Ghi chú (tùy chọn)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = { TextButton(onClick = viewModel::confirmComplete) { Text("Hoàn tất") } },
            dismissButton = { TextButton(onClick = viewModel::dismissComplete) { Text("Hủy") } }
        )
    }
}

@Composable
private fun AdminReturnDetailContent(
    rr: ReturnRequestResponse,
    isActing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ReturnStatusBadge(status = rr.status)
            Spacer(Modifier.width(8.dp))
            Text(
                "Gửi lúc ${formatReturnDate(rr.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(16.dp))

        // Khách hàng
        InfoCard(title = "Khách hàng") {
            Text(rr.customer?.fullName ?: "—", fontWeight = FontWeight.Medium)
            rr.customer?.phone?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            rr.customer?.email?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Sản phẩm trả
        InfoCard(title = "Sản phẩm trả") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))) {
                    if (rr.plantImage != null) {
                        AsyncImage(
                            model = rr.plantImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) { Text("🌿") }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(rr.plantName, fontWeight = FontWeight.Medium)
                    Text(
                        "${rr.quantity} × ${MoneyFormatter.format(rr.unitPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    MoneyFormatter.format(rr.unitPrice * rr.quantity),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Lý do khách
        InfoCard(title = "Lý do khách nêu") {
            Text(rr.reason, style = MaterialTheme.typography.bodyMedium)
        }

        // Thông tin đã xử lý (admin xem đầy đủ)
        if (rr.refundAmount != null || !rr.adminNote.isNullOrBlank() || rr.restock != null) {
            Spacer(Modifier.height(12.dp))
            InfoCard(title = "Đã xử lý") {
                rr.refundAmount?.let {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Số tiền hoàn")
                        Text(MoneyFormatter.format(it), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (rr.isCompleted && rr.restock != null) {
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Nhập lại kho", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (rr.restock) "Có" else "Không", fontWeight = FontWeight.Medium)
                    }
                }
                if (!rr.adminNote.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Ghi chú: ${rr.adminNote}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Hành động theo trạng thái
        if (isActing) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            when (rr.status) {
                "requested" -> {
                    PrimaryButton(text = "Duyệt yêu cầu", onClick = onApprove)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) { Text("Từ chối") }
                }
                "approved" -> {
                    PrimaryButton(text = "Xác nhận đã hoàn tiền", onClick = onComplete)
                }
                else -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            if (rr.isCompleted) "Yêu cầu đã hoàn tất." else "Yêu cầu đã bị từ chối.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
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
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

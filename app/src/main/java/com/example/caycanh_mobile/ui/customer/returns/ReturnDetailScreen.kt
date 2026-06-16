package com.example.caycanh_mobile.ui.customer.returns

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.caycanh_mobile.data.remote.dto.returns.ReturnRequestResponse
import com.example.caycanh_mobile.data.repository.ReturnRepository
import com.example.caycanh_mobile.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SHOP_ZALO = "0982699028"

data class ReturnDetailUiState(
    val returnRequest: ReturnRequestResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReturnDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val returnRepository: ReturnRepository
) : ViewModel() {

    private val id: String = checkNotNull(savedStateHandle["id"]) { "Thiếu id yêu cầu trả hàng" }

    private val _uiState = MutableStateFlow(ReturnDetailUiState())
    val uiState: StateFlow<ReturnDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            returnRepository.getMyReturnById(id)
                .onSuccess { rr ->
                    _uiState.update { it.copy(isLoading = false, returnRequest = rr) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Không tải được yêu cầu")
                    }
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReturnDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết yêu cầu") },
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
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.load() }) { Text("Thử lại") }
                    }
                }
            }

            uiState.returnRequest != null -> {
                ReturnDetailContent(
                    rr = uiState.returnRequest!!,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun ReturnDetailContent(rr: ReturnRequestResponse, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Trạng thái + ngày
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

        // Cây cần trả
        SectionTitle("Sản phẩm trả")
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))) {
                    if (rr.plantImage != null) {
                        AsyncImage(
                            model = rr.plantImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) { Text("🌿") }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(rr.plantName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Số lượng trả: ${rr.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Đơn giá: ${MoneyFormatter.format(rr.unitPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Lý do của khách
        SectionTitle("Lý do trả hàng")
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Text(rr.reason, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
        }

        // Xử lý của shop — chỉ hiển thị thông tin khách cần biết.
        // Ẩn "nhập lại kho" và ghi chú nội bộ; riêng khi BỊ TỪ CHỐI thì hiện lý do.
        val refund = rr.refundAmount
        val rejectReason = if (rr.isRejected) rr.adminNote else null
        if (refund != null || !rejectReason.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))
            SectionTitle("Xử lý của shop")
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (refund != null) {
                        InfoLine("Số tiền hoàn", MoneyFormatter.format(refund))
                    }
                    if (!rejectReason.isNullOrBlank()) {
                        if (refund != null) Spacer(Modifier.height(6.dp))
                        Text(
                            "Lý do từ chối: $rejectReason",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Nhắc gửi bằng chứng qua Zalo khi còn chờ duyệt
        if (rr.isRequested) {
            Spacer(Modifier.height(16.dp))
            ZaloReminderCard()
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Mã yêu cầu: ${rr.id}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Mã đơn: ${rr.orderId}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ZaloReminderCard() {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        border = BorderStroke(1.dp, Color(0xFFFFB74D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📷 Chưa gửi ảnh/video bằng chứng?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6D4C41)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Gửi ảnh hoặc video cây bị vấn đề qua Zalo $SHOP_ZALO kèm mã yêu cầu " +
                        "để shop duyệt nhanh hơn.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6D4C41)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { uriHandler.openUri("https://zalo.me/$SHOP_ZALO") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6D4C41)),
                border = BorderStroke(1.dp, Color(0xFF6D4C41))
            ) {
                Text("Nhắn Zalo ngay")
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}
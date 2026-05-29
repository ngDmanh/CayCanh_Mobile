package com.example.caycanh_mobile.ui.customer.rentals

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.caycanh_mobile.data.remote.dto.rental.CustomerRentalResponse
import com.example.caycanh_mobile.data.repository.RentalRepository
import com.example.caycanh_mobile.ui.common.daysToEnd
import com.example.caycanh_mobile.ui.common.durationUnitLabel
import com.example.caycanh_mobile.ui.common.rentalStatusDisplay
import com.example.caycanh_mobile.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerRentalDetailUiState(
    val rental: CustomerRentalResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CustomerRentalDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val rentalRepository: RentalRepository
) : ViewModel() {

    private val rentalId: String = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(CustomerRentalDetailUiState(isLoading = true))
    val uiState: StateFlow<CustomerRentalDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            rentalRepository.getMyRentalById(rentalId)
                .onSuccess { r -> _uiState.update { it.copy(isLoading = false, rental = r) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerRentalDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: CustomerRentalDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết thuê") },
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
                    // Header: ảnh + tên + status
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

                    // Highlight: ngày còn lại / quá hạn nổi bật
                    val days = daysToEnd(r.endDate)
                    if (r.status in listOf("active", "overdue") && days != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    days < 0 -> Color(0xFFFFEBEE)        // đỏ nhạt
                                    days <= 3 -> Color(0xFFFFF3E0)       // cam nhạt
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    when {
                                        days < 0 -> "QUÁ HẠN"
                                        days == 0 -> "HẾT HẠN HÔM NAY"
                                        else -> "CÒN LẠI"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        days < 0 -> Color(0xFFE53935)
                                        days <= 3 -> Color(0xFFE65100)
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    when {
                                        days < 0 -> "${-days} ngày"
                                        days == 0 -> ""
                                        else -> "$days ngày"
                                    },
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        days < 0 -> Color(0xFFE53935)
                                        days <= 3 -> Color(0xFFE65100)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    InfoCard(title = "Thông tin thuê") {
                        InfoRow("Thời hạn", "${r.duration} ${durationUnitLabel(r.durationUnit)}")
                        if (r.startDate != null) InfoRow("Bắt đầu", r.startDate)
                        if (r.endDate != null) InfoRow("Kết thúc", r.endDate)
                        if (r.startDate == null && r.status == "pending_delivery") {
                            Text(
                                "Cây đang được chuẩn bị giao. Bạn sẽ nhận thông báo khi cây được giao.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        r.actualReturnDate?.let { InfoRow("Ngày trả thực tế", it) }
                        r.conditionOnReturn?.let { InfoRow("Tình trạng khi trả", it) }
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Tổng tiền thuê", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(MoneyFormatter.format(r.totalRentalFee),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
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
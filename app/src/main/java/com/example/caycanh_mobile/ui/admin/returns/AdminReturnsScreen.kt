package com.example.caycanh_mobile.ui.admin.returns

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.caycanh_mobile.data.remote.dto.returns.ReturnRequestResponse
import com.example.caycanh_mobile.data.repository.ReturnRepository
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

private data class ReturnFilter(val label: String, val value: String?)
private val returnFilters = listOf(
    ReturnFilter("Tất cả", null),
    ReturnFilter("Chờ duyệt", "requested"),
    ReturnFilter("Đã duyệt", "approved"),
    ReturnFilter("Đã hoàn tiền", "completed"),
    ReturnFilter("Bị từ chối", "rejected")
)

data class AdminReturnsUiState(
    val returns: List<ReturnRequestResponse> = emptyList(),
    val selectedFilter: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminReturnsViewModel @Inject constructor(
    private val returnRepository: ReturnRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminReturnsUiState())
    val uiState: StateFlow<AdminReturnsUiState> = _uiState.asStateFlow()

    fun load(status: String? = _uiState.value.selectedFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, selectedFilter = status) }
            returnRepository.getAllReturns(status = status, page = 0, size = 100)
                .onSuccess { page ->
                    _uiState.update { it.copy(isLoading = false, returns = page.content) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun onFilterChange(status: String?) = load(status)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReturnsScreen(
    onReturnClick: (id: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AdminReturnsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Tải lại mỗi khi quay lại màn (sau khi xử lý xong một yêu cầu)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) viewModel.load()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yêu cầu trả hàng") },
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                returnFilters.forEach { f ->
                    FilterChip(
                        selected = uiState.selectedFilter == f.value,
                        onClick = { viewModel.onFilterChange(f.value) },
                        label = { Text(f.label) }
                    )
                }
            }

            when {
                uiState.isLoading && uiState.returns.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null && uiState.returns.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.load() }) { Text("Thử lại") }
                        }
                    }
                }
                uiState.returns.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Không có yêu cầu nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.returns, key = { it.id }) { item ->
                            AdminReturnCard(item = item, onClick = { onReturnClick(item.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminReturnCard(item: ReturnRequestResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))) {
                    if (item.plantImage != null) {
                        AsyncImage(
                            model = item.plantImage,
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
                    Text(
                        item.plantName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Khách: ${item.customer?.fullName ?: "—"} • SL: ${item.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatReturnDate(item.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ReturnStatusBadge(status = item.status)
            }
            if (item.refundAmount != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tiền hoàn: ${MoneyFormatter.format(item.refundAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

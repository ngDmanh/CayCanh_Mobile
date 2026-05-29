package com.example.caycanh_mobile.ui.admin.rentals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

private data class RentalFilter(val label: String, val value: String?)
private val rentalFilters = listOf(
    RentalFilter("Tất cả", null),
    RentalFilter("Chờ giao", "pending_delivery"),
    RentalFilter("Đang thuê", "active"),
    RentalFilter("Quá hạn", "overdue"),
    RentalFilter("Đã trả", "returned")
)

data class AdminRentalsUiState(
    val rentals: List<AdminRentalResponse> = emptyList(),
    val selectedFilter: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminRentalsViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminRentalsUiState())
    val uiState: StateFlow<AdminRentalsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load(status: String? = _uiState.value.selectedFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, selectedFilter = status) }
            adminRepository.getRentals(status)
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, rentals = list) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    fun onFilterChange(status: String?) = load(status)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRentalsScreen(
    onRentalClick: (rentalId: String) -> Unit,
    viewModel: AdminRentalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    android.util.Log.d("AdminRentalsUI", "rentals.size=${uiState.rentals.size}, isLoading=${uiState.isLoading}, error=${uiState.errorMessage}, filter=${uiState.selectedFilter}")


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
                title = { Text("Đơn cho thuê") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
                rentalFilters.forEach { f ->
                    FilterChip(
                        selected = uiState.selectedFilter == f.value,
                        onClick = { viewModel.onFilterChange(f.value) },
                        label = { Text(f.label) }
                    )
                }
            }

            when {
                uiState.isLoading && uiState.rentals.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                uiState.rentals.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Không có rental nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.rentals, key = { it.id }) { rental ->
                            AdminRentalCard(rental = rental, onClick = { onRentalClick(rental.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminRentalCard(rental: AdminRentalResponse, onClick: () -> Unit) {
    val st = rentalStatusDisplay(rental.status)
    val daysLeft = daysToEnd(rental.endDate)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))) {
                if (rental.primaryImageUrl != null) {
                    AsyncImage(model = rental.primaryImageUrl, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center) { Text("🌿") }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        rental.plantName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(shape = RoundedCornerShape(6.dp), color = st.color.copy(alpha = 0.12f)) {
                        Text(st.label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.bodySmall, color = st.color,
                            fontWeight = FontWeight.Medium)
                    }
                }
                Text(
                    "${rental.customer?.fullName ?: "?"} • ${rental.duration} ${durationUnitLabel(rental.durationUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Từ ${rental.startDate} → ${rental.endDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Cảnh báo còn lại / quá hạn
                if (daysLeft != null && rental.status != "returned") {
                    val (text, color) = when {
                        daysLeft < 0 -> "Quá hạn ${-daysLeft} ngày" to Color(0xFFE53935)
                        daysLeft == 0 -> "Hết hạn hôm nay" to Color(0xFFFF9800)
                        daysLeft <= 3 -> "Còn $daysLeft ngày" to Color(0xFFFF9800)
                        else -> "Còn $daysLeft ngày" to MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(text, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Medium)
                }

                Text(
                    MoneyFormatter.format(rental.totalRentalFee),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
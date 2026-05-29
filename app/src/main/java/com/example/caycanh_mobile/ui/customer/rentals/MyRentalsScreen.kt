package com.example.caycanh_mobile.ui.customer.rentals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class MyRentalsUiState(
    val rentals: List<CustomerRentalResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MyRentalsViewModel @Inject constructor(
    private val rentalRepository: RentalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyRentalsUiState())
    val uiState: StateFlow<MyRentalsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            rentalRepository.getMyRentals()
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, rentals = list) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }
}

@Composable
fun MyRentalsScreen(
    onRentalClick: (rentalId: String) -> Unit,
    viewModel: MyRentalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) viewModel.load()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when {
        uiState.isLoading && uiState.rentals.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        uiState.errorMessage != null && uiState.rentals.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.load() }) { Text("Thử lại") }
                }
            }
        }
        uiState.rentals.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🌿", style = MaterialTheme.typography.displayMedium)
                    Text("Bạn chưa thuê cây nào", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Khám phá các cây cho thuê tại Trang chủ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.rentals, key = { it.id }) { rental ->
                    MyRentalCard(rental = rental, onClick = { onRentalClick(rental.id) })
                }
            }
        }
    }
}

@Composable
private fun MyRentalCard(rental: CustomerRentalResponse, onClick: () -> Unit) {
    val st = rentalStatusDisplay(rental.status)
    val days = daysToEnd(rental.endDate)

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
                    "Thuê ${rental.duration} ${durationUnitLabel(rental.durationUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (rental.startDate != null && rental.endDate != null) {
                    Text(
                        "Từ ${rental.startDate} → ${rental.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (rental.status == "pending_delivery") {
                    Text(
                        "Chờ admin giao cây",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (days != null && rental.status in listOf("active", "overdue")) {
                    val (text, color) = when {
                        days < 0 -> "Quá hạn ${-days} ngày" to Color(0xFFE53935)
                        days == 0 -> "Hết hạn hôm nay" to Color(0xFFFF9800)
                        days <= 3 -> "Còn $days ngày" to Color(0xFFFF9800)
                        else -> "Còn $days ngày" to MaterialTheme.colorScheme.onSurfaceVariant
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
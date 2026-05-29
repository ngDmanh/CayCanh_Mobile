package com.example.caycanh_mobile.ui.admin.plants

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.caycanh_mobile.data.remote.dto.plant.PlantResponse
import com.example.caycanh_mobile.data.repository.AdminRepository
import com.example.caycanh_mobile.data.repository.PlantRepository
import com.example.caycanh_mobile.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminPlantsUiState(
    val plants: List<PlantResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val deletingId: String? = null,
    val plantToDelete: PlantResponse? = null
)

@HiltViewModel
class AdminPlantsViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminPlantsUiState())
    val uiState: StateFlow<AdminPlantsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            plantRepository.getPlants(page = 0, size = 100)
                .onSuccess { page ->
                    _uiState.update { it.copy(isLoading = false, plants = page.content) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun askDelete(plant: PlantResponse) { _uiState.update { it.copy(plantToDelete = plant) } }
    fun dismissDelete() { _uiState.update { it.copy(plantToDelete = null) } }

    fun confirmDelete() {
        val plant = _uiState.value.plantToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(deletingId = plant.id, plantToDelete = null) }
            adminRepository.deletePlant(plant.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(deletingId = null, plants = it.plants.filterNot { p -> p.id == plant.id })
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(deletingId = null, errorMessage = e.message) }
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlantsScreen(
    onAddPlant: () -> Unit,
    onEditPlant: (plantId: String) -> Unit,
    viewModel: AdminPlantsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Reload mỗi khi vào màn
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
                title = { Text("Quản lý cây") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPlant,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Thêm cây") }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading && uiState.plants.isEmpty() -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.plants.isEmpty() -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌵", style = MaterialTheme.typography.displayMedium)
                        Text("Chưa có cây nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 12.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.plants, key = { it.id }) { plant ->
                        AdminPlantRow(
                            plant = plant,
                            isDeleting = uiState.deletingId == plant.id,
                            onEdit = { onEditPlant(plant.id) },
                            onDelete = { viewModel.askDelete(plant) }
                        )
                    }
                }
            }
        }
    }

    uiState.plantToDelete?.let { plant ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Xóa cây?") },
            text = { Text("Xóa \"${plant.name}\"? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissDelete) { Text("Hủy") } }
        )
    }
}

@Composable
private fun AdminPlantRow(
    plant: PlantResponse,
    isDeleting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                val img = plant.imageUrls.firstOrNull()
                Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))) {
                    if (img != null) {
                        AsyncImage(
                            model = img, contentDescription = null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("🌿") }
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Tên + badge loại
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            plant.name,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        ListingTypeBadge(plant.listingType)
                    }
                    Text(
                        plant.categoryName ?: "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Badge trạng thái ẩn/hiện
                    if (plant.status != "active") {
                        Text(
                            "Đang ẩn",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Sửa",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Chi tiết giá
            val isForSale = plant.listingType == "sale" || plant.listingType == "both"
            val isForRent = plant.listingType == "rent" || plant.listingType == "both"

            if (isForSale) {
                PriceLine(
                    label = "Giá bán",
                    value = plant.priceSale?.let { MoneyFormatter.format(it) } ?: "Chưa đặt",
                    extra = "Tồn: ${plant.stockQuantity}"
                )
            }
            if (isForRent) {
                if (isForSale) Spacer(Modifier.height(6.dp))
                Text(
                    "Giá thuê",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                RentPriceRow("Ngày", plant.pricePerDay)
                RentPriceRow("Tuần", plant.pricePerWeek)
                RentPriceRow("Tháng", plant.pricePerMonth)
                Spacer(Modifier.height(2.dp))
                Text(
                    "Số lượng còn: ${plant.rentAvailableQty}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ListingTypeBadge(listingType: String) {
    val (text, color) = when (listingType) {
        "sale" -> "Bán" to androidx.compose.ui.graphics.Color(0xFF1976D2)
        "rent" -> "Thuê" to androidx.compose.ui.graphics.Color(0xFF388E3C)
        "both" -> "Bán & Thuê" to androidx.compose.ui.graphics.Color(0xFF7B1FA2)
        else -> listingType to androidx.compose.ui.graphics.Color.Gray
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PriceLine(label: String, value: String, extra: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "$label: $value",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
        if (extra != null) {
            Text(
                extra,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RentPriceRow(unit: String, price: Long?) {
    if (price == null || price <= 0) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "• $unit",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            MoneyFormatter.format(price),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
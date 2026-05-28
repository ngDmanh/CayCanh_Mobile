package com.example.caycanh_mobile.ui.customer.plantdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.caycanh_mobile.data.remote.dto.plant.PlantResponse
import com.example.caycanh_mobile.ui.components.PrimaryButton
import com.example.caycanh_mobile.util.MoneyFormatter
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    onNavigateBack: () -> Unit,
    onAddToCartSuccess: () -> Unit,
    viewModel: PlantDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Hiện snackbar khi có lỗi add to cart
    LaunchedEffect(uiState.addToCartError) {
        uiState.addToCartError?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeAddToCartError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.plant?.name ?: "Chi tiết cây") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.plant != null) {
                PlantDetailBottomBar(
                    plant = uiState.plant!!,
                    selectedAction = uiState.selectedActionType,
                    rentTotal = viewModel.calculateRentTotal(),
                    isLoading = uiState.isAddingToCart,
                    onAddToCart = {
                        viewModel.addToCart { onAddToCartSuccess() }
                    }
                )
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadPlant() }) { Text("Thử lại") }
                    }
                }
            }
            uiState.plant != null -> {
                PlantDetailContent(
                    plant = uiState.plant!!,
                    uiState = uiState,
                    rentTotal = viewModel.calculateRentTotal(),
                    onActionTypeChange = viewModel::onActionTypeChange,
                    onRentUnitChange = viewModel::onRentUnitChange,
                    onRentQuantityChange = viewModel::onRentQuantityChange,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun PlantDetailContent(
    plant: PlantResponse,
    uiState: PlantDetailUiState,
    rentTotal: Long,
    onActionTypeChange: (ActionType) -> Unit,
    onRentUnitChange: (RentUnit) -> Unit,
    onRentQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // Image gallery
        ImageGallery(imageUrls = plant.imageUrls)

        Column(modifier = Modifier.padding(16.dp)) {
            // Tên + Category
            Text(
                plant.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (plant.categoryName != null) {
                Text(
                    plant.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            // Chọn Mua / Thuê (nếu listingType = both)
            if (plant.listingType == "both") {
                Text(
                    "Bạn muốn:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.selectedActionType == ActionType.Sale,
                        onClick = { onActionTypeChange(ActionType.Sale) },
                        label = { Text("Mua đứt") }
                    )
                    FilterChip(
                        selected = uiState.selectedActionType == ActionType.Rent,
                        onClick = { onActionTypeChange(ActionType.Rent) },
                        label = { Text("Thuê") }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // Card giá theo lựa chọn
            when {
                uiState.selectedActionType == ActionType.Sale && plant.isForSale -> {
                    SaleCard(plant = plant)
                }
                uiState.selectedActionType == ActionType.Rent && plant.isForRent -> {
                    RentCard(
                        plant = plant,
                        selectedUnit = uiState.selectedRentUnit,
                        quantity = uiState.rentQuantity,
                        total = rentTotal,
                        onUnitChange = onRentUnitChange,
                        onQuantityChange = onRentQuantityChange
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Mô tả
            if (!plant.description.isNullOrBlank()) {
                Text(
                    "Mô tả",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    plant.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ─── Section Đánh giá ───
            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(
                "Đánh giá",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))

            if (uiState.totalReviews > 0) {
                // Summary: điểm trung bình + sao
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        String.format("%.1f", uiState.averageRating ?: 0.0),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB300)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        com.example.caycanh_mobile.ui.customer.review.StarRow(
                            rating = Math.round(uiState.averageRating ?: 0.0).toInt(),
                            starSize = 18
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${uiState.totalReviews} đánh giá",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Danh sách review
                uiState.reviews.forEach { review ->
                    com.example.caycanh_mobile.ui.customer.review.ReviewCard(
                        review = review,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            } else {
                Text(
                    "Chưa có đánh giá nào cho cây này",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageGallery(imageUrls: List<String>) {
    if (imageUrls.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("🌿", style = MaterialTheme.typography.displayLarge)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    Box {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) { page ->
            AsyncImage(
                model = imageUrls[page],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Indicator dots
        if (imageUrls.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(imageUrls.size) { index ->
                    val isActive = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) Color.White
                                else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SaleCard(plant: PlantResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Giá bán",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                MoneyFormatter.format(plant.priceSale ?: 0),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Còn ${plant.stockQuantity} cây",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun RentCard(
    plant: PlantResponse,
    selectedUnit: RentUnit,
    quantity: Int,
    total: Long,
    onUnitChange: (RentUnit) -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Giá thuê",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))

            // Chọn khung thời gian
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RentUnitChip(
                    unit = RentUnit.Day,
                    price = plant.pricePerDay,
                    selected = selectedUnit == RentUnit.Day,
                    onClick = { onUnitChange(RentUnit.Day) }
                )
                RentUnitChip(
                    unit = RentUnit.Week,
                    price = plant.pricePerWeek,
                    selected = selectedUnit == RentUnit.Week,
                    onClick = { onUnitChange(RentUnit.Week) }
                )
                RentUnitChip(
                    unit = RentUnit.Month,
                    price = plant.pricePerMonth,
                    selected = selectedUnit == RentUnit.Month,
                    onClick = { onUnitChange(RentUnit.Month) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Stepper số lượng
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Thời gian thuê:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onQuantityChange(-1) },
                        enabled = quantity > 1
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Giảm")
                    }
                    Text(
                        "$quantity ${selectedUnit.label.lowercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = { onQuantityChange(1) }) {
                        Icon(Icons.Default.Add, contentDescription = "Tăng")
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Tổng tiền
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Tổng tiền thuê:",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    MoneyFormatter.format(total),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "Còn ${plant.rentAvailableQty} cây",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun RentUnitChip(
    unit: RentUnit,
    price: Long?,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (price == null || price <= 0) return

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Column {
                Text(unit.label, style = MaterialTheme.typography.bodySmall)
                Text(
                    MoneyFormatter.formatShort(price),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
private fun PlantDetailBottomBar(
    plant: PlantResponse,
    selectedAction: ActionType,
    rentTotal: Long,
    isLoading: Boolean,
    onAddToCart: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tạm tính", style = MaterialTheme.typography.bodySmall)
                val amount = when (selectedAction) {
                    ActionType.Sale -> plant.priceSale ?: 0
                    ActionType.Rent -> rentTotal
                }
                Text(
                    MoneyFormatter.format(amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            PrimaryButton(
                text = "Thêm vào giỏ",
                onClick = onAddToCart,
                loading = isLoading,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
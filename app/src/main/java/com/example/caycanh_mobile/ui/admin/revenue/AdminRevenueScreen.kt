package com.example.caycanh_mobile.ui.admin.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caycanh_mobile.data.remote.dto.admin.*
import com.example.caycanh_mobile.data.repository.AdminRepository
import com.example.caycanh_mobile.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminRevenueUiState(
    val totalRevenue: Long = 0,
    val totalOrders: Long = 0,
    val avgOrderValue: Long = 0,
    val saleRevenue: Long = 0,
    val rentalRevenue: Long = 0,
    val saleOrders: Long = 0,
    val rentalOrders: Long = 0,
    val monthly: List<RevenueMonthlyItem> = emptyList(),
    val topPlants: List<TopPlantItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminRevenueViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminRevenueUiState(isLoading = true))
    val uiState: StateFlow<AdminRevenueUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                coroutineScope {
                    val byTypeDef = async { adminRepository.getRevenueByType() }
                    val monthlyDef = async { adminRepository.getRevenueMonthly() }
                    val topPlantsDef = async { adminRepository.getTopPlants() }

                    val byType = byTypeDef.await().getOrNull() ?: emptyList()
                    val monthly = monthlyDef.await().getOrNull() ?: emptyList()
                    val topPlants = topPlantsDef.await().getOrNull() ?: emptyList()

                    val sale = byType.firstOrNull { it.orderType == "sale" }
                    val rental = byType.firstOrNull { it.orderType == "rental" }
                    val totalRevenue = byType.sumOf { it.totalRevenue }
                    val totalOrders = byType.sumOf { it.totalOrders }
                    val avg = if (totalOrders > 0) totalRevenue / totalOrders else 0L

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            totalRevenue = totalRevenue,
                            totalOrders = totalOrders,
                            avgOrderValue = avg,
                            saleRevenue = sale?.totalRevenue ?: 0,
                            rentalRevenue = rental?.totalRevenue ?: 0,
                            saleOrders = sale?.totalOrders ?: 0,
                            rentalOrders = rental?.totalOrders ?: 0,
                            monthly = monthly,
                            topPlants = topPlants
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRevenueScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminRevenueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doanh thu") },
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
        if (uiState.isLoading) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Card lớn doanh thu tổng
            TotalRevenueHero(
                total = uiState.totalRevenue,
                orders = uiState.totalOrders,
                avg = uiState.avgOrderValue
            )

            Spacer(Modifier.height(20.dp))

            // 2 thẻ Mua vs Thuê
            Text("Phân tích theo loại", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TypeCard(
                    label = "Mua đứt",
                    icon = Icons.Default.ShoppingCart,
                    revenue = uiState.saleRevenue,
                    orders = uiState.saleOrders,
                    total = uiState.totalRevenue,
                    color = Color(0xFF388E3C),
                    modifier = Modifier.weight(1f)
                )
                TypeCard(
                    label = "Cho thuê",
                    icon = Icons.Default.AccountBalance,
                    revenue = uiState.rentalRevenue,
                    orders = uiState.rentalOrders,
                    total = uiState.totalRevenue,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Biểu đồ 12 tháng
            Text("Doanh thu 12 tháng gần nhất", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            MonthlyChart(uiState.monthly)

            Spacer(Modifier.height(20.dp))

            // Top cây bán chạy đầy đủ
            if (uiState.topPlants.isNotEmpty()) {
                Text("Top cây bán/cho thuê", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                TopPlantsFullList(uiState.topPlants)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun TotalRevenueHero(total: Long, orders: Long, avg: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Tổng doanh thu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                MoneyFormatter.format(total),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        "$orders đơn hoàn thành",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "TB ${MoneyFormatter.format(avg)}/đơn",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    revenue: Long,
    orders: Long,
    total: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    val percent = if (total > 0) (revenue * 100f / total).toInt() else 0
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                MoneyFormatter.format(revenue),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                "$orders đơn • $percent% tổng",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MonthlyChart(monthly: List<RevenueMonthlyItem>) {
    // Gộp sale+rental cùng tháng
    val dataByMonth = monthly
        .groupBy { it.month.take(7) }
        .mapValues { (_, items) -> items.sumOf { it.revenue } }

    val months = generateRecentMonths(12)
    val chartData = months.map { it to (dataByMonth[it] ?: 0L) }
    val maxRev = chartData.maxOfOrNull { it.second } ?: 0L

    val thisMonth = chartData.last().second
    val lastMonth = chartData.dropLast(1).lastOrNull()?.second ?: 0L
    val percentChange: Int? = when {
        lastMonth == 0L -> null
        else -> (((thisMonth - lastMonth) * 100f) / lastMonth).toInt()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: tháng này + so sánh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Tháng này", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        MoneyFormatter.format(thisMonth),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                percentChange?.let { pct ->
                    val (text, color, icon) = if (pct >= 0)
                        Triple("$pct%", Color(0xFF4CAF50), Icons.Default.TrendingUp)
                    else
                        Triple("${-pct}%", Color(0xFFE53935), Icons.AutoMirrored.Filled.TrendingDown)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = color.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, contentDescription = null, tint = color,
                                modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(text, style = MaterialTheme.typography.bodySmall,
                                color = color, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Biểu đồ cột 12 tháng
            Row(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                chartData.forEach { (month, revenue) ->
                    val heightFrac = if (maxRev > 0)
                        (revenue.toFloat() / maxRev).coerceIn(0.02f, 1f)
                    else 0.02f
                    val isCurrent = month == chartData.last().first

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (revenue > 0) {
                            Text(
                                formatShortRevenue(revenue),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Spacer(Modifier.height(12.dp))
                        }
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(heightFrac)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(
                                    if (revenue > 0) {
                                        if (isCurrent) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    } else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Nhãn tháng
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                chartData.forEach { (month, _) ->
                    Text(
                        "T${month.substring(5).trimStart('0')}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun TopPlantsFullList(plants: List<TopPlantItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            plants.forEachIndexed { idx, plant ->
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)) {
                    val rankColor = when (idx) {
                        0 -> Color(0xFFFFD700)
                        1 -> Color(0xFFC0C0C0)
                        2 -> Color(0xFFCD7F32)
                        else -> Color(0xFFBDBDBD)
                    }
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(rankColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${idx + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(plant.name, style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium)
                        val typeLabel = when (plant.listingType) {
                            "sale" -> "Mua"
                            "rent" -> "Thuê"
                            "both" -> "Mua/Thuê"
                            else -> plant.listingType
                        }
                        Text(
                            "$typeLabel • ${plant.totalQuantity} lượt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (plant.reviewCount > 0 && plant.avgRating != null) {
                            Text(
                                "⭐ ${"%.1f".format(plant.avgRating)} (${plant.reviewCount} đánh giá)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        MoneyFormatter.format(plant.totalRevenue),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (idx < plants.size - 1) HorizontalDivider()
            }
        }
    }
}

private fun generateRecentMonths(count: Int): List<String> {
    val today = java.time.LocalDate.now()
    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")
    return (count - 1 downTo 0).map { back ->
        today.minusMonths(back.toLong()).format(formatter)
    }
}

private fun formatShortRevenue(amount: Long): String = when {
    amount >= 1_000_000_000 -> "${"%.1f".format(amount / 1_000_000_000.0)}tỷ"
    amount >= 1_000_000 -> "${"%.1f".format(amount / 1_000_000.0).removeSuffix(".0")}tr"
    amount >= 1_000 -> "${amount / 1_000}k"
    else -> amount.toString()
}
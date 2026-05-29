package com.example.caycanh_mobile.ui.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caycanh_mobile.data.local.TokenManager
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

data class AdminDashboardUiState(
    val userName: String? = null,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val totalRevenue: Long = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val name = tokenManager.getUserNameOnce()

            try {
                coroutineScope {
                    val ordersDef = async { adminRepository.getOrderSummary() }
                    val byTypeDef = async { adminRepository.getRevenueByType() }

                    val orders = ordersDef.await().getOrNull() ?: emptyList()
                    val byType = byTypeDef.await().getOrNull() ?: emptyList()

                    val totalRevenue = byType.sumOf { it.totalRevenue }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userName = name,
                            totalOrders = orders.size,
                            pendingOrders = orders.count { o ->
                                o.status in listOf("pending", "confirmed", "awaiting_deposit", "awaiting_payment", "delivering")
                            },
                            completedOrders = orders.count { it.status == "completed" },
                            cancelledOrders = orders.count { it.status == "cancelled" },
                            totalRevenue = totalRevenue
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, userName = name, errorMessage = e.message)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = hiltViewModel()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cây Cảnh — Quản trị") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Xin chào, ${uiState.userName ?: "Admin"}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Tổng quan hoạt động cửa hàng",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            // 1. Tổng doanh thu
            TotalRevenueCard(uiState.totalRevenue)

            Spacer(Modifier.height(20.dp))

            // 2. 4 thẻ thống kê đơn
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Tổng đơn", uiState.totalOrders.toString(), Color(0xFF1976D2), Modifier.weight(1f))
                StatCard("Chờ xử lý", uiState.pendingOrders.toString(), Color(0xFFFF9800), Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Hoàn thành", uiState.completedOrders.toString(), Color(0xFF4CAF50), Modifier.weight(1f))
                StatCard("Đã hủy", uiState.cancelledOrders.toString(), Color(0xFFE53935), Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))

        }
    }
}

@Composable
private fun SectionTitle(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
private fun TotalRevenueCard(total: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Tổng doanh thu",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                MoneyFormatter.format(total),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Text(
                value,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RevenueByTypeCard(byType: List<RevenueByTypeItem>, total: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            byType.forEach { item ->
                val percent = if (total > 0) (item.totalRevenue * 100f / total) else 0f
                val color = if (item.orderType == "rental") Color(0xFF1976D2) else Color(0xFF388E3C)
                val label = if (item.orderType == "rental") "Cho thuê" else "Mua đứt"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(10.dp).clip(CircleShape).background(color)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(label, style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f))
                    Text(
                        MoneyFormatter.format(item.totalRevenue),
                        fontWeight = FontWeight.Bold, color = color
                    )
                }
                Spacer(Modifier.height(4.dp))
                // Thanh tiến độ
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percent / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
                Text(
                    "${item.totalOrders} đơn • TB ${MoneyFormatter.format(item.avgOrderValue.toLong())}/đơn",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item != byType.last()) Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MonthlyRevenueChart(monthly: List<RevenueMonthlyItem>) {
    // Gom data theo tháng (sale + rental cùng tháng cộng dồn)
    val dataByMonth: Map<String, Long> = monthly
        .groupBy { it.month.take(7) }                          // "yyyy-MM"
        .mapValues { (_, items) -> items.sumOf { it.revenue } }

    // Generate 12 tháng gần nhất kể cả tháng 0₫
    val months = generateRecentMonths(12)
    val chartData = months.map { it to (dataByMonth[it] ?: 0L) }

    val maxRevenue = chartData.maxOfOrNull { it.second } ?: 0L

    // Tính tổng + trung bình + so với tháng trước
    val totalAll = chartData.sumOf { it.second }
    val monthsWithRevenue = chartData.count { it.second > 0 }
    val avgPerMonth = if (monthsWithRevenue > 0) totalAll / monthsWithRevenue else 0L
    val thisMonth = chartData.lastOrNull()?.second ?: 0L
    val lastMonth = chartData.dropLast(1).lastOrNull()?.second ?: 0L
    val percentChange: Int? = when {
        lastMonth == 0L && thisMonth > 0 -> null                // không so sánh được
        lastMonth == 0L -> null
        else -> (((thisMonth - lastMonth) * 100f) / lastMonth).toInt()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tóm tắt: tháng này + so với tháng trước
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "Tháng này",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        MoneyFormatter.format(thisMonth),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    percentChange?.let { pct ->
                        val (arrow, color) = if (pct >= 0)
                            "↑ $pct%" to Color(0xFF4CAF50)
                        else
                            "↓ ${-pct}%" to Color(0xFFE53935)
                        Text(
                            "$arrow so với tháng trước",
                            style = MaterialTheme.typography.bodySmall,
                            color = color,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "TB/tháng",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        MoneyFormatter.format(avgPerMonth),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Biểu đồ cột 12 tháng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                chartData.forEach { (month, revenue) ->
                    val heightFrac = if (maxRevenue > 0)
                        (revenue.toFloat() / maxRevenue).coerceIn(0.02f, 1f)
                    else 0.02f
                    val isCurrentMonth = month == chartData.last().first

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (revenue > 0) {
                            Text(
                                formatShortRevenue(revenue),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp),
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
                                        if (isCurrentMonth) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    } else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Nhãn tháng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                chartData.forEach { (month, _) ->
                    Text(
                        "T${month.substring(5).trimStart('0')}",   // "T1", "T2", ..., "T12"
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

/** Sinh 12 tháng gần nhất dạng "yyyy-MM", từ cũ → mới */
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

@Composable
private fun TopPlantsCard(plants: List<TopPlantItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            plants.take(5).forEachIndexed { idx, plant ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                    // Rank badge
                    val rankColor = when (idx) {
                        0 -> Color(0xFFFFD700)        // vàng
                        1 -> Color(0xFFC0C0C0)        // bạc
                        2 -> Color(0xFFCD7F32)        // đồng
                        else -> Color(0xFFBDBDBD)
                    }
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(rankColor),
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
                        Text(
                            plant.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${plant.totalQuantity} đã bán/thuê",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        MoneyFormatter.format(plant.totalRevenue),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (idx < plants.take(5).size - 1) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun LowStockCard(plants: List<LowStockItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            plants.forEach { plant ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(plant.name, fontWeight = FontWeight.Medium)
                        plant.categoryName?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (plant.listingType in listOf("sale", "both")) {
                            Text("Bán: ${plant.stockQuantity}", style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE53935), fontWeight = FontWeight.Medium)
                        }
                        if (plant.listingType in listOf("rent", "both")) {
                            Text("Thuê: ${plant.rentAvailableQty}", style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE53935), fontWeight = FontWeight.Medium)
                        }
                    }
                }
                if (plant != plants.last()) HorizontalDivider(color = Color(0xFFE53935).copy(alpha = 0.2f))
            }
        }
    }
}
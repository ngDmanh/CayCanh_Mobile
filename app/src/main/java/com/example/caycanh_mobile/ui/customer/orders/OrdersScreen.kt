package com.example.caycanh_mobile.ui.customer.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.caycanh_mobile.data.remote.dto.order.OrderResponse
import com.example.caycanh_mobile.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onOrderClick: (orderId: String) -> Unit,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadOrders() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đơn hàng của tôi") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Tab filter
            OrderTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::onTabChange,
                activeCount = uiState.orders.count { it.isActive },
                completedCount = uiState.orders.count { it.isFinished }
            )

            val filteredOrders = viewModel.getFilteredOrders()

            when {
                uiState.isLoading && uiState.orders.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null && uiState.orders.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.loadOrders() }) { Text("Thử lại") }
                        }
                    }
                }
                filteredOrders.isEmpty() -> {
                    EmptyOrders(tab = uiState.selectedTab)
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredOrders, key = { it.id }) { order ->
                            OrderCard(
                                order = order,
                                onClick = { onOrderClick(order.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderTabRow(
    selectedTab: OrderTab,
    onTabSelected: (OrderTab) -> Unit,
    activeCount: Int,
    completedCount: Int
) {
    TabRow(
        selectedTabIndex = if (selectedTab == OrderTab.Active) 0 else 1,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Tab(
            selected = selectedTab == OrderTab.Active,
            onClick = { onTabSelected(OrderTab.Active) },
            text = {
                Text(
                    "${OrderTab.Active.label}${if (activeCount > 0) " ($activeCount)" else ""}",
                    fontWeight = if (selectedTab == OrderTab.Active) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
        Tab(
            selected = selectedTab == OrderTab.Completed,
            onClick = { onTabSelected(OrderTab.Completed) },
            text = {
                Text(
                    "${OrderTab.Completed.label}${if (completedCount > 0) " ($completedCount)" else ""}",
                    fontWeight = if (selectedTab == OrderTab.Completed) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
    }
}

@Composable
private fun OrderCard(
    order: OrderResponse,
    onClick: () -> Unit
) {
    val displayInfo = getOrderDisplayInfo(order)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // Header: mã đơn + badge type + status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Đơn #${order.id.take(8)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(6.dp))
                        OrderTypeBadge(orderType = order.orderType)
                    }
                    Text(
                        formatCreatedAt(order.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(displayInfo = displayInfo)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Items preview — chỉ 2 item đầu
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                order.items.take(2).forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            if (item.primaryImageUrl != null) {
                                AsyncImage(
                                    model = item.primaryImageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) { Text("🌿", style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            item.plantName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "× ${item.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (order.items.size > 2) {
                    Text(
                        "+ ${order.items.size - 2} sản phẩm khác",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Action message hoặc tổng tiền
            if (displayInfo.actionMessage != null) {
                ActionMessageCard(
                    message = displayInfo.actionMessage,
                    color = displayInfo.statusColor
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        displayInfo.userMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        MoneyFormatter.format(order.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderTypeBadge(orderType: String) {
    val (text, color) = when (orderType) {
        "sale" -> "Mua" to Color(0xFF388E3C)
        "rental" -> "Thuê" to Color(0xFF1976D2)
        else -> "" to Color.Gray
    }
    if (text.isEmpty()) return

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun StatusBadge(displayInfo: OrderDisplayInfo) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = displayInfo.statusColor.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = displayInfo.statusIcon,
                contentDescription = null,
                tint = displayInfo.statusColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                displayInfo.statusLabel,
                style = MaterialTheme.typography.bodySmall,
                color = displayInfo.statusColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActionMessageCard(message: String, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyOrders(tab: OrderTab) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Inventory2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                when (tab) {
                    OrderTab.Active -> "Không có đơn nào đang theo dõi"
                    OrderTab.Completed -> "Chưa có đơn hoàn thành"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Đặt hàng để xem ở đây",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Format ISO date string → "20/05/2026 14:30" */
private fun formatCreatedAt(isoString: String): String {
    return try {
        val date = java.time.OffsetDateTime.parse(isoString)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        date.format(formatter)
    } catch (e: Exception) {
        isoString.take(10)
    }
}
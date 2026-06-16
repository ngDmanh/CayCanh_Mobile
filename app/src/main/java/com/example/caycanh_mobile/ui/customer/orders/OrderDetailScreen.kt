package com.example.caycanh_mobile.ui.customer.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
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
import com.example.caycanh_mobile.data.remote.dto.order.OrderItemResponse
import com.example.caycanh_mobile.data.remote.dto.order.OrderResponse
import com.example.caycanh_mobile.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    onNavigateBack: () -> Unit,
    onReviewClick: (orderId: String, plantId: String, plantName: String) -> Unit,
    onReturnClick: (orderId: String, orderItemId: String) -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.cancelSuccess) {
        if (uiState.cancelSuccess) {
            snackbarHostState.showSnackbar("Đã hủy đơn hàng")
            viewModel.consumeCancelSuccess()
        }
    }

    LaunchedEffect(uiState.cancelErrorMessage) {
        uiState.cancelErrorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeCancelError()
        }
    }

    // Tải lại trạng thái trả hàng mỗi khi quay lại màn (admin có thể vừa duyệt/hoàn tiền)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadReturns()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            uiState.order?.let { order ->
                if (order.canCancel) {
                    BottomCancelBar(
                        isCancelling = uiState.isCancelling,
                        onCancel = viewModel::onCancelClick
                    )
                }
            }
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
                        Button(onClick = { viewModel.loadOrder() }) { Text("Thử lại") }
                    }
                }
            }
            uiState.order != null -> {
                OrderDetailContent(
                    order = uiState.order!!,
                    reviewedPlantIds = uiState.reviewedPlantIds,
                    returnStatusByItemId = uiState.returnStatusByItemId,
                    onReviewClick = onReviewClick,
                    onReturnClick = onReturnClick,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    // Cancel confirmation dialog
    if (uiState.showCancelDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onCancelDismiss,
            title = { Text("Hủy đơn hàng?") },
            text = { Text("Bạn có chắc chắn muốn hủy đơn hàng này? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = viewModel::onCancelConfirm) {
                    Text("Đồng ý hủy", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelDismiss) {
                    Text("Không")
                }
            }
        )
    }
}

@Composable
private fun OrderDetailContent(
    order: OrderResponse,
    reviewedPlantIds: Set<String>,
    returnStatusByItemId: Map<String, String>,
    onReviewClick: (orderId: String, plantId: String, plantName: String) -> Unit,
    onReturnClick: (orderId: String, orderItemId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayInfo = getOrderDisplayInfo(order)
    val timeline = buildTimeline(order)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // Header tóm tắt status
        OrderStatusHeader(order = order, displayInfo = displayInfo)

        // Action card (nếu cần làm gì)
        if (displayInfo.actionMessage != null) {
            Spacer(Modifier.height(12.dp))
            ActionCard(
                message = displayInfo.actionMessage,
                color = displayInfo.statusColor,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Timeline
        SectionTitle("Tiến trình đơn hàng", modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        OrderTimeline(
            steps = timeline,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Thông tin nhận hàng
        SectionTitle("Thông tin nhận hàng", modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        RecipientCard(order = order, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(Modifier.height(24.dp))

        // Items
        SectionTitle("Sản phẩm (${order.items.size})", modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        ItemsCard(
            items = order.items,
            canReview = order.status == "completed",
            reviewedPlantIds = reviewedPlantIds,
            returnStatusByItemId = returnStatusByItemId,
            onReviewClick = { item ->
                onReviewClick(order.id, item.plantId, item.plantName)
            },
            onReturnClick = { item ->
                onReturnClick(order.id, item.id)
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Thanh toán
        SectionTitle("Thanh toán", modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        PaymentCard(order = order, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(Modifier.height(24.dp))

        // Mã đơn + ngày
        OrderMetaCard(order = order, modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@Composable
private fun OrderStatusHeader(order: OrderResponse, displayInfo: OrderDisplayInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = displayInfo.statusColor.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = displayInfo.statusIcon,
                    contentDescription = null,
                    tint = displayInfo.statusColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    displayInfo.statusLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = displayInfo.statusColor
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                displayInfo.userMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ActionCard(message: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📱", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.Medium
            )
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

@Composable
private fun OrderTimeline(steps: List<TimelineStep>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            steps.forEachIndexed { index, step ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Bullet + line
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(24.dp)
                    ) {
                        StepBullet(state = step.state)
                        if (index < steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(40.dp)
                                    .background(
                                        when (step.state) {
                                            StepState.Done -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        }
                                    )
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    // Label
                    Column(modifier = Modifier.padding(bottom = if (index < steps.size - 1) 16.dp else 0.dp)) {
                        Text(
                            step.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = when (step.state) {
                                StepState.Active -> FontWeight.Bold
                                StepState.Done -> FontWeight.Medium
                                else -> FontWeight.Normal
                            },
                            color = when (step.state) {
                                StepState.Pending -> MaterialTheme.colorScheme.onSurfaceVariant
                                StepState.Failed -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        step.sublabel?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepBullet(state: StepState) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
                when (state) {
                    StepState.Done -> MaterialTheme.colorScheme.primary
                    StepState.Active -> MaterialTheme.colorScheme.primary
                    StepState.Pending -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    StepState.Failed -> MaterialTheme.colorScheme.error
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            StepState.Done -> Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp)
            )
            StepState.Active -> Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary)
            )
            StepState.Failed -> Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(14.dp)
            )
            StepState.Pending -> {}
        }
    }
}

@Composable
private fun RecipientCard(order: OrderResponse, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(icon = Icons.Default.Person, label = order.recipientName)
            Spacer(Modifier.height(8.dp))
            InfoRow(icon = Icons.Default.Phone, label = order.recipientPhone)
            Spacer(Modifier.height(8.dp))
            InfoRow(icon = Icons.Default.LocationOn, label = order.shippingAddress)
            if (!order.note.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ghi chú: ${order.note}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ItemsCard(
    items: List<OrderItemResponse>,
    canReview: Boolean,
    reviewedPlantIds: Set<String>,
    returnStatusByItemId: Map<String, String>,
    onReviewClick: (OrderItemResponse) -> Unit,
    onReturnClick: (OrderItemResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            items.forEachIndexed { idx, item ->
                ItemRow(
                    item = item,
                    canReview = canReview,
                    alreadyReviewed = reviewedPlantIds.contains(item.plantId),
                    returnStatus = returnStatusByItemId[item.id],
                    onReviewClick = { onReviewClick(item) },
                    onReturnClick = { onReturnClick(item) }
                )
                if (idx < items.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun ItemRow(
    item: OrderItemResponse,
    canReview: Boolean = false,
    alreadyReviewed: Boolean = false,
    returnStatus: String? = null,
    onReviewClick: () -> Unit = {},
    onReturnClick: () -> Unit = {}
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
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
                    ) { Text("🌿") }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.plantName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val rental = item.rental
                val meta = if (rental != null) {
                    val unit = when (rental.durationUnit) {
                        "day" -> "ngày"
                        "week" -> "tuần"
                        "month" -> "tháng"
                        else -> ""
                    }
                    val timeRange = if (rental.startDate != null && rental.endDate != null) {
                        " • ${rental.startDate} → ${rental.endDate}"
                    } else {
                        " • Chờ admin giao cây"
                    }
                    "Thuê ${rental.duration} $unit × ${MoneyFormatter.format(item.unitPrice)}$timeRange"
                } else {
                    "${item.quantity} × ${MoneyFormatter.format(item.unitPrice)}"
                }
                Text(
                    meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                MoneyFormatter.format(item.subtotal),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        // Khu vực hành động — chỉ với cây mua trong đơn đã hoàn thành
        if (canReview && item.rental == null) {
            val hasActiveReturn = returnStatus == "requested" || returnStatus == "approved"
            val isRefunded = returnStatus == "completed"

            when {
                // Đã gửi yêu cầu, đang xử lý → ẩn nút, hiện trạng thái
                hasActiveReturn -> {
                    Spacer(Modifier.height(8.dp))
                    ReturnStatusLabel(text = "Đang duyệt trả hàng", color = Color(0xFFFF9800))
                }
                // Đã hoàn tiền xong
                isRefunded -> {
                    Spacer(Modifier.height(8.dp))
                    ReturnStatusLabel(text = "Đã hoàn tiền", color = Color(0xFF4CAF50))
                }
                // Chưa trả (hoặc yêu cầu trước đã bị từ chối) → hiện nút Đánh giá + Trả hàng
                else -> {
                    Spacer(Modifier.height(8.dp))
                    if (alreadyReviewed) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Đã đánh giá",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onReviewClick,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Đánh giá", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onReturnClick,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text("Trả hàng", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReturnStatusLabel(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PaymentCard(order: OrderResponse, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tổng tiền:", style = MaterialTheme.typography.bodyLarge)
                Text(
                    MoneyFormatter.format(order.totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Trạng thái thanh toán:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val (label, color) = when (order.paymentStatus) {
                    "paid" -> "Đã thanh toán" to Color(0xFF4CAF50)
                    "partial" -> "Đã cọc một phần" to Color(0xFFFF9800)
                    else -> "Chưa thanh toán" to Color(0xFF9E9E9E)
                }
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun OrderMetaCard(order: OrderResponse, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Mã đơn: ${order.id}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Ngày đặt: ${formatCreatedAt(order.createdAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BottomCancelBar(isCancelling: Boolean, onCancel: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !isCancelling,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.error,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Hủy đơn hàng")
                }
            }
        }
    }
}

private fun formatCreatedAt(isoString: String): String {
    return try {
        val date = java.time.OffsetDateTime.parse(isoString)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        date.format(formatter)
    } catch (e: Exception) {
        isoString.take(10)
    }
}
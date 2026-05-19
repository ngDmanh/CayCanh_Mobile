package com.example.caycanh_mobile.ui.customer.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.caycanh_mobile.data.remote.dto.cart.CartItemResponse
import com.example.caycanh_mobile.ui.components.PrimaryButton
import com.example.caycanh_mobile.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onCheckout: () -> Unit,
    onBrowsePlants: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Reload khi vào tab Cart
    LaunchedEffect(Unit) { viewModel.loadCart() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giỏ hàng (${uiState.totalItems})") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                CartBottomBar(
                    totalAmount = uiState.totalAmount,
                    onCheckout = onCheckout
                )
            }
        }
    ) { padding ->
        when {
            uiState.isLoading && uiState.items.isEmpty() -> {
                Box(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            uiState.items.isEmpty() -> {
                EmptyCart(
                    modifier = Modifier.padding(padding),
                    onBrowsePlants = onBrowsePlants
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        CartItemCard(
                            item = item,
                            isProcessing = uiState.processingItemId == item.id,
                            onQuantityChange = { delta ->
                                viewModel.onQuantityChange(item.id, delta)
                            },
                            onRemove = { viewModel.onRemoveClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItemResponse,
    isProcessing: Boolean,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh
            Box(
                modifier = Modifier
                    .size(80.dp)
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
                    ) {
                        Text("🌿", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.plantName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    ItemTypeBadge(itemType = item.itemType)
                }

                Spacer(Modifier.height(4.dp))

                // Meta — quantity hoặc duration
                val metaText = if (item.isRent) {
                    val unitLabel = when (item.durationUnit) {
                        "day" -> "ngày"
                        "week" -> "tuần"
                        "month" -> "tháng"
                        else -> ""
                    }
                    "${item.duration} $unitLabel × ${MoneyFormatter.format(item.unitPrice)}"
                } else {
                    "${item.quantity} × ${MoneyFormatter.format(item.unitPrice)}"
                }
                Text(
                    metaText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                // Subtotal
                Text(
                    MoneyFormatter.format(item.subtotal),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                // Stepper + Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Stepper(
                        value = if (item.isRent) item.duration ?: 1 else item.quantity,
                        suffix = if (item.isRent) {
                            when (item.durationUnit) {
                                "day" -> "ngày"
                                "week" -> "tuần"
                                "month" -> "tháng"
                                else -> ""
                            }
                        } else "",
                        enabled = !isProcessing,
                        onDecrement = { onQuantityChange(-1) },
                        onIncrement = { onQuantityChange(1) }
                    )

                    Spacer(Modifier.weight(1f))

                    IconButton(
                        onClick = onRemove,
                        enabled = !isProcessing
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Xóa",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Stepper(
    value: Int,
    suffix: String,
    enabled: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement, enabled = enabled && value > 1) {
                Icon(Icons.Default.Remove, contentDescription = "Giảm")
            }
            Text(
                text = if (suffix.isNotEmpty()) "$value $suffix" else "$value",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(onClick = onIncrement, enabled = enabled) {
                Icon(Icons.Default.Add, contentDescription = "Tăng")
            }
        }
    }
}

@Composable
private fun ItemTypeBadge(itemType: String) {
    val (text, color) = when (itemType) {
        "sale" -> "Mua" to MaterialTheme.colorScheme.primary
        "rent" -> "Thuê" to MaterialTheme.colorScheme.tertiary
        else -> "" to MaterialTheme.colorScheme.outline
    }
    if (text.isEmpty()) return

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun CartBottomBar(
    totalAmount: Long,
    onCheckout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tổng tiền", style = MaterialTheme.typography.bodySmall)
                Text(
                    MoneyFormatter.format(totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            PrimaryButton(
                text = "Đặt hàng",
                onClick = onCheckout,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EmptyCart(
    modifier: Modifier = Modifier,
    onBrowsePlants: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🛒", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Giỏ hàng đang trống",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "Khám phá các cây cảnh nhé",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onBrowsePlants) {
                Text("Mua sắm ngay")
            }
        }
    }
}
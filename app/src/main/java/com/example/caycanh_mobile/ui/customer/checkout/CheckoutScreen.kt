package com.example.caycanh_mobile.ui.customer.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.caycanh_mobile.data.remote.dto.cart.CartItemResponse
import com.example.caycanh_mobile.ui.components.PrimaryButton
import com.example.caycanh_mobile.ui.components.PrimaryTextField
import com.example.caycanh_mobile.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onCheckoutSuccess: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isCheckoutSuccess) {
        if (uiState.isCheckoutSuccess) onCheckoutSuccess()
    }

    LaunchedEffect(uiState.checkoutErrorMessage) {
        uiState.checkoutErrorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đặt hàng") },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            CheckoutBottomBar(
                totalAmount = uiState.totalAmount,
                isLoading = uiState.isCheckingOut,
                onCheckout = viewModel::onCheckoutClick
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
            // Section 1: Thông tin nhận hàng
            SectionTitle("Thông tin nhận hàng")
            Spacer(Modifier.height(8.dp))

            PrimaryTextField(
                value = uiState.recipientName,
                onValueChange = viewModel::onNameChange,
                label = "Tên người nhận *",
                leadingIcon = Icons.Default.Person,
                errorMessage = uiState.nameError,
                enabled = !uiState.isCheckingOut
            )
            Spacer(Modifier.height(12.dp))

            PrimaryTextField(
                value = uiState.recipientPhone,
                onValueChange = viewModel::onPhoneChange,
                label = "Số điện thoại *",
                leadingIcon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone,
                errorMessage = uiState.phoneError,
                enabled = !uiState.isCheckingOut
            )
            Spacer(Modifier.height(12.dp))

            PrimaryTextField(
                value = uiState.shippingAddress,
                onValueChange = viewModel::onAddressChange,
                label = "Địa chỉ giao hàng *",
                leadingIcon = Icons.Default.LocationOn,
                errorMessage = uiState.addressError,
                enabled = !uiState.isCheckingOut,
                singleLine = false
            )
            Spacer(Modifier.height(12.dp))

            PrimaryTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChange,
                label = "Ghi chú (tùy chọn)",
                leadingIcon = Icons.Default.Note,
                enabled = !uiState.isCheckingOut,
                singleLine = false
            )

            Spacer(Modifier.height(24.dp))

            // Section 2: Đơn hàng
            SectionTitle("Đơn hàng (${uiState.cartItems.size} sản phẩm)")
            Spacer(Modifier.height(8.dp))

            if (uiState.isCartLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        uiState.cartItems.forEachIndexed { idx, item ->
                            CheckoutItemRow(item = item)
                            if (idx < uiState.cartItems.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Section 3: Thanh toán
            SectionTitle("Thanh toán")
            Spacer(Modifier.height(8.dp))

            PaymentInfoCard(totalAmount = uiState.totalAmount, cartItems = uiState.cartItems)

            Spacer(Modifier.height(80.dp))  // space for bottom bar
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun CheckoutItemRow(item: CartItemResponse) {
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

            val meta = if (item.isRent) {
                val unit = when (item.durationUnit) {
                    "day" -> "ngày"
                    "week" -> "tuần"
                    "month" -> "tháng"
                    else -> ""
                }
                "Thuê ${item.duration} $unit"
            } else {
                "Mua ${item.quantity}"
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
}

@Composable
private fun PaymentInfoCard(totalAmount: Long, cartItems: List<CartItemResponse>) {
    val hasRental = cartItems.any { it.isRent }
    val saleTotal = cartItems.filter { !it.isRent }.sumOf { it.subtotal }
    val needsDeposit = saleTotal > 500_000

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tổng tiền:", style = MaterialTheme.typography.bodyLarge)
                Text(
                    MoneyFormatter.format(totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (needsDeposit || hasRental) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                if (needsDeposit) {
                    Text(
                        "⚠️ Đơn mua trên 500k cần cọc 50% (${MoneyFormatter.format(saleTotal / 2)}) trước khi giao hàng",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                if (hasRental) {
                    if (needsDeposit) Spacer(Modifier.height(8.dp))
                    Text(
                        "📌 Cây thuê cần thanh toán 100% trước khi giao",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "💬 Admin sẽ liên hệ qua Zalo để hướng dẫn chuyển khoản",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun CheckoutBottomBar(
    totalAmount: Long,
    isLoading: Boolean,
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
                Text("Tổng cộng", style = MaterialTheme.typography.bodySmall)
                Text(
                    MoneyFormatter.format(totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            PrimaryButton(
                text = "Xác nhận đặt hàng",
                onClick = onCheckout,
                loading = isLoading,
                modifier = Modifier.weight(1.2f)
            )
        }
    }
}
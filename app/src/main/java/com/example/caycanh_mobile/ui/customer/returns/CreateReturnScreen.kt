package com.example.caycanh_mobile.ui.customer.returns

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.caycanh_mobile.ui.components.PrimaryButton
import com.example.caycanh_mobile.ui.components.PrimaryTextField
import com.example.caycanh_mobile.util.MoneyFormatter

private const val SHOP_ZALO = "0982699028"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReturnScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: CreateReturnViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onSuccess()
    }
    LaunchedEffect(uiState.submitError) {
        uiState.submitError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yêu cầu trả hàng") },
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
            if (!uiState.isLoading && uiState.loadError == null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        PrimaryButton(
                            text = "Gửi yêu cầu trả hàng",
                            onClick = viewModel::onSubmit,
                            loading = uiState.isSubmitting
                        )
                    }
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

            uiState.loadError != null -> {
                Box(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.loadError!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadItem() }) { Text("Thử lại") }
                    }
                }
            }

            else -> {
                CreateReturnContent(
                    uiState = uiState,
                    onIncrease = viewModel::increaseQuantity,
                    onDecrease = viewModel::decreaseQuantity,
                    onReasonChange = viewModel::onReasonChange,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun CreateReturnContent(
    uiState: CreateReturnUiState,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onReasonChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Thông tin cây cần trả
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                ) {
                    if (uiState.plantImage != null) {
                        AsyncImage(
                            model = uiState.plantImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) { Text("🌿") }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        uiState.plantName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Đơn giá: ${MoneyFormatter.format(uiState.unitPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Đã mua: ${uiState.maxQuantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Số lượng trả
        SectionTitle("Số lượng trả")
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(
                onClick = onDecrease,
                enabled = uiState.quantity > 1
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Giảm")
            }
            Text(
                "${uiState.quantity}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            FilledTonalIconButton(
                onClick = onIncrease,
                enabled = uiState.quantity < uiState.maxQuantity
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tăng")
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "/ ${uiState.maxQuantity} cây",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

        // Lý do
        SectionTitle("Lý do trả hàng *")
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(
            value = uiState.reason,
            onValueChange = onReasonChange,
            label = "Mô tả tình trạng cây (vd: lá héo, gãy cành...)",
            errorMessage = uiState.reasonError,
            enabled = !uiState.isSubmitting,
            singleLine = false
        )

        Spacer(Modifier.height(20.dp))

        // Thẻ hướng dẫn gửi ảnh/video qua Zalo
        ZaloEvidenceCard()

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ZaloEvidenceCard() {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        border = BorderStroke(1.dp, Color(0xFFFFB74D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📷 Gửi ảnh/video bằng chứng qua Zalo",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6D4C41)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Để shop xử lý nhanh hơn, vui lòng gửi ảnh hoặc video cây bị vấn đề " +
                    "qua Zalo $SHOP_ZALO kèm mã đơn. Shop sẽ liên hệ xác nhận và hoàn tiền.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6D4C41)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { uriHandler.openUri("https://zalo.me/$SHOP_ZALO") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6D4C41)),
                border = BorderStroke(1.dp, Color(0xFF6D4C41))
            ) {
                Text("Nhắn Zalo ngay")
            }
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

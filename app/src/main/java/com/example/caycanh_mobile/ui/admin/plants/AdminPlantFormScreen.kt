package com.example.caycanh_mobile.ui.admin.plants

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlantFormScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AdminPlantFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Photo picker chọn nhiều ảnh
    val pickImages = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) viewModel.addImages(uris)
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(if (uiState.isEdit) "Đã cập nhật cây" else "Đã thêm cây")
            onSaveSuccess()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEdit) "Sửa cây" else "Thêm cây mới") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !uiState.isSaving) {
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            // ── Ảnh ──
            SectionLabel("Hình ảnh")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Nút thêm ảnh
                item {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable(enabled = !uiState.isSaving) {
                                pickImages.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = "Thêm ảnh",
                                tint = MaterialTheme.colorScheme.primary)
                            Text("Thêm ảnh", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                itemsIndexed(uiState.images) { index, img ->
                    val model: Any = when (img) {
                        is FormImage.Existing -> img.url
                        is FormImage.Local -> img.uri
                    }
                    Box(modifier = Modifier.size(90.dp)) {
                        AsyncImage(
                            model = model,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                        )
                        // Nút xóa
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable(enabled = !uiState.isSaving) { viewModel.removeImage(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa",
                                tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        // Sao đánh dấu ảnh đại diện
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(2.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable(enabled = !uiState.isSaving) { viewModel.setPrimary(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (img.isPrimary) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Ảnh đại diện",
                                tint = if (img.isPrimary) Color(0xFFFFB300) else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            Text(
                "Bấm ⭐ để chọn ảnh đại diện. Tối đa 8 ảnh.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(20.dp))

            // ── Danh mục ──
            SectionLabel("Danh mục")
            CategoryDropdown(
                categories = uiState.categories,
                selectedId = uiState.selectedCategoryId,
                onSelect = viewModel::onCategorySelect,
                onAddNew = viewModel::openAddCategory,
                enabled = !uiState.isSaving
            )

            Spacer(Modifier.height(16.dp))

            // ── Tên ──
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Tên cây *") },
                singleLine = true,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ── Mô tả ──
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Mô tả") },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )

            Spacer(Modifier.height(20.dp))

            // ── Loại ──
            SectionLabel("Loại")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ListingChip("Bán", "sale", uiState.listingType, viewModel::onListingTypeChange, !uiState.isSaving)
                ListingChip("Thuê", "rent", uiState.listingType, viewModel::onListingTypeChange, !uiState.isSaving)
                ListingChip("Cả hai", "both", uiState.listingType, viewModel::onListingTypeChange, !uiState.isSaving)
            }

            Spacer(Modifier.height(16.dp))

            val isSale = uiState.listingType == "sale" || uiState.listingType == "both"
            val isRent = uiState.listingType == "rent" || uiState.listingType == "both"

            // ── Giá bán + tồn ──
            if (isSale) {
                OutlinedTextField(
                    value = uiState.priceSale,
                    onValueChange = viewModel::onPriceSaleChange,
                    label = { Text("Giá bán (₫) *") },
                    singleLine = true,
                    enabled = !uiState.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.stockQuantity,
                    onValueChange = viewModel::onStockChange,
                    label = { Text("Số lượng tồn kho") },
                    singleLine = true,
                    enabled = !uiState.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── Giá thuê + số cho thuê ──
            if (isRent) {
                OutlinedTextField(
                    value = uiState.pricePerDay,
                    onValueChange = viewModel::onPriceDayChange,
                    label = { Text("Giá thuê / ngày (₫) *") },
                    singleLine = true,
                    enabled = !uiState.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.pricePerWeek,
                    onValueChange = viewModel::onPriceWeekChange,
                    label = { Text("Giá thuê / tuần (₫) *") },
                    singleLine = true,
                    enabled = !uiState.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.pricePerMonth,
                    onValueChange = viewModel::onPriceMonthChange,
                    label = { Text("Giá thuê / tháng (₫) *") },
                    singleLine = true,
                    enabled = !uiState.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.rentAvailableQty,
                    onValueChange = viewModel::onRentQtyChange,
                    label = { Text("Số lượng cho thuê") },
                    singleLine = true,
                    enabled = !uiState.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(20.dp))

            // ── Nút Lưu ──
            Button(
                onClick = { viewModel.save(context) },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(uiState.uploadProgress ?: "Đang lưu...")
                } else {
                    Text(if (uiState.isEdit) "Cập nhật" else "Thêm cây", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Dialog thêm danh mục nhanh
    if (uiState.showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissAddCategory,
            title = { Text("Thêm danh mục") },
            text = {
                OutlinedTextField(
                    value = uiState.newCategoryName,
                    onValueChange = viewModel::onNewCategoryNameChange,
                    label = { Text("Tên danh mục") },
                    singleLine = true,
                    enabled = !uiState.isCreatingCategory,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmAddCategory,
                    enabled = !uiState.isCreatingCategory && uiState.newCategoryName.isNotBlank()
                ) {
                    if (uiState.isCreatingCategory) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else Text("Tạo")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissAddCategory, enabled = !uiState.isCreatingCategory) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<com.example.caycanh_mobile.data.remote.dto.admin.CategoryResponse>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onAddNew: () -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = categories.firstOrNull { it.id == selectedId }?.name ?: "Chọn danh mục"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.name) },
                    onClick = { onSelect(cat.id); expanded = false }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null,
                            modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Tạo danh mục mới", color = MaterialTheme.colorScheme.primary)
                    }
                },
                onClick = { expanded = false; onAddNew() }
            )
        }
    }
}

@Composable
private fun ListingChip(
    label: String,
    value: String,
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean
) {
    FilterChip(
        selected = selected == value,
        onClick = { if (enabled) onSelect(value) },
        label = { Text(label) },
        enabled = enabled
    )
}
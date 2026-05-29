package com.example.caycanh_mobile.ui.admin.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caycanh_mobile.data.remote.dto.admin.CategoryResponse
import com.example.caycanh_mobile.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminCategoriesUiState(
    val categories: List<CategoryResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showAddDialog: Boolean = false,
    val editingCategory: CategoryResponse? = null,
    val deletingCategory: CategoryResponse? = null,
    val dialogName: String = "",
    val isProcessing: Boolean = false
)

@HiltViewModel
class AdminCategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCategoriesUiState(isLoading = true))
    val uiState: StateFlow<AdminCategoriesUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            categoryRepository.getCategories()
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, categories = list) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    fun openAdd() { _uiState.update { it.copy(showAddDialog = true, dialogName = "") } }
    fun openEdit(cat: CategoryResponse) { _uiState.update { it.copy(editingCategory = cat, dialogName = cat.name) } }
    fun askDelete(cat: CategoryResponse) { _uiState.update { it.copy(deletingCategory = cat) } }
    fun dismissDialog() { _uiState.update { it.copy(showAddDialog = false, editingCategory = null, deletingCategory = null) } }
    fun onNameChange(v: String) { _uiState.update { it.copy(dialogName = v) } }

    fun confirmAdd() {
        val name = _uiState.value.dialogName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            categoryRepository.createCategory(name)
                .onSuccess { cat ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            showAddDialog = false,
                            categories = it.categories + cat
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isProcessing = false, errorMessage = e.message) } }
        }
    }

    fun confirmEdit() {
        val cat = _uiState.value.editingCategory ?: return
        val name = _uiState.value.dialogName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            categoryRepository.updateCategory(cat.id, name)
                .onSuccess { updated ->
                    _uiState.update { st ->
                        st.copy(
                            isProcessing = false,
                            editingCategory = null,
                            categories = st.categories.map { if (it.id == updated.id) updated else it }
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isProcessing = false, errorMessage = e.message) } }
        }
    }

    fun confirmDelete() {
        val cat = _uiState.value.deletingCategory ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, deletingCategory = null) }
            categoryRepository.deleteCategory(cat.id)
                .onSuccess {
                    _uiState.update { st ->
                        st.copy(
                            isProcessing = false,
                            categories = st.categories.filterNot { it.id == cat.id }
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isProcessing = false, errorMessage = e.message) } }
        }
    }

    fun consumeError() { _uiState.update { it.copy(errorMessage = null) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminCategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.consumeError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh mục cây (${uiState.categories.size})") },
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::openAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Thêm danh mục") }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.categories.isEmpty() -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có danh mục nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.categories, key = { it.id }) { cat ->
                        CategoryRow(
                            category = cat,
                            onEdit = { viewModel.openEdit(cat) },
                            onDelete = { viewModel.askDelete(cat) }
                        )
                    }
                }
            }
        }
    }

    // Dialog thêm/sửa
    if (uiState.showAddDialog || uiState.editingCategory != null) {
        val isEdit = uiState.editingCategory != null
        AlertDialog(
            onDismissRequest = viewModel::dismissDialog,
            title = { Text(if (isEdit) "Sửa danh mục" else "Thêm danh mục") },
            text = {
                OutlinedTextField(
                    value = uiState.dialogName,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Tên danh mục") },
                    singleLine = true,
                    enabled = !uiState.isProcessing,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { if (isEdit) viewModel.confirmEdit() else viewModel.confirmAdd() },
                    enabled = !uiState.isProcessing && uiState.dialogName.isNotBlank()
                ) {
                    if (uiState.isProcessing) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text(if (isEdit) "Lưu" else "Tạo")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialog, enabled = !uiState.isProcessing) { Text("Hủy") }
            }
        )
    }

    // Dialog xóa
    uiState.deletingCategory?.let { cat ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDialog,
            title = { Text("Xóa danh mục?") },
            text = { Text("Xóa danh mục \"${cat.name}\"? Cây thuộc danh mục này có thể bị ảnh hưởng.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissDialog) { Text("Hủy") } }
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏷️", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                category.slug?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Sửa",
                    tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
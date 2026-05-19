package com.example.caycanh_mobile.ui.customer.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.caycanh_mobile.data.remote.dto.plant.CategoryResponse
import com.example.caycanh_mobile.data.remote.dto.plant.PlantResponse
import com.example.caycanh_mobile.data.repository.PlantRepository
import com.example.caycanh_mobile.ui.components.PlantCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesPlaceholderScreen(
    onPlantClick: (plantId: String) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadInitial() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh mục") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Filter listing type
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedListingType == null,
                    onClick = { viewModel.onListingTypeSelected(null) },
                    label = { Text("Tất cả") },
                    shape = RoundedCornerShape(20.dp)
                )
                FilterChip(
                    selected = uiState.selectedListingType == "sale",
                    onClick = { viewModel.onListingTypeSelected("sale") },
                    label = { Text("Mua") },
                    shape = RoundedCornerShape(20.dp)
                )
                FilterChip(
                    selected = uiState.selectedListingType == "rent",
                    onClick = { viewModel.onListingTypeSelected("rent") },
                    label = { Text("Thuê") },
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Categories row
            if (uiState.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryId == null,
                            onClick = { viewModel.onCategorySelected(null) },
                            label = { Text("Tất cả danh mục") },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    items(uiState.categories.size) { i ->
                        val cat = uiState.categories[i]
                        FilterChip(
                            selected = uiState.selectedCategoryId == cat.id,
                            onClick = { viewModel.onCategorySelected(cat.id) },
                            label = { Text(cat.name) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
            }

            // Plants grid
            when {
                uiState.isLoading && uiState.plants.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.plants.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌵", style = MaterialTheme.typography.displayLarge)
                            Text(
                                "Không tìm thấy cây phù hợp",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.plants, key = { it.id }) { plant ->
                            PlantCard(plant = plant, onClick = { onPlantClick(plant.id) })
                        }
                    }
                }
            }
        }
    }
}

data class CategoriesUiState(
    val categories: List<CategoryResponse> = emptyList(),
    val selectedCategoryId: String? = null,
    val selectedListingType: String? = null,
    val plants: List<PlantResponse> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val plantRepository: PlantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    fun loadInitial() {
        loadCategories()
        loadPlants()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            plantRepository.getCategories().onSuccess { list ->
                _uiState.update { it.copy(categories = list) }
            }
        }
    }

    fun onCategorySelected(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadPlants()
    }

    fun onListingTypeSelected(listingType: String?) {
        _uiState.update { it.copy(selectedListingType = listingType) }
        loadPlants()
    }

    private fun loadPlants() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value
            plantRepository.getPlants(
                categoryId = state.selectedCategoryId,
                listingType = state.selectedListingType,
                page = 0,
                size = 50
            )
                .onSuccess { page ->
                    _uiState.update { it.copy(isLoading = false, plants = page.content) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }
}
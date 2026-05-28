package com.example.caycanh_mobile.ui.customer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caycanh_mobile.data.local.TokenManager
import com.example.caycanh_mobile.data.remote.dto.plant.PlantResponse
import com.example.caycanh_mobile.data.repository.NotificationRepository
import com.example.caycanh_mobile.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerHomeUiState(
    val userName: String? = null,
    val searchQuery: String = "",
    val plants: List<PlantResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val unreadCount: Long = 0L      // ← thêm
) {
    val filteredPlants: List<PlantResponse>
        get() = if (searchQuery.isBlank()) plants
        else plants.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    (it.categoryName?.contains(searchQuery, ignoreCase = true) ?: false)
        }
}

@HiltViewModel
class CustomerHomeViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val notificationRepository: NotificationRepository,    // ← thêm
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerHomeUiState())
    val uiState: StateFlow<CustomerHomeUiState> = _uiState.asStateFlow()

    fun loadInitial() {
        loadUserName()
        loadPlants()
        loadUnreadCount()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            val name = tokenManager.getUserNameOnce()
            _uiState.update { it.copy(userName = name) }
        }
    }

    fun loadPlants() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            plantRepository.getPlants(page = 0, size = 50)
                .onSuccess { page ->
                    _uiState.update { it.copy(isLoading = false, plants = page.content) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Không tải được"
                        )
                    }
                }
        }
    }

    /**
     * Load số thông báo chưa đọc để hiện badge trên chuông.
     * Gọi mỗi lần Home resume để cập nhật.
     */
    fun loadUnreadCount() {
        viewModelScope.launch {
            notificationRepository.getUnreadCount()
                .onSuccess { count ->
                    _uiState.update { it.copy(unreadCount = count) }
                }
                .onFailure {
                    // Lỗi → giữ count cũ, không show lỗi
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
package com.example.caycanh_mobile.ui.customer.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caycanh_mobile.data.remote.dto.review.ReviewResponse
import com.example.caycanh_mobile.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

data class MyReviewsUiState(
    val reviews: List<ReviewResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val editingReview: ReviewResponse? = null,    // ← review đang sửa (null = đóng dialog)
    val editRating: Int = 0,                       // ← sao đang chọn trong dialog
    val editComment: String = "",                  // ← comment đang nhập
    val isUpdating: Boolean = false,               // ← đang gửi update
    val updateError: String? = null
)

@HiltViewModel
class MyReviewsViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyReviewsUiState())
    val uiState: StateFlow<MyReviewsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            reviewRepository.getMyReviews(page = 0, size = 50)
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, reviews = list) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Không tải được")
                    }
                }
        }
    }

    fun onEditClick(review: ReviewResponse) {
        _uiState.update {
            it.copy(
                editingReview = review,
                editRating = review.rating,
                editComment = review.comment ?: "",
                updateError = null
            )
        }
    }

    fun onEditDismiss() {
        if (_uiState.value.isUpdating) return
        _uiState.update { it.copy(editingReview = null) }
    }

    fun onEditRatingChange(rating: Int) {
        if (_uiState.value.isUpdating) return
        _uiState.update { it.copy(editRating = rating) }
    }

    fun onEditCommentChange(comment: String) {
        if (_uiState.value.isUpdating) return
        _uiState.update { it.copy(editComment = comment.take(500)) }
    }

    fun onEditSubmit() {
        val state = _uiState.value
        val review = state.editingReview ?: return
        if (state.editRating < 1) {
            _uiState.update { it.copy(updateError = "Vui lòng chọn số sao") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, updateError = null) }
            reviewRepository.updateReview(
                reviewId = review.id,
                rating = state.editRating,
                comment = state.editComment
            )
                .onSuccess { updated ->
                    // Cập nhật list local + đóng dialog
                    _uiState.update { s ->
                        s.copy(
                            isUpdating = false,
                            editingReview = null,
                            reviews = s.reviews.map { if (it.id == updated.id) updated else it }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isUpdating = false, updateError = e.message ?: "Lỗi cập nhật")
                    }
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MyReviewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đánh giá của tôi") },
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
                        Button(onClick = { viewModel.load() }) { Text("Thử lại") }
                    }
                }
            }
            uiState.reviews.isEmpty() -> {
                Box(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.RateReview,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Bạn chưa có đánh giá nào", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Đánh giá cây sau khi đơn hoàn thành",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.reviews, key = { it.id }) { review ->
                        ReviewCard(review = review, showPlantName = true, onEditClick = { viewModel.onEditClick(review) })
                    }
                }
            }
        }
    }
    // Dialog sửa đánh giá
    uiState.editingReview?.let { review ->
        AlertDialog(
            onDismissRequest = viewModel::onEditDismiss,
            title = { Text("Sửa đánh giá") },
            text = {
                Column {
                    Text(
                        review.plantName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))

                    // Chọn sao
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 1..5) {
                            val selected = i <= uiState.editRating
                            Icon(
                                if (selected) Icons.Default.Star
                                else Icons.Outlined.StarBorder,
                                contentDescription = "$i sao",
                                tint = if (selected) Color(0xFFFFB300)
                                else MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable(enabled = !uiState.isUpdating) {
                                        viewModel.onEditRatingChange(i)
                                    }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Comment
                    OutlinedTextField(
                        value = uiState.editComment,
                        onValueChange = viewModel::onEditCommentChange,
                        placeholder = { Text("Nhận xét...") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isUpdating,
                        supportingText = { Text("${uiState.editComment.length}/500") }
                    )

                    uiState.updateError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onEditSubmit,
                    enabled = !uiState.isUpdating && uiState.editRating > 0
                ) {
                    if (uiState.isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Lưu")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onEditDismiss, enabled = !uiState.isUpdating) {
                    Text("Hủy")
                }
            }
        )
    }
}
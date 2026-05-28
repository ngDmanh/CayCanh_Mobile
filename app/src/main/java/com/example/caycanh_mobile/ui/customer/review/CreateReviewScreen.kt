package com.example.caycanh_mobile.ui.customer.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caycanh_mobile.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

data class CreateReviewUiState(
    val orderId: String = "",
    val plantId: String = "",
    val plantName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val submitSuccess: Boolean = false
)

@HiltViewModel
class CreateReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val orderIdArg: String = checkNotNull(savedStateHandle["orderId"])
    private val plantIdArg: String = checkNotNull(savedStateHandle["plantId"])
    private val plantNameArg: String = URLDecoder.decode(
        checkNotNull(savedStateHandle["plantName"]), "UTF-8"
    )

    private val _uiState = MutableStateFlow(
        CreateReviewUiState(
            orderId = orderIdArg,
            plantId = plantIdArg,
            plantName = plantNameArg
        )
    )
    val uiState: StateFlow<CreateReviewUiState> = _uiState.asStateFlow()

    fun onRatingChange(rating: Int) {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(rating = rating) }
    }

    fun onCommentChange(comment: String) {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(comment = comment.take(500)) }
    }

    fun onSubmit() {
        if (_uiState.value.isSubmitting) return
        val state = _uiState.value
        if (state.rating < 1 || state.rating > 5) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn số sao (1-5)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            reviewRepository.createReview(
                orderId = state.orderId,
                plantId = state.plantId,
                rating = state.rating,
                comment = state.comment
            )
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = e.message ?: "Lỗi gửi đánh giá"
                        )
                    }
                }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReviewScreen(
    onNavigateBack: () -> Unit,
    onReviewSuccess: () -> Unit,
    viewModel: CreateReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            snackbarHostState.showSnackbar("Cảm ơn bạn đã đánh giá!")
            onReviewSuccess()
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
                title = { Text("Đánh giá cây") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !uiState.isSubmitting) {
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
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            // Tên cây
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Bạn đang đánh giá:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        uiState.plantName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Stars
            Text(
                "Chất lượng cây",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 1..5) {
                    val isSelected = i <= uiState.rating
                    Icon(
                        if (isSelected) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "$i sao",
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(enabled = !uiState.isSubmitting) {
                                viewModel.onRatingChange(i)
                            },
                        tint = if (isSelected) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Rating label
            val ratingLabel = when (uiState.rating) {
                1 -> "Rất tệ 😞"
                2 -> "Tệ 😐"
                3 -> "Bình thường 🙂"
                4 -> "Tốt 😊"
                5 -> "Tuyệt vời 😍"
                else -> "Xin hãy đánh giá"
            }
            Text(
                ratingLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // Comment
            Text(
                "Nhận xét (tùy chọn)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.comment,
                onValueChange = viewModel::onCommentChange,
                placeholder = { Text("Chia sẻ cảm nhận của bạn về cây này...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                enabled = !uiState.isSubmitting,
                readOnly = uiState.isSubmitting,
                supportingText = { Text("${uiState.comment.length}/500 ký tự") }
            )

            Spacer(Modifier.weight(1f))

            // Submit
            Button(
                onClick = viewModel::onSubmit,
                enabled = uiState.rating > 0 && !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Đang gửi...", fontWeight = FontWeight.Medium)
                } else {
                    Text("Gửi đánh giá", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
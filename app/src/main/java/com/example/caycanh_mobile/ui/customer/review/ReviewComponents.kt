package com.example.caycanh_mobile.ui.customer.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.caycanh_mobile.data.remote.dto.review.ReviewResponse

/** Hàng 5 sao (chỉ hiển thị, không tương tác) */
@Composable
fun StarRow(
    rating: Int,
    starSize: Int = 16,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        for (i in 1..5) {
            Icon(
                if (i <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(starSize.dp)
            )
        }
    }
}

/** 1 card review */
@Composable
fun ReviewCard(
    review: ReviewResponse,
    showPlantName: Boolean = false,
    onEditClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    review.userName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                StarRow(rating = review.rating, starSize = 14)
            }

            if (showPlantName) {
                Spacer(Modifier.height(2.dp))
                Text(
                    review.plantName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (!review.comment.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(6.dp))

            // Hàng cuối: ngày + nút Sửa (nếu có)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatReviewTime(review.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (onEditClick != null) {
                    TextButton(
                        onClick = onEditClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Sửa", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private fun formatReviewTime(isoString: String): String {
    return try {
        val date = java.time.OffsetDateTime.parse(isoString)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        date.format(formatter)
    } catch (e: Exception) {
        isoString.take(10)
    }
}
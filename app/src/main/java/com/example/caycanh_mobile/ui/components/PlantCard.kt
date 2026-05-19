package com.example.caycanh_mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.caycanh_mobile.data.remote.dto.plant.PlantResponse
import com.example.caycanh_mobile.util.MoneyFormatter
import androidx.compose.foundation.background

/**
 * Card hiển thị 1 cây trong grid.
 * Tự xử lý case không có ảnh (hiện placeholder emoji).
 */
@Composable
fun PlantCard(
    plant: PlantResponse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column {
            // Ảnh hoặc placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                val imageUrl = plant.primaryImageUrl
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = plant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder khi không có ảnh
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "🌿",
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                }

                // Badge listingType ở góc phải trên
                ListingTypeBadge(
                    listingType = plant.listingType,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }

            // Thông tin cây
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                // Giá — ưu tiên giá bán, nếu chỉ thuê thì hiện giá tháng
                val priceText = when {
                    plant.isForSale && plant.priceSale != null && plant.priceSale > 0 ->
                        MoneyFormatter.format(plant.priceSale)
                    plant.isForRent && plant.pricePerMonth != null ->
                        "${MoneyFormatter.formatShort(plant.pricePerMonth)}/tháng"
                    else -> "Liên hệ"
                }

                Text(
                    text = priceText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = plant.categoryName ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Badge nhỏ hiện loại cây: Bán / Thuê / Cả 2.
 */
@Composable
private fun ListingTypeBadge(
    listingType: String,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (listingType) {
        "sale" -> "Bán" to Color(0xFF388E3C)
        "rent" -> "Thuê" to Color(0xFF1976D2)
        "both" -> "Cả 2" to Color(0xFF6D4C41)
        else -> "" to Color.Gray
    }
    if (text.isEmpty()) return

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.9f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontWeight = FontWeight.Medium
        )
    }
}
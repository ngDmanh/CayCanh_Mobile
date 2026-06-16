package com.example.caycanh_mobile.ui.customer.returns

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Nhãn + màu cho từng trạng thái yêu cầu trả hàng */
data class ReturnStatusInfo(val label: String, val color: Color)

fun returnStatusInfo(status: String): ReturnStatusInfo = when (status) {
    "requested" -> ReturnStatusInfo("Chờ duyệt", Color(0xFFFF9800))
    "approved" -> ReturnStatusInfo("Đã duyệt", Color(0xFF2196F3))
    "rejected" -> ReturnStatusInfo("Bị từ chối", Color(0xFFE53935))
    "completed" -> ReturnStatusInfo("Đã hoàn tiền", Color(0xFF4CAF50))
    else -> ReturnStatusInfo(status, Color(0xFF9E9E9E))
}

@Composable
fun ReturnStatusBadge(status: String, modifier: Modifier = Modifier) {
    val info = returnStatusInfo(status)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = info.color.copy(alpha = 0.15f)
    ) {
        Text(
            info.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = info.color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Định dạng chuỗi thời gian ISO sang dd/MM/yyyy HH:mm */
fun formatReturnDate(isoString: String): String = try {
    val date = java.time.OffsetDateTime.parse(isoString)
    date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
} catch (e: Exception) {
    isoString.take(10)
}

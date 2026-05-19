package com.example.caycanh_mobile.ui.customer.home

import androidx.compose.ui.graphics.Color

/**
 * Banner hiển thị ở Home — hardcode tạm.
 * Sau có thể fetch từ API.
 */
data class HomeBanner(
    val title: String,
    val subtitle: String,
    val backgroundColor: Color,
    val textColor: Color
)

val defaultBanners = listOf(
    HomeBanner(
        title = "Chào mừng đến Cây Cảnh",
        subtitle = "Khám phá bộ sưu tập cây cảnh độc đáo",
        backgroundColor = Color(0xFFC8E6C9),
        textColor = Color(0xFF1B5E20)
    ),
    HomeBanner(
        title = "Thuê cây dễ dàng",
        subtitle = "Theo ngày, tuần, tháng — linh hoạt mọi nhu cầu",
        backgroundColor = Color(0xFFFFE0B2),
        textColor = Color(0xFF6D4C41)
    ),
    HomeBanner(
        title = "Bonsai nghệ thuật",
        subtitle = "Tinh hoa cây cảnh truyền thống Việt Nam",
        backgroundColor = Color(0xFFD7CCC8),
        textColor = Color(0xFF3E2723)
    )
)
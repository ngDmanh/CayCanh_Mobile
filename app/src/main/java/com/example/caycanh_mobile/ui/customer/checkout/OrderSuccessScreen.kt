package com.example.caycanh_mobile.ui.customer.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.caycanh_mobile.ui.components.PrimaryButton

/**
 * Màn báo đặt hàng thành công.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSuccessScreen(
    onViewOrders: () -> Unit,
    onContinueShopping: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(100.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Đặt hàng thành công!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Cảm ơn bạn đã đặt hàng. Admin sẽ liên hệ xác nhận sớm qua Zalo hoặc điện thoại.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Card hướng dẫn chuyển khoản
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📱 Liên hệ Zalo: 0901234567",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6D4C41)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Vui lòng giữ máy để admin gọi xác nhận đơn hàng.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6D4C41)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            PrimaryButton(
                text = "Xem đơn hàng",
                onClick = onViewOrders
            )

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onContinueShopping,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Tiếp tục mua sắm")
            }
        }
    }
}
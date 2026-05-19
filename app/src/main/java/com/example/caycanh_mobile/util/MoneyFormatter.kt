package com.example.caycanh_mobile.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Format tiền VND có dấu phẩy hàng nghìn.
 * 150000 → "150.000₫"
 */
object MoneyFormatter {

    private val formatter = NumberFormat.getInstance(Locale("vi", "VN"))

    fun format(amount: Long): String = "${formatter.format(amount)}₫"

    fun format(amount: Double): String = "${formatter.format(amount.toLong())}₫"

    fun formatShort(amount: Long): String = when {
        amount >= 1_000_000 -> "${formatter.format(amount / 1_000_000)}tr"
        amount >= 1_000 -> "${formatter.format(amount / 1_000)}k"
        else -> formatter.format(amount)
    }
}
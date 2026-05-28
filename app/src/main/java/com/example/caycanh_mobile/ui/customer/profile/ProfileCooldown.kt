package com.example.caycanh_mobile.ui.customer.profile

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/**
 * Tính số giờ còn lại cho đến khi được phép sửa profile tiếp.
 * Trả về 0 nếu được sửa ngay, hoặc số giờ phải đợi (1-24).
 */
fun hoursUntilCanEditProfile(lastUpdatedAtIso: String?): Long {
    if (lastUpdatedAtIso == null) return 0
    return try {
        val lastUpdate = OffsetDateTime.parse(lastUpdatedAtIso)
        val now = OffsetDateTime.now(lastUpdate.offset)
        val hoursSince = ChronoUnit.HOURS.between(lastUpdate, now)
        if (hoursSince >= 24) 0 else (24 - hoursSince)
    } catch (e: Exception) {
        0
    }
}

fun canEditProfile(lastUpdatedAtIso: String?): Boolean {
    return hoursUntilCanEditProfile(lastUpdatedAtIso) == 0L
}
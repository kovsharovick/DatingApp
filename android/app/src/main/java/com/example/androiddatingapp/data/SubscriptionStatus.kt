package com.example.androiddatingapp.data

import com.example.androiddatingapp.data.api.dto.SubscriptionDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object SubscriptionStatus {

    fun isPremiumActive(sub: SubscriptionDto): Boolean =
        sub.effectivePlan.equals("PREMIUM", ignoreCase = true) && isNotExpired(sub.expiresAt)

    fun isNotExpired(expiresAt: String?): Boolean {
        if (expiresAt.isNullOrBlank() || expiresAt.equals("never", ignoreCase = true)) {
            return true
        }
        return parseDateTime(expiresAt)?.isAfter(LocalDateTime.now()) != false
    }

    fun formatExpiresLabel(expiresAt: String?): String? {
        if (expiresAt.isNullOrBlank() || expiresAt.equals("never", ignoreCase = true)) {
            return null
        }
        val dt = parseDateTime(expiresAt) ?: return expiresAt.take(10)
        return dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }

    private fun parseDateTime(raw: String): LocalDateTime? {
        val normalized = raw.trim().replace(' ', 'T').let {
            if (it.length >= 19) it.substring(0, 19) else it
        }
        return try {
            LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}

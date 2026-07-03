package com.expenseit.core.model

import androidx.compose.ui.graphics.Color

/**
 * Fixed set of expense categories — mirrors the web app's Categories list.
 * Keeping it fixed means AI auto-categorization (future) is constrained to known values.
 */
enum class Category(
    val displayName: String,
    val pillBackground: Color,
    val pillForeground: Color,
    val emoji: String
) {
    FOOD("Food", Color(0xFFFFF7ED), Color(0xFFC2410C), "🍔"),
    GROCERIES("Groceries", Color(0xFFF7FEE7), Color(0xFF4D7C0F), "🛒"),
    TRANSPORT("Transport", Color(0xFFECFEFF), Color(0xFF0E7490), "🚗"),
    TRAVEL("Travel", Color(0xFFF0F9FF), Color(0xFF0369A1), "✈️"),
    SHOPPING("Shopping", Color(0xFFF5F3FF), Color(0xFF6D28D9), "🛍️"),
    BILLS("Bills", Color(0xFFFFF1F2), Color(0xFFBE123C), "📄"),
    ENTERTAINMENT("Entertainment", Color(0xFFFDF4FF), Color(0xFFA21CAF), "🎬"),
    HEALTH("Health", Color(0xFFECFDF5), Color(0xFF047857), "💊"),
    OTHER("Other", Color(0xFFF1F5F9), Color(0xFF475569), "📋");

    companion object {
        fun fromString(value: String): Category {
            return entries.find { it.displayName.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

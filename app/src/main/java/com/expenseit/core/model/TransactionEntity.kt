package com.expenseit.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core domain entity for personal expense tracking.
 * Amounts are stored as Long minor units (paise) — never floats.
 * Mirrors the web app's Transaction struct in models.go.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amountMinor: Long,           // paise: 125050 = ₹1,250.50
    val currency: String = "INR",
    val merchant: String,
    val description: String = "",
    val category: String,            // Category enum name
    val txnDate: Long,               // epoch millis
    val isAnomaly: Boolean = false,  // AI hook (future)
    val anomalyReason: String = "",  // AI hook (future)
    val createdAt: Long              // epoch millis
)

package com.expenseit.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Records a debt settlement between two group members.
 */
@Entity(tableName = "settlements")
data class SettlementEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val fromMemberId: String,        // who paid
    val toMemberId: String,          // who received
    val amountMinor: Long,           // paise
    val settledAt: Long              // epoch millis
)

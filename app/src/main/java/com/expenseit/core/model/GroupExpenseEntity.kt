package com.expenseit.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A shared expense within a group.
 * paidByMemberId identifies who paid.
 * splitType determines how the expense is divided.
 */
@Entity(tableName = "group_expenses")
data class GroupExpenseEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val description: String,
    val amountMinor: Long,              // paise
    val currency: String = "INR",
    val paidByMemberId: String,         // GroupMemberEntity.id
    val splitType: String = "EQUAL",    // "EQUAL", "EXACT", "PERCENTAGE"
    val category: String = "Other",
    val txnDate: Long,
    val createdAt: Long
)

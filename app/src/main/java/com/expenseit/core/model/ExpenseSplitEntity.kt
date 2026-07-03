package com.expenseit.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * How a single group expense is split among members.
 * Each record represents one member's share of one expense.
 */
@Entity(tableName = "expense_splits")
data class ExpenseSplitEntity(
    @PrimaryKey val id: String,
    val expenseId: String,           // GroupExpenseEntity.id
    val memberId: String,            // GroupMemberEntity.id
    val shareMinor: Long             // this member's share in paise
)

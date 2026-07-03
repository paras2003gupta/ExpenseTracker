package com.expenseit.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A group for splitting expenses (e.g., "Trip to Goa", "Roommates").
 */
@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val createdAt: Long
)

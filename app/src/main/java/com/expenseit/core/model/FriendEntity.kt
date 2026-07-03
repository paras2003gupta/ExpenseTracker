package com.expenseit.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Friend profile — stored locally. Each friend can be added to groups.
 * Phone number is used for WhatsApp message integration.
 */
@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String = "",          // for WhatsApp integration
    val avatarColorIndex: Int = 0,   // index into a predefined color palette
    val createdAt: Long
)

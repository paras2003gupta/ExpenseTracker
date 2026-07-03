package com.expenseit.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A member within a split group. Links to a friend profile via friendId.
 * isCurrentUser marks "You" in the group.
 */
@Entity(tableName = "group_members")
data class GroupMemberEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val friendId: String = "",       // links to FriendEntity.id (empty for "You")
    val name: String,
    val isCurrentUser: Boolean = false
)

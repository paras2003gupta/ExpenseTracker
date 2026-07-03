package com.expenseit.feature.splitter.ui.groups

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expenseit.ExpenseItApplication
import com.expenseit.core.model.FriendEntity
import com.expenseit.core.model.GroupEntity
import com.expenseit.core.model.GroupMemberEntity
import com.expenseit.core.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class GroupsState(
    val groups: List<GroupWithBalance> = emptyList(),
    val friends: List<FriendEntity> = emptyList(),
    val showCreateSheet: Boolean = false
)

data class GroupWithBalance(
    val group: GroupEntity,
    val memberCount: Int,
    val yourBalance: Long // Positive = owed to you, negative = you owe
)

class GroupsViewModel(application: Application) : ViewModel() {

    private val app = application as ExpenseItApplication
    private val repository = app.splitterRepository

    private val _uiState = MutableStateFlow(GroupsState())
    val uiState: StateFlow<GroupsState> = _uiState.asStateFlow()

    init {
        // Load groups
        viewModelScope.launch {
            repository.getAllGroups().collectLatest { groupsList ->
                val listWithBalances = groupsList.map { group ->
                    val members = repository.getMembersByGroupOnce(group.id)
                    val balances = repository.calculateBalances(group.id)
                    
                    // Find current user's member ID in this group
                    val currentUserMember = members.find { it.isCurrentUser }
                    val userBalance = currentUserMember?.let { balances[it.id] } ?: 0L

                    GroupWithBalance(
                        group = group,
                        memberCount = members.size,
                        yourBalance = userBalance
                    )
                }
                _uiState.value = _uiState.value.copy(groups = listWithBalances)
            }
        }

        // Load friends
        viewModelScope.launch {
            repository.getAllFriends().collectLatest { friendsList ->
                _uiState.value = _uiState.value.copy(friends = friendsList)
            }
        }
    }

    fun createGroup(name: String, selectedFriendIds: List<String>) {
        viewModelScope.launch {
            val groupId = DateUtils.newId()
            val group = GroupEntity(
                id = groupId,
                name = name.trim(),
                createdAt = DateUtils.now()
            )
            repository.insertGroup(group)

            // Add "You" (current user) as a member
            val currentUserMember = GroupMemberEntity(
                id = DateUtils.newId(),
                groupId = groupId,
                name = "You",
                isCurrentUser = true
            )
            repository.insertMember(currentUserMember)

            // Add selected friends as members
            val selectedFriends = repository.getFriendsByIds(selectedFriendIds)
            selectedFriends.forEach { friend ->
                val member = GroupMemberEntity(
                    id = DateUtils.newId(),
                    groupId = groupId,
                    friendId = friend.id,
                    name = friend.name,
                    isCurrentUser = false
                )
                repository.insertMember(member)
            }

            toggleCreateSheet(false)
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            repository.deleteGroupFull(groupId)
        }
    }

    fun addFriendQuickly(name: String, phone: String, onAdded: (String) -> Unit) {
        viewModelScope.launch {
            val newFriendId = DateUtils.newId()
            val newFriend = FriendEntity(
                id = newFriendId,
                name = name.trim(),
                phone = phone.trim(),
                avatarColorIndex = (0..7).random(),
                createdAt = DateUtils.now()
            )
            repository.insertFriend(newFriend)
            onAdded(newFriendId)
        }
    }

    fun toggleCreateSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreateSheet = show)
    }
}

class GroupsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

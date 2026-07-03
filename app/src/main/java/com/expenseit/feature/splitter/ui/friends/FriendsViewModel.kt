package com.expenseit.feature.splitter.ui.friends

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expenseit.ExpenseItApplication
import com.expenseit.core.model.FriendEntity
import com.expenseit.core.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

data class FriendsState(
    val friends: List<FriendEntity> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingFriend: FriendEntity? = null
)

class FriendsViewModel(application: Application) : ViewModel() {

    private val app = application as ExpenseItApplication
    private val repository = app.splitterRepository

    private val _uiState = MutableStateFlow(FriendsState())
    val uiState: StateFlow<FriendsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllFriends().collectLatest { list ->
                _uiState.value = _uiState.value.copy(friends = list)
            }
        }
    }

    fun addFriend(name: String, phone: String) {
        viewModelScope.launch {
            val friend = FriendEntity(
                id = DateUtils.newId(),
                name = name.trim(),
                phone = phone.trim(),
                avatarColorIndex = Random.nextInt(10),
                createdAt = DateUtils.now()
            )
            repository.insertFriend(friend)
            hideAddDialog()
        }
    }

    fun updateFriend(friend: FriendEntity, name: String, phone: String) {
        viewModelScope.launch {
            val updated = friend.copy(
                name = name.trim(),
                phone = phone.trim()
            )
            repository.updateFriend(updated)
            clearEditingFriend()
        }
    }

    fun deleteFriend(id: String) {
        viewModelScope.launch {
            repository.deleteFriend(id)
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun setEditingFriend(friend: FriendEntity?) {
        _uiState.value = _uiState.value.copy(editingFriend = friend)
    }

    fun clearEditingFriend() {
        _uiState.value = _uiState.value.copy(editingFriend = null)
    }
}

class FriendsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FriendsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

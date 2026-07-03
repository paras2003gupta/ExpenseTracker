package com.expenseit.feature.splitter.ui.group_detail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expenseit.ExpenseItApplication
import com.expenseit.core.model.ExpenseSplitEntity
import com.expenseit.core.model.FriendEntity
import com.expenseit.core.model.GroupEntity
import com.expenseit.core.model.GroupExpenseEntity
import com.expenseit.core.model.GroupMemberEntity
import com.expenseit.core.model.SettlementEntity
import com.expenseit.core.model.TransactionEntity
import com.expenseit.core.util.DateUtils
import com.expenseit.core.util.MoneyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class GroupDetailState(
    val group: GroupEntity? = null,
    val members: List<GroupMemberEntity> = emptyList(),
    val expenses: List<GroupExpenseEntity> = emptyList(),
    val balances: Map<String, Long> = emptyMap(), // memberId -> balance
    val simplifiedDebts: List<Triple<String, String, Long>> = emptyList(), // fromMemberId, toMemberId, amount
    val showAddExpense: Boolean = false,
    val showSettleUp: Boolean = false,
    val friendProfiles: Map<String, FriendEntity> = emptyMap(), // friendId -> FriendEntity
    val allFriends: List<FriendEntity> = emptyList(),
    val showAddMemberDialog: Boolean = false
)

class GroupDetailViewModel(
    application: Application,
    private val groupId: String
) : ViewModel() {

    private val app = application as ExpenseItApplication
    private val repository = app.splitterRepository

    private val _uiState = MutableStateFlow(GroupDetailState())
    val uiState: StateFlow<GroupDetailState> = _uiState.asStateFlow()

    init {
        loadGroupData()
    }

    private fun loadGroupData() {
        viewModelScope.launch {
            val groupInfo = repository.getGroupById(groupId)
            _uiState.value = _uiState.value.copy(group = groupInfo)
        }

        // Collect members
        viewModelScope.launch {
            repository.getMembersByGroup(groupId).collectLatest { memberList ->
                _uiState.value = _uiState.value.copy(members = memberList)
                loadFriendProfiles(memberList)
                recalculateBalancesAndDebts()
            }
        }

        // Collect all friends
        viewModelScope.launch {
            repository.getAllFriends().collectLatest { friendsList ->
                _uiState.value = _uiState.value.copy(allFriends = friendsList)
            }
        }

        // Collect expenses
        viewModelScope.launch {
            repository.getExpensesByGroup(groupId).collectLatest { expenseList ->
                _uiState.value = _uiState.value.copy(expenses = expenseList)
                recalculateBalancesAndDebts()
            }
        }

        // Collect settlements
        viewModelScope.launch {
            repository.getSettlementsByGroup(groupId).collectLatest {
                recalculateBalancesAndDebts()
            }
        }
    }

    private fun loadFriendProfiles(members: List<GroupMemberEntity>) {
        viewModelScope.launch {
            val friendIds = members.map { it.friendId }.filter { it.isNotEmpty() }
            if (friendIds.isNotEmpty()) {
                val friends = repository.getFriendsByIds(friendIds)
                val profilesMap = friends.associateBy { it.id }
                _uiState.value = _uiState.value.copy(friendProfiles = profilesMap)
            }
        }
    }

    private fun recalculateBalancesAndDebts() {
        viewModelScope.launch {
            val balances = repository.calculateBalances(groupId)
            val simplified = repository.simplifyDebts(groupId)
            _uiState.value = _uiState.value.copy(
                balances = balances,
                simplifiedDebts = simplified
            )
        }
    }

    fun addExpense(
        description: String,
        amountRupees: Double,
        paidByMemberId: String,
        category: String
    ) {
        viewModelScope.launch {
            val totalMinor = MoneyFormatter.rupeesToMinor(amountRupees)
            val expenseId = DateUtils.newId()
            val now = DateUtils.now()

            val expense = GroupExpenseEntity(
                id = expenseId,
                groupId = groupId,
                description = description.trim(),
                amountMinor = totalMinor,
                paidByMemberId = paidByMemberId,
                splitType = "EQUAL",
                category = category,
                txnDate = now,
                createdAt = now
            )

            // Equal Split calculation
            val membersList = _uiState.value.members
            val count = membersList.size
            if (count > 0) {
                val baseShare = totalMinor / count
                val remainder = totalMinor - (baseShare * count)

                val splits = membersList.mapIndexed { index, member ->
                    // Add remainder to the first split to ensure exact sum matches
                    val share = if (index == 0) baseShare + remainder else baseShare
                    ExpenseSplitEntity(
                        id = DateUtils.newId(),
                        expenseId = expenseId,
                        memberId = member.id,
                        shareMinor = share
                    )
                }

                repository.insertExpenseWithSplits(expense, splits)

                // Track current user's split share in personal transactions
                val groupEntity = _uiState.value.group
                val currentUserMember = membersList.find { it.isCurrentUser }
                if (currentUserMember != null && groupEntity != null) {
                    val userSplit = splits.find { it.memberId == currentUserMember.id }
                    if (userSplit != null) {
                        val personalTxn = TransactionEntity(
                            id = "split_${userSplit.id}",
                            amountMinor = userSplit.shareMinor,
                            currency = "INR",
                            merchant = groupEntity.name,
                            description = "Share: ${description.trim()}",
                            category = category,
                            txnDate = now,
                            createdAt = now
                        )
                        app.transactionRepository.create(personalTxn)
                    }
                }
            }

            toggleAddExpense(false)
        }
    }

    fun recordSettlement(
        fromMemberId: String,
        toMemberId: String,
        amountRupees: Double
    ) {
        viewModelScope.launch {
            val amountMinor = MoneyFormatter.rupeesToMinor(amountRupees)
            val settlement = SettlementEntity(
                id = DateUtils.newId(),
                groupId = groupId,
                fromMemberId = fromMemberId,
                toMemberId = toMemberId,
                amountMinor = amountMinor,
                settledAt = DateUtils.now()
            )
            repository.insertSettlement(settlement)
            toggleSettleUp(false)
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            val splits = repository.getSplitsByExpense(expenseId)
            val membersList = _uiState.value.members
            val currentUserMember = membersList.find { it.isCurrentUser }
            if (currentUserMember != null) {
                val userSplit = splits.find { it.memberId == currentUserMember.id }
                if (userSplit != null) {
                    app.transactionRepository.deleteById("split_${userSplit.id}")
                }
            }
            repository.deleteExpense(expenseId)
        }
    }

    fun toggleAddExpense(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddExpense = show)
    }

    fun toggleSettleUp(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSettleUp = show)
    }

    fun toggleAddMember(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddMemberDialog = show)
    }

    fun addMembersToGroup(friendIds: List<String>) {
        viewModelScope.launch {
            val selectedFriends = repository.getFriendsByIds(friendIds)
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
            recalculateBalancesAndDebts()
            toggleAddMember(false)
        }
    }
}

class GroupDetailViewModelFactory(
    private val application: Application,
    private val groupId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupDetailViewModel(application, groupId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

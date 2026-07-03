package com.expenseit.feature.tracker.ui.transactions

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expenseit.ExpenseItApplication
import com.expenseit.core.model.Category
import com.expenseit.core.model.TransactionEntity
import com.expenseit.core.model.GroupEntity
import com.expenseit.core.model.GroupExpenseEntity
import com.expenseit.core.model.ExpenseSplitEntity
import com.expenseit.core.util.DateUtils
import com.expenseit.core.util.MoneyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class TransactionsState(
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: List<String> = Category.entries.map { it.displayName },
    val selectedCategory: String = "",
    val showAddSheet: Boolean = false,
    val groups: List<GroupEntity> = emptyList()
)

class TransactionsViewModel(application: Application) : ViewModel() {

    private val app = application as ExpenseItApplication
    private val repository = app.transactionRepository

    private val _uiState = MutableStateFlow(TransactionsState())
    val uiState: StateFlow<TransactionsState> = _uiState.asStateFlow()

    init {
        loadTransactions()
        viewModelScope.launch {
            app.splitterRepository.getAllGroups().collectLatest { groupsList ->
                _uiState.value = _uiState.value.copy(groups = groupsList)
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getAllTransactions().collectLatest { allTxns ->
                filterAndEmit(allTxns, _uiState.value.selectedCategory)
            }
        }
    }

    fun setCategory(category: String) {
        val currentCategory = if (category == "All") "" else category
        viewModelScope.launch {
            repository.getAllTransactions().collectLatest { allTxns ->
                filterAndEmit(allTxns, currentCategory)
            }
        }
    }

    private fun filterAndEmit(allTxns: List<TransactionEntity>, category: String) {
        val filtered = if (category.isEmpty()) {
            allTxns
        } else {
            allTxns.filter { it.category.equals(category, ignoreCase = true) }
        }
        _uiState.value = _uiState.value.copy(
            transactions = filtered,
            selectedCategory = category
        )
    }

    fun createTransaction(
        merchant: String,
        description: String,
        amountRupees: Double,
        category: String,
        dateMillis: Long,
        splitGroupId: String? = null
    ) {
        viewModelScope.launch {
            val amountMinor = MoneyFormatter.rupeesToMinor(amountRupees)
            
            if (!splitGroupId.isNullOrEmpty()) {
                val expenseId = DateUtils.newId()
                val membersList = app.splitterRepository.getMembersByGroupOnce(splitGroupId)
                val currentUserMember = membersList.find { it.isCurrentUser }
                
                if (currentUserMember != null) {
                    val groupExpense = GroupExpenseEntity(
                        id = expenseId,
                        groupId = splitGroupId,
                        description = merchant.trim() + (if (description.isNotEmpty()) " - $description" else ""),
                        amountMinor = amountMinor,
                        paidByMemberId = currentUserMember.id,
                        splitType = "EQUAL",
                        category = category,
                        txnDate = dateMillis,
                        createdAt = DateUtils.now()
                    )

                    val count = membersList.size
                    if (count > 0) {
                        val baseShare = amountMinor / count
                        val remainder = amountMinor - (baseShare * count)

                        val splits = membersList.mapIndexed { index, member ->
                            val share = if (index == 0) baseShare + remainder else baseShare
                            ExpenseSplitEntity(
                                id = DateUtils.newId(),
                                expenseId = expenseId,
                                memberId = member.id,
                                shareMinor = share
                            )
                        }

                        app.splitterRepository.insertExpenseWithSplits(groupExpense, splits)

                        // Add personal transaction representing user's share
                        val userSplit = splits.find { it.memberId == currentUserMember.id }
                        val groupEntity = app.splitterRepository.getGroupById(splitGroupId)
                        if (userSplit != null && groupEntity != null) {
                            val personalTxn = TransactionEntity(
                                id = "split_${userSplit.id}",
                                amountMinor = userSplit.shareMinor,
                                currency = "INR",
                                merchant = groupEntity.name,
                                description = "Share: " + merchant.trim() + (if (description.isNotEmpty()) " - $description" else ""),
                                category = category,
                                txnDate = dateMillis,
                                createdAt = DateUtils.now()
                            )
                            repository.create(personalTxn)
                        }
                    }
                }
            } else {
                val txn = TransactionEntity(
                    id = DateUtils.newId(),
                    amountMinor = amountMinor,
                    currency = "INR",
                    merchant = merchant.trim(),
                    description = description.trim(),
                    category = if (category.isEmpty()) "Other" else category,
                    txnDate = dateMillis,
                    createdAt = DateUtils.now()
                )
                repository.create(txn)
            }
            toggleAddSheet(false)
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun splitExistingTransaction(txn: TransactionEntity, groupId: String) {
        viewModelScope.launch {
            val membersList = app.splitterRepository.getMembersByGroupOnce(groupId)
            val currentUserMember = membersList.find { it.isCurrentUser }

            if (currentUserMember != null) {
                val expenseId = DateUtils.newId()
                val groupExpense = GroupExpenseEntity(
                    id = expenseId,
                    groupId = groupId,
                    description = txn.merchant + (if (txn.description.isNotEmpty()) " - ${txn.description}" else ""),
                    amountMinor = txn.amountMinor,
                    paidByMemberId = currentUserMember.id,
                    splitType = "EQUAL",
                    category = txn.category,
                    txnDate = txn.txnDate,
                    createdAt = DateUtils.now()
                )

                val count = membersList.size
                if (count > 0) {
                    val baseShare = txn.amountMinor / count
                    val remainder = txn.amountMinor - (baseShare * count)

                    val splits = membersList.mapIndexed { index, member ->
                        val share = if (index == 0) baseShare + remainder else baseShare
                        ExpenseSplitEntity(
                            id = DateUtils.newId(),
                            expenseId = expenseId,
                            memberId = member.id,
                            shareMinor = share
                        )
                    }

                    app.splitterRepository.insertExpenseWithSplits(groupExpense, splits)

                    // Delete original personal transaction
                    repository.deleteById(txn.id)

                    // Add personal transaction representing user's share
                    val userSplit = splits.find { it.memberId == currentUserMember.id }
                    val groupEntity = app.splitterRepository.getGroupById(groupId)
                    if (userSplit != null && groupEntity != null) {
                        val personalTxn = TransactionEntity(
                            id = "split_${userSplit.id}",
                            amountMinor = userSplit.shareMinor,
                            currency = "INR",
                            merchant = groupEntity.name,
                            description = "Share: " + txn.merchant + (if (txn.description.isNotEmpty()) " - ${txn.description}" else ""),
                            category = txn.category,
                            txnDate = txn.txnDate,
                            createdAt = DateUtils.now()
                        )
                        repository.create(personalTxn)
                    }
                }
            }
        }
    }

    fun toggleAddSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddSheet = show)
    }
}

class TransactionsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

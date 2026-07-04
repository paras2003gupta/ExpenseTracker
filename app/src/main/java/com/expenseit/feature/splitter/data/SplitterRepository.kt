package com.expenseit.feature.splitter.data

import com.expenseit.core.model.ExpenseSplitEntity
import com.expenseit.core.model.FriendEntity
import com.expenseit.core.model.GroupEntity
import com.expenseit.core.model.GroupExpenseEntity
import com.expenseit.core.model.GroupMemberEntity
import com.expenseit.core.model.SettlementEntity
import com.expenseit.core.util.DateUtils
import kotlinx.coroutines.flow.Flow

/**
 * Repository for expense splitting: groups, members, expenses, splits, settlements.
 */
class SplitterRepository(
    private val groupDao: GroupDao,
    private val friendDao: FriendDao
) {
    // --- Friends ---
    fun getAllFriends(): Flow<List<FriendEntity>> = friendDao.getAll()
    suspend fun getAllFriendsOnce(): List<FriendEntity> = friendDao.getAllOnce()
    suspend fun getFriendById(id: String): FriendEntity? = friendDao.getById(id)
    suspend fun getFriendsByIds(ids: List<String>): List<FriendEntity> = friendDao.getByIds(ids)
    suspend fun insertFriend(friend: FriendEntity) = friendDao.insert(friend)
    suspend fun updateFriend(friend: FriendEntity) = friendDao.update(friend)
    suspend fun deleteFriend(id: String) = friendDao.deleteById(id)

    // --- Groups ---
    fun getAllGroups(): Flow<List<GroupEntity>> = groupDao.getAllGroups()
    suspend fun getGroupById(id: String): GroupEntity? = groupDao.getGroupById(id)
    suspend fun insertGroup(group: GroupEntity) = groupDao.insertGroup(group)

    suspend fun deleteGroupFull(groupId: String) {
        groupDao.deleteSettlementsByGroup(groupId)
        // Delete splits for each expense
        val expenses = groupDao.getExpensesByGroupOnce(groupId)
        expenses.forEach { groupDao.deleteSplitsByExpense(it.id) }
        groupDao.deleteExpensesByGroup(groupId)
        groupDao.deleteMembersByGroup(groupId)
        groupDao.deleteGroup(groupId)
    }

    // --- Members ---
    fun getMembersByGroup(groupId: String): Flow<List<GroupMemberEntity>> =
        groupDao.getMembersByGroup(groupId)

    suspend fun getMembersByGroupOnce(groupId: String): List<GroupMemberEntity> =
        groupDao.getMembersByGroupOnce(groupId)

    suspend fun insertMember(member: GroupMemberEntity) = groupDao.insertMember(member)

    suspend fun getCurrentUserMember(groupId: String): GroupMemberEntity? =
        groupDao.getCurrentUserMember(groupId)

    // --- Expenses ---
    fun getExpensesByGroup(groupId: String): Flow<List<GroupExpenseEntity>> =
        groupDao.getExpensesByGroup(groupId)

    suspend fun getExpensesByGroupOnce(groupId: String): List<GroupExpenseEntity> =
        groupDao.getExpensesByGroupOnce(groupId)

    suspend fun insertExpenseWithSplits(
        expense: GroupExpenseEntity,
        splits: List<ExpenseSplitEntity>
    ) {
        groupDao.insertExpenseWithSplits(expense, splits)
    }

    suspend fun deleteExpense(expenseId: String) {
        groupDao.deleteSplitsByExpense(expenseId)
        groupDao.deleteExpense(expenseId)
    }

    // --- Splits ---
    suspend fun getSplitsByExpense(expenseId: String): List<ExpenseSplitEntity> =
        groupDao.getSplitsByExpense(expenseId)

    suspend fun getSplitsByExpenses(expenseIds: List<String>): List<ExpenseSplitEntity> {
        if (expenseIds.isEmpty()) return emptyList()
        return groupDao.getSplitsByExpenses(expenseIds)
    }

    // --- Settlements ---
    fun getSettlementsByGroup(groupId: String): Flow<List<SettlementEntity>> =
        groupDao.getSettlementsByGroup(groupId)

    suspend fun getSettlementsByGroupOnce(groupId: String): List<SettlementEntity> =
        groupDao.getSettlementsByGroupOnce(groupId)

    suspend fun insertSettlement(settlement: SettlementEntity) =
        groupDao.insertSettlement(settlement)

    // --- Balance Calculation ---
    /**
     * Calculate net balance for each member in a group.
     * Positive balance = others owe them. Negative = they owe others.
     * Takes into account settlements already made.
     */
    suspend fun calculateBalances(groupId: String): Map<String, Long> {
        val members = getMembersByGroupOnce(groupId)
        val expenses = getExpensesByGroupOnce(groupId)
        val settlements = getSettlementsByGroupOnce(groupId)
        val splits = getSplitsByExpenses(expenses.map { it.id })

        // Initialize balances
        val balances = mutableMapOf<String, Long>()
        members.forEach { balances[it.id] = 0L }

        // For each expense: payer's balance increases, each member's share decreases their balance
        for (expense in expenses) {
            balances[expense.paidByMemberId] =
                (balances[expense.paidByMemberId] ?: 0L) + expense.amountMinor

            val expenseSplits = splits.filter { it.expenseId == expense.id }
            for (split in expenseSplits) {
                balances[split.memberId] =
                    (balances[split.memberId] ?: 0L) - split.shareMinor
            }
        }

        // Apply settlements
        for (settlement in settlements) {
            balances[settlement.fromMemberId] =
                (balances[settlement.fromMemberId] ?: 0L) + settlement.amountMinor
            balances[settlement.toMemberId] =
                (balances[settlement.toMemberId] ?: 0L) - settlement.amountMinor
        }

        return balances
    }

    /**
     * Simplify debts — greedy algorithm to minimize number of transactions.
     * Returns list of (fromMemberId, toMemberId, amount) triples.
     */
    suspend fun simplifyDebts(groupId: String): List<Triple<String, String, Long>> {
        val balances = calculateBalances(groupId)
        val result = mutableListOf<Triple<String, String, Long>>()

        // Separate into creditors (+) and debtors (-)
        val creditors = balances.filter { it.value > 0 }.toMutableMap()
        val debtors = balances.filter { it.value < 0 }.toMutableMap()

        while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
            val maxCreditor = creditors.maxByOrNull { it.value } ?: break
            val maxDebtor = debtors.minByOrNull { it.value } ?: break

            val amount = minOf(maxCreditor.value, -maxDebtor.value)
            if (amount > 0) {
                result.add(Triple(maxDebtor.key, maxCreditor.key, amount))
            }

            creditors[maxCreditor.key] = maxCreditor.value - amount
            debtors[maxDebtor.key] = maxDebtor.value + amount

            if (creditors[maxCreditor.key] == 0L) creditors.remove(maxCreditor.key)
            if (debtors[maxDebtor.key] == 0L) debtors.remove(maxDebtor.key)
        }

        return result
    }
}

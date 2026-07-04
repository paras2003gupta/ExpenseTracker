package com.expenseit.feature.splitter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import com.expenseit.core.model.ExpenseSplitEntity
import com.expenseit.core.model.GroupEntity
import com.expenseit.core.model.GroupExpenseEntity
import com.expenseit.core.model.GroupMemberEntity
import com.expenseit.core.model.SettlementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    // --- Groups ---
    @Insert
    suspend fun insertGroup(group: GroupEntity)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE id = :id")
    suspend fun deleteGroup(id: String)

    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupById(id: String): GroupEntity?

    // --- Group Members ---
    @Insert
    suspend fun insertMember(member: GroupMemberEntity)

    @Query("DELETE FROM group_members WHERE id = :id")
    suspend fun deleteMember(id: String)

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    fun getMembersByGroup(groupId: String): Flow<List<GroupMemberEntity>>

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    suspend fun getMembersByGroupOnce(groupId: String): List<GroupMemberEntity>

    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserMember(groupId: String): GroupMemberEntity?

    // --- Group Expenses ---
    @Insert
    suspend fun insertExpense(expense: GroupExpenseEntity)

    @Transaction
    suspend fun insertExpenseWithSplits(
        expense: GroupExpenseEntity,
        splits: List<ExpenseSplitEntity>
    ) {
        insertExpense(expense)
        splits.forEach { insertSplit(it) }
    }

    @Query("DELETE FROM group_expenses WHERE id = :id")
    suspend fun deleteExpense(id: String)

    @Query("SELECT * FROM group_expenses WHERE groupId = :groupId ORDER BY txnDate DESC")
    fun getExpensesByGroup(groupId: String): Flow<List<GroupExpenseEntity>>

    @Query("SELECT * FROM group_expenses WHERE groupId = :groupId ORDER BY txnDate DESC")
    suspend fun getExpensesByGroupOnce(groupId: String): List<GroupExpenseEntity>

    // --- Expense Splits ---
    @Insert
    suspend fun insertSplit(split: ExpenseSplitEntity)

    @Query("SELECT * FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun getSplitsByExpense(expenseId: String): List<ExpenseSplitEntity>

    @Query("SELECT * FROM expense_splits WHERE expenseId IN (:expenseIds)")
    suspend fun getSplitsByExpenses(expenseIds: List<String>): List<ExpenseSplitEntity>

    @Query("DELETE FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun deleteSplitsByExpense(expenseId: String)

    // --- Settlements ---
    @Insert
    suspend fun insertSettlement(settlement: SettlementEntity)

    @Query("SELECT * FROM settlements WHERE groupId = :groupId ORDER BY settledAt DESC")
    fun getSettlementsByGroup(groupId: String): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM settlements WHERE groupId = :groupId")
    suspend fun getSettlementsByGroupOnce(groupId: String): List<SettlementEntity>

    // --- Cleanup ---
    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun deleteMembersByGroup(groupId: String)

    @Query("DELETE FROM group_expenses WHERE groupId = :groupId")
    suspend fun deleteExpensesByGroup(groupId: String)

    @Query("DELETE FROM settlements WHERE groupId = :groupId")
    suspend fun deleteSettlementsByGroup(groupId: String)
}

package com.expenseit.feature.tracker.data

import com.expenseit.core.model.TransactionEntity
import com.expenseit.core.util.DateUtils
import kotlinx.coroutines.flow.Flow

/**
 * Repository for personal expense transactions.
 * Mirrors web app's TransactionRepository interface from repository.go.
 */
class TransactionRepository(private val dao: TransactionDao) {

    fun getAllTransactions(): Flow<List<TransactionEntity>> = dao.getAll()

    fun getByCategory(category: String): Flow<List<TransactionEntity>> = dao.getByCategory(category)

    fun getByDateRange(start: Long, end: Long): Flow<List<TransactionEntity>> =
        dao.getByDateRange(start, end)

    fun getByCategoryAndDateRange(category: String, start: Long, end: Long): Flow<List<TransactionEntity>> =
        dao.getByCategoryAndDateRange(category, start, end)

    suspend fun getById(id: String): TransactionEntity? = dao.getById(id)

    suspend fun create(transaction: TransactionEntity) = dao.insert(transaction)

    suspend fun update(transaction: TransactionEntity) = dao.update(transaction)

    suspend fun deleteById(id: String) = dao.deleteById(id)

    suspend fun getCount(): Int = dao.getCount()

    /**
     * Seed demo data matching web app's main.go seed function.
     */
    suspend fun seedIfEmpty() {
        if (getCount() > 0) return

        val now = DateUtils.now()

        // Current month transactions
        create(TransactionEntity(DateUtils.newId(), 48900, "INR", "Swiggy", "Dinner order", "Food", DateUtils.thisMonth(3), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 67200, "INR", "Uber", "Airport ride", "Transport", DateUtils.thisMonth(5), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 312000, "INR", "BigBasket", "Weekly groceries", "Groceries", DateUtils.thisMonth(6), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 249900, "INR", "Amazon", "Wireless headphones", "Shopping", DateUtils.thisMonth(8), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 145600, "INR", "Tata Power", "Electricity bill", "Bills", DateUtils.thisMonth(10), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 64900, "INR", "Netflix", "Monthly subscription", "Entertainment", DateUtils.thisMonth(11), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 38500, "INR", "Zomato", "Team lunch", "Food", DateUtils.thisMonth(14), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 89000, "INR", "Apollo Pharmacy", "Medicines", "Health", DateUtils.thisMonth(16), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 95000, "INR", "BookMyShow", "Movie tickets", "Entertainment", DateUtils.thisMonth(18), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 47500, "INR", "Starbucks", "Coffee", "Food", DateUtils.thisMonth(20), createdAt = now))

        // Previous month (for MoM comparison)
        create(TransactionEntity(DateUtils.newId(), 41200, "INR", "Swiggy", "Dinner order", "Food", DateUtils.lastMonth(4), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 129900, "INR", "Amazon", "Phone case", "Shopping", DateUtils.lastMonth(7), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 132400, "INR", "Tata Power", "Electricity bill", "Bills", DateUtils.lastMonth(10), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 28900, "INR", "Ola", "Cab ride", "Transport", DateUtils.lastMonth(12), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 287000, "INR", "BigBasket", "Groceries", "Groceries", DateUtils.lastMonth(15), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 64900, "INR", "Netflix", "Monthly subscription", "Entertainment", DateUtils.lastMonth(16), createdAt = now))
        create(TransactionEntity(DateUtils.newId(), 199900, "INR", "Cult.fit", "Gym membership", "Health", DateUtils.lastMonth(18), createdAt = now))
    }
}

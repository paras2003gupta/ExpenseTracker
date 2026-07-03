package com.expenseit

import android.app.Application
import com.expenseit.core.database.ParooDatabase
import com.expenseit.feature.splitter.data.SplitterRepository
import com.expenseit.feature.tracker.data.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class — provides database and repository singletons.
 * Manual DI pattern (simpler than Hilt for this project size, easily upgradable).
 */
class ExpenseItApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: ParooDatabase by lazy { ParooDatabase.getDatabase(this) }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(database.transactionDao())
    }

    val splitterRepository: SplitterRepository by lazy {
        SplitterRepository(database.groupDao(), database.friendDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Seed demo data on first launch
        applicationScope.launch {
            transactionRepository.seedIfEmpty()
        }
    }
}

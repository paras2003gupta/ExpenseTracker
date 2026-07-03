package com.expenseit.feature.tracker.ui.dashboard

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expenseit.ExpenseItApplication
import com.expenseit.core.model.Category
import com.expenseit.core.model.TransactionEntity
import com.expenseit.core.util.DateUtils
import com.expenseit.core.util.MoneyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DashboardState(
    val isLoading: Boolean = true,
    val monthLabel: String = "",
    val totalDisplay: String = "₹0.00",
    val totalMinor: Long = 0,
    val transactionCount: Int = 0,
    val topCategory: String = "—",
    val topCategoryAmount: String = "",
    val momChangePct: Double? = null,
    val categoryTotals: List<CategoryTotalUi> = emptyList(),
    val topMerchants: List<MerchantTotalUi> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val monthlyLimit: Double = 0.0
)

data class CategoryTotalUi(
    val category: String,
    val totalMinor: Long,
    val totalDisplay: String,
    val count: Int,
    val percentage: Float
)

data class MerchantTotalUi(
    val merchant: String,
    val totalMinor: Long,
    val totalDisplay: String,
    val count: Int
)

class DashboardViewModel(application: Application) : ViewModel() {

    private val app = application as ExpenseItApplication
    private val repository = app.transactionRepository
    private val prefs = app.getSharedPreferences("expenseit_prefs", android.content.Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTransactions().collectLatest { allTxns ->
                loadDashboard(allTxns)
            }
        }
    }

    private fun loadDashboard(allTxns: List<TransactionEntity>) {
        val now = DateUtils.now()
        val currentMonthLabel = DateUtils.formatMonthYear(now)

        // Time ranges (epoch millis)
        val currentStart = DateUtils.monthStart(0)
        val currentEnd = DateUtils.monthEnd(0)
        val prevStart = DateUtils.monthStart(-1)
        val prevEnd = DateUtils.monthEnd(-1)

        // Current month filter
        val currentMonthTxns = allTxns.filter { it.txnDate in currentStart..currentEnd }
        // Previous month filter
        val prevMonthTxns = allTxns.filter { it.txnDate in prevStart..prevEnd }

        // Sum amounts
        val currentTotal = currentMonthTxns.sumOf { it.amountMinor }
        val prevTotal = prevMonthTxns.sumOf { it.amountMinor }

        // MoM calculation (increase/decrease percentage)
        val momChangePct = if (prevTotal > 0) {
            ((currentTotal - prevTotal).toDouble() / prevTotal) * 100.0
        } else {
            null
        }

        // Category aggregation
        val categoryGroups = currentMonthTxns.groupBy { it.category }
        val categoryTotals = categoryGroups.map { (catName, txs) ->
            val minorSum = txs.sumOf { it.amountMinor }
            val pct = if (currentTotal > 0) (minorSum.toFloat() / currentTotal) * 100f else 0f
            CategoryTotalUi(
                category = catName,
                totalMinor = minorSum,
                totalDisplay = MoneyFormatter.formatINR(minorSum),
                count = txs.size,
                percentage = pct
            )
        }.sortedByDescending { it.totalMinor }

        // Merchant aggregation (top 5)
        val merchantGroups = currentMonthTxns.groupBy { it.merchant }
        val topMerchants = merchantGroups.map { (merch, txs) ->
            val minorSum = txs.sumOf { it.amountMinor }
            MerchantTotalUi(
                merchant = merch,
                totalMinor = minorSum,
                totalDisplay = MoneyFormatter.formatINR(minorSum),
                count = txs.size
            )
        }.sortedByDescending { it.totalMinor }.take(5)

        // Top category info
        val topCat = categoryTotals.firstOrNull()
        val topCategoryName = topCat?.category ?: "—"
        val topCategoryAmt = topCat?.totalDisplay ?: ""

        // Recent activity (first 6 transactions sorted desc by date)
        val recent = allTxns.take(6)

        val limit = prefs.getFloat("monthly_limit", 0f).toDouble()

        _uiState.value = DashboardState(
            isLoading = false,
            monthLabel = currentMonthLabel,
            totalDisplay = MoneyFormatter.formatINR(currentTotal),
            totalMinor = currentTotal,
            transactionCount = currentMonthTxns.size,
            topCategory = topCategoryName,
            topCategoryAmount = topCategoryAmt,
            momChangePct = momChangePct,
            categoryTotals = categoryTotals,
            topMerchants = topMerchants,
            recentTransactions = recent,
            monthlyLimit = limit
        )
    }

    fun setMonthlyLimit(limit: Double) {
        prefs.edit().putFloat("monthly_limit", limit.toFloat()).apply()
        viewModelScope.launch {
            repository.getAllTransactions().collectLatest { allTxns ->
                loadDashboard(allTxns)
            }
        }
    }
}

class DashboardViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

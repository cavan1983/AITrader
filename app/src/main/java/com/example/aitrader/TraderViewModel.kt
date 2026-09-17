package com.example.aitrader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale
import android.util.Log

data class PerformanceStats(
    val totalDecisions: Int = 0,
    val successfulDecisions: Int = 0,
    val failedDecisions: Int = 0,
    val accuracy: Double = 0.0,
    val dailyWinRatio: Double = 0.0,
    val timeframeAccuracy: Map<String, Double> = emptyMap(),
    val symbolStats: Map<String, SymbolPerformance> = emptyMap(),
    val buyAccuracy: Double = 0.0,
    val sellAccuracy: Double = 0.0,
    val holdAccuracy: Double = 0.0
)

data class SymbolPerformance(
    val total: Int,
    val success: Int,
    val accuracy: Double
)

class TraderViewModel(
    private val repository: TraderRepository
) : ViewModel() {

    val watchlist: StateFlow<List<WatchlistEntity>> = repository.watchlist
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLogs: StateFlow<List<DecisionLogEntity>> = repository.getAllLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val latestLogs: StateFlow<List<DecisionLogEntity>> = repository.getLatestLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val selectedSymbol = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedStock: StateFlow<WatchlistEntity?> = selectedSymbol
        .flatMapLatest { symbol ->
            if (symbol == null) flowOf(null)
            else repository.getStockBySymbol(symbol)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedStockLog: StateFlow<DecisionLogEntity?> = selectedSymbol
        .flatMapLatest { symbol ->
            if (symbol == null) flowOf(null)
            else repository.getLatestLogForSymbol(symbol)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val performanceStats: StateFlow<PerformanceStats> = allLogs.map { logs ->
        calculateDailyPerformance(logs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PerformanceStats()
    )

    private val _aiSelfEvaluation = MutableStateFlow<String>("Gündəlik analiz üçün yenilə düyməsinə klikləyin.")
    val aiSelfEvaluation: StateFlow<String> = _aiSelfEvaluation

    private val _isSelfEvaluationLoading = MutableStateFlow(false)
    val isSelfEvaluationLoading: StateFlow<Boolean> = _isSelfEvaluationLoading

    private val _marketStatusFlow = MutableStateFlow("")
    val marketStatusFlow: StateFlow<String> = _marketStatusFlow

    private val _analysisLoadingStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val analysisLoadingStates: StateFlow<Map<String, Boolean>> = _analysisLoadingStates

    private val _syncTimerFlow = MutableStateFlow(60)
    val syncTimerFlow: StateFlow<Int> = _syncTimerFlow

    private var updateJob: Job? = null

    init {
        startPeriodicPriceUpdates()
    }

    fun selectSymbol(symbol: String?) {
        selectedSymbol.value = symbol
    }

    private fun startPeriodicPriceUpdates() {
        if (updateJob?.isActive == true) return

        updateJob = viewModelScope.launch {
            while (isActive) {
                if (MarketUtils.isMarketOpen()) {
                    _marketStatusFlow.value = "MARKET_OPEN"
                    val interval = MarketUtils.getDynamicPollingInterval()
                    
                    if (_syncTimerFlow.value <= 0 || _syncTimerFlow.value > interval) {
                        try {
                            repository.updateAllStockPrices()
                            repository.backfillMissingData()
                        } catch (e: Exception) {
                            Log.e("TraderViewModel", "Periodic update failed: ${e.message}")
                            if (e is IOException) {
                                repository.recordMissedSync(System.currentTimeMillis())
                            }
                        }
                        _syncTimerFlow.value = interval
                    }
                    
                    delay(1000)
                    _syncTimerFlow.value -= 1
                } else {
                    // Market is closed, update countdown every second
                    val timeToOpen = MarketUtils.getTimeToNextMarketOpen()
                    _marketStatusFlow.value = "Açılır: ${MarketUtils.formatMilliseconds(timeToOpen)}"
                    
                    // Reset sync timer to standard default so it's ready
                    _syncTimerFlow.value = 60
                    
                    delay(1000)
                }
            }
        }
    }

    private fun calculateDailyPerformance(logs: List<DecisionLogEntity>): PerformanceStats {
        // Only evaluate "Mature" decisions (e.g., status changed from PENDING)
        val evaluatedLogs = logs.filter { it.status1D == "SUCCESS" || it.status1D == "FAILED" }
        
        var totalSuccess = 0
        val symbolMap = mutableMapOf<String, Pair<Int, Int>>()

        val timeframeCounts = mutableMapOf<String, Int>()
        val timeframeSuccess = mutableMapOf<String, Int>()

        var buyCount = 0
        var buySuccess = 0
        var sellCount = 0
        var sellSuccess = 0
        var holdCount = 0
        var holdSuccess = 0

        evaluatedLogs.forEach { log ->
            val isSuccess = log.status1D == "SUCCESS"
            if (isSuccess) totalSuccess++
            
            val currentStats = symbolMap.getOrDefault(log.symbol, 0 to 0)
            symbolMap[log.symbol] = (currentStats.first + 1) to (currentStats.second + (if (isSuccess) 1 else 0))

            // Action specific stats
            when {
                log.aiDecision.contains("BUY") -> {
                    buyCount++
                    if (isSuccess) buySuccess++
                }
                log.aiDecision.contains("SELL") -> {
                    sellCount++
                    if (isSuccess) sellSuccess++
                }
                log.aiDecision.contains("HOLD") -> {
                    holdCount++
                    if (isSuccess) holdSuccess++
                }
            }

            updateTimeframeStats(log.status1D, "1D", timeframeCounts, timeframeSuccess)
            updateTimeframeStats(log.status3D, "3D", timeframeCounts, timeframeSuccess)
            updateTimeframeStats(log.status5D, "5D", timeframeCounts, timeframeSuccess)
            updateTimeframeStats(log.status10D, "10D", timeframeCounts, timeframeSuccess)
            updateTimeframeStats(log.status30D, "30D", timeframeCounts, timeframeSuccess)
        }

        val total = evaluatedLogs.size
        val accuracy = if (total > 0) (totalSuccess.toDouble() / total) * 100 else 0.0

        val timeframeAccuracy = timeframeCounts.mapValues { (k, v) ->
            if (v > 0) (timeframeSuccess.getOrDefault(k, 0).toDouble() / v) * 100 else 0.0
        }

        val symbolStats = symbolMap.mapValues { (_, stats) ->
            SymbolPerformance(
                total = stats.first,
                success = stats.second,
                accuracy = if (stats.first > 0) (stats.second.toDouble() / stats.first) * 100 else 0.0
            )
        }

        return PerformanceStats(
            totalDecisions = total,
            successfulDecisions = totalSuccess,
            failedDecisions = total - totalSuccess,
            accuracy = accuracy,
            dailyWinRatio = accuracy,
            timeframeAccuracy = timeframeAccuracy,
            symbolStats = symbolStats,
            buyAccuracy = if (buyCount > 0) (buySuccess.toDouble() / buyCount) * 100 else 0.0,
            sellAccuracy = if (sellCount > 0) (sellSuccess.toDouble() / sellCount) * 100 else 0.0,
            holdAccuracy = if (holdCount > 0) (holdSuccess.toDouble() / holdCount) * 100 else 0.0
        )
    }

    private fun updateTimeframeStats(status: String, key: String, counts: MutableMap<String, Int>, success: MutableMap<String, Int>) {
        if (status != "PENDING" && status != "NEUTRAL") {
            counts[key] = counts.getOrDefault(key, 0) + 1
            if (status == "SUCCESS") {
                success[key] = success.getOrDefault(key, 0) + 1
            }
        }
    }

    fun generateAISelfEvaluation() {
        if (_isSelfEvaluationLoading.value) return

        viewModelScope.launch {
            _isSelfEvaluationLoading.value = true
            // To be implemented in Repository for cleaner separation, 
            // for now we use a placeholder or wait for next update.
            _aiSelfEvaluation.value = "REST API keçidi tamamlandı. Gündəlik analiz üçün yeniləyin."
            _isSelfEvaluationLoading.value = false
        }
    }

    fun addStock(symbol: String) {
        viewModelScope.launch {
            repository.addStockToWatchlist(symbol, companyName = "Yüklənir...")

            when (val result = repository.fetchMarketData(symbol, RetrofitInstance.API_KEY)) {
                is MarketDataResult.Success -> {
                    val priceInfo = "Qiymət: $${result.price}"
                    repository.addStockToWatchlist(
                        symbol, 
                        companyName = priceInfo, 
                        price = result.price,
                        open = result.openPrice,
                        prevClose = result.previousClose,
                        volume = result.volume
                    )
                }
                is MarketDataResult.Error -> {
                    repository.addStockToWatchlist(symbol, companyName = result.message)
                }
            }
        }
    }

    fun removeStock(item: WatchlistEntity) {
        viewModelScope.launch {
            repository.removeStockFromWatchlist(item)
        }
    }

    suspend fun analyzeStock(symbol: String): MarketDataResult {
        return repository.fetchMarketData(symbol, RetrofitInstance.API_KEY)
    }

    fun performManualAIAnalysis(symbol: String) {
        viewModelScope.launch {
            _analysisLoadingStates.value = _analysisLoadingStates.value + (symbol to true)
            
            try {
                val marketData = repository.fetchMarketData(symbol, RetrofitInstance.API_KEY)
                if (marketData is MarketDataResult.Success) {
                    val responseText = repository.getManualAIAnalysis(symbol, marketData)
                    
                    val simplifiedDecision = when {
                        responseText.contains("BUY", ignoreCase = true) -> "BUY 🟢"
                        responseText.contains("SELL", ignoreCase = true) -> "SELL 🔴"
                        else -> "HOLD 🟡"
                    }

                    repository.logDecision(
                        DecisionLogEntity(
                            symbol = symbol.uppercase(),
                            rsiValue = marketData.rsi,
                            volumeStatus = "MANUAL",
                            volume = marketData.volume,
                            newsScore = marketData.sentimentScore,
                            aiDecision = simplifiedDecision,
                            decisionReason = responseText,
                            entryPrice = marketData.price,
                            marketOpenPrice = marketData.openPrice
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("GeminiAI", "Analysis error for $symbol: ${e.message}", e)
                val errorMsg = when {
                    e.message?.contains("429") == true -> "AI analiz limiti müvəqqəti aşıldı."
                    e.message?.contains("404") == true -> "AI Model tapılmadı (404). Model adı və ya API açarını yoxlayın."
                    else -> "Xəta: ${e.localizedMessage ?: "Naməlum xəta"}"
                }
                repository.logDecision(
                    DecisionLogEntity(
                        symbol = symbol.uppercase(),
                        rsiValue = 0.0,
                        volumeStatus = "ERROR",
                        newsScore = 0,
                        aiDecision = "ERROR ⚠️",
                        decisionReason = errorMsg,
                        entryPrice = 0.0
                    )
                )
            } finally {
                _analysisLoadingStates.value = _analysisLoadingStates.value - symbol
            }
        }
    }

}

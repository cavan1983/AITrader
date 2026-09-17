package com.example.aitrader

import java.util.Locale

object MarketAnalyzer {

    data class AnalysisSignal(
        val decision: String,
        val rsi: Double,
        val sentimentScore: Int,
        val volumeStatus: String,
        val volume: Double,
        val reason: String
    )

    /**
     * Əsas AI Analiz Mühərriki.
     * Xəbərlər, RSI, Həcm və Keçmiş xətaları birləşdirərək qərar verir.
     */
    fun analyze(
        quote: StockQuoteDto,
        bullishPercent: Double,
        bearishPercent: Double,
        failedHistory: List<DecisionLogEntity>
    ): AnalysisSignal {
        val sentimentScore = analyzeNewsSentiment(bullishPercent, bearishPercent)
        val rsi = calculateRSI(quote)
        val volumeStatus = evaluateVolume(quote.volume) // Burada sadələşdirilmiş həcm analizi

        // 1. Texniki Siqnallar
        var signalPoints = 0
        if (rsi < 30) signalPoints += 2 // Oversold
        if (rsi > 70) signalPoints -= 2 // Overbought

        // 2. Sentiment Siqnalları
        signalPoints += sentimentScore

        // 3. Həcm Təsdiqi
        if (volumeStatus == "HIGH") {
            if (signalPoints > 0) signalPoints += 1
            if (signalPoints < 0) signalPoints -= 1
        }

        // 4. Tarixi Xəta Düzəlişi (Failed Logs)
        // Əgər keçmişdə eyni simvol üçün çox sayda FAILED log varsa, qərarı neytrallaşdırırıq
        val failureCount = failedHistory.size
        if (failureCount >= 3) {
            // Çox xəta varsa, ehtiyatlı davranırıq
            signalPoints /= 2
        }

        // Yekun Qərar
        val (decision, reason) = when {
            signalPoints >= 2 -> "BUY 🟢" to "Güclü alış siqnalı: Sentiment ($sentimentScore) və RSI (${String.format(Locale.US, "%.1f", rsi)}) uyğundur."
            signalPoints <= -2 -> "SELL 🔴" to "Satış tövsiyəsi: RSI ($rsi) çox yüksəkdir və ya xəbərlər neqativdir."
            else -> "HOLD 🟡" to "Bazar qeyri-müəyyəndir. Siqnal balı: $signalPoints"
        }

        return AnalysisSignal(decision, rsi, sentimentScore, volumeStatus, quote.volume, reason)
    }

    // Xəbər sentiment faizləri əsasında -3 ilə +3 arasında sentiment balı verir
    fun analyzeNewsSentiment(bullish: Double, bearish: Double): Int {
        val diff = bullish - bearish
        return when {
            diff > 40 -> 3
            diff > 20 -> 2
            diff > 5 -> 1
            diff < -40 -> -3
            diff < -20 -> -2
            diff < -5 -> -1
            else -> 0
        }
    }

    /**
     * RSI Hesablanması (Və ya Fallback indikatoru)
     * Tam tarixi məlumat yoxdursa, gün içi volatillik və momentum əsasında təxmini RSI verir.
     */
    private fun calculateRSI(quote: StockQuoteDto): Double {
        // Tam RSI üçün 14 günlük data lazımdır. 
        // Fallback: Cari dəyişiklik faizi əsasında "psevdo-RSI"
        val change = quote.currentPrice - quote.previousClose
        val percentChange = if (quote.previousClose != 0.0) (change / quote.previousClose) * 100 else 0.0
        
        // Sadə fallback: 50 mərkəz nöqtəsi olmaqla dəyişikliyə görə 0-100 arası
        val estimatedRSI = 50.0 + (percentChange * 5)
        return estimatedRSI.coerceIn(0.0, 100.0)
    }

    // Həcm vəziyyətini müəyyən edir
    fun evaluateVolume(currentVolume: Double): String {
        // Fallback: Həcm 1 milyondan çoxdursa
        return if (currentVolume > 1_000_000) "HIGH" else "NORMAL"
    }
}
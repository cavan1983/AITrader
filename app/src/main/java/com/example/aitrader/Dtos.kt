package com.example.aitrader

import com.google.gson.annotations.SerializedName

data class StockQuoteDto(
    @SerializedName("c") val currentPrice: Double = 0.0,
    @SerializedName("d") val change: Double = 0.0,
    @SerializedName("dp") val percentChange: Double = 0.0,
    @SerializedName("h") val highPrice: Double = 0.0,
    @SerializedName("l") val lowPrice: Double = 0.0,
    @SerializedName("o") val openPrice: Double = 0.0,
    @SerializedName("pc") val previousClose: Double = 0.0,
    @SerializedName("v") val volume: Double = 0.0,
    @SerializedName("volume") val volumeFull: Double = 0.0,
    @SerializedName("totalVolume") val totalVolume: Double = 0.0,
    @SerializedName("vol24h") val vol24h: Double = 0.0,
    @SerializedName("volume_24h") val volume24h: Double = 0.0
)

data class NewsItemDto(
    @SerializedName("category") val category: String = "",
    @SerializedName("datetime") val datetime: Long = 0L,
    @SerializedName("headline") val headline: String = "",
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("image") val image: String = "",
    @SerializedName("related") val related: String = "",
    @SerializedName("source") val source: String = "",
    @SerializedName("summary") val summary: String = "",
    @SerializedName("url") val url: String = ""
)

data class StockCandleDto(
    @SerializedName("c") val closePrices: List<Double> = emptyList(),
    @SerializedName("h") val highPrices: List<Double> = emptyList(),
    @SerializedName("l") val lowPrices: List<Double> = emptyList(),
    @SerializedName("o") val openPrices: List<Double> = emptyList(),
    @SerializedName("s") val status: String = "",
    @SerializedName("t") val timestamps: List<Long> = emptyList(),
    @SerializedName("v") val volumes: List<Long> = emptyList()
)

data class NewsSentimentDto(
    @SerializedName("buzz") val buzz: BuzzDto? = null,
    @SerializedName("sentiment") val sentiment: SentimentMetricsDto? = null,
    @SerializedName("symbol") val symbol: String = "",
    @SerializedName("sectorAverageBullishPercent") val sectorAverageBullishPercent: Double = 0.0,
    @SerializedName("sectorAverageNewsScore") val sectorAverageNewsScore: Double = 0.0
)

data class BuzzDto(
    @SerializedName("articlesInLastWeek") val articlesInLastWeek: Int = 0,
    @SerializedName("buzz") val buzz: Double = 0.0,
    @SerializedName("weeklyAverage") val weeklyAverage: Double = 0.0
)

data class SentimentMetricsDto(
    @SerializedName("bearishPercent") val bearishPercent: Double = 0.0,
    @SerializedName("bullishPercent") val bullishPercent: Double = 0.0,
    @SerializedName("sectorAverageBullishPercent") val sectorAverageBullishPercent: Double = 0.0,
    @SerializedName("sectorAverageBearishPercent") val sectorAverageBearishPercent: Double = 0.0
)

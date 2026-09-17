package com.example.aitrader

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    // Səhmin cari qiymətini və həcmini çəkmək üçün
    @GET("quote")
    suspend fun getStockQuote(
        @Query("symbol") symbol: String,
        @Query("token") apiKey: String = RetrofitInstance.API_KEY
    ): StockQuoteDto

    // Səhm haqqında son xəbərləri çəkmək üçün
    @GET("company-news")
    suspend fun getCompanyNews(
        @Query("symbol") symbol: String,
        @Query("from") fromDate: String,
        @Query("to") toDate: String,
        @Query("token") apiKey: String
    ): List<NewsItemDto>

    // Səhm haqqında sentiment analizi çəkmək üçün
    @GET("news-sentiment")
    suspend fun getNewsSentiment(
        @Query("symbol") symbol: String,
        @Query("token") apiKey: String = RetrofitInstance.API_KEY
    ): NewsSentimentDto

    // Səhmin tarixi (candle) məlumatlarını çəkmək üçün
    @GET("stock/candle")
    suspend fun getStockCandles(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String, // "1", "5", "15", "30", "60", "D", "W", "M"
        @Query("from") fromTimestamp: Long,
        @Query("to") toTimestamp: Long,
        @Query("token") apiKey: String = RetrofitInstance.API_KEY
    ): StockCandleDto
}

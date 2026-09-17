package com.example.aitrader

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val symbol: String, // Məsələn: AAPL, TSLA
    val companyName: String = "",
    val currentPrice: Double = 0.0,
    val openPrice: Double = 0.0,
    val previousClose: Double = 0.0,
    val currentVolume: Double = 0.0,
    val addedDate: Long = System.currentTimeMillis()
)

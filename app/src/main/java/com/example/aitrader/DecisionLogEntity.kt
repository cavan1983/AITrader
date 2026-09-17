package com.example.aitrader

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decision_logs")
data class DecisionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String, // Məsələn: AAPL
    val rsiValue: Double,
    val volumeStatus: String,
    val newsScore: Int,
    val aiDecision: String,
    val decisionReason: String,
    val entryPrice: Double,
    val volume: Double = 0.0,
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis(),
    
    // Market session prices
    val marketOpenPrice: Double? = null,
    val marketClosePrice: Double? = null,

    // Timeframe Evaluations (SUCCESS, FAILED, NEUTRAL, PENDING)
    val status1D: String = "PENDING",
    val status3D: String = "PENDING",
    val status5D: String = "PENDING",
    val status10D: String = "PENDING",
    val status30D: String = "PENDING",

    // Risk Management
    val stopLossPct: Double? = null,
    val takeProfitPct: Double? = null
)

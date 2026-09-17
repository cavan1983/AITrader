package com.example.aitrader

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TraderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: WatchlistEntity)

    @Delete
    suspend fun deleteStock(stock: WatchlistEntity)

    @Query("SELECT * FROM watchlist ORDER BY addedDate DESC")
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist")
    suspend fun getAllWatchlistSync(): List<WatchlistEntity>

    @Insert
    suspend fun insertLog(log: DecisionLogEntity)

    @Update
    suspend fun updateLog(log: DecisionLogEntity)

    @Query("SELECT * FROM decision_logs WHERE symbol = :symbol AND status = 'PENDING'")
    suspend fun getPendingLogs(symbol: String): List<DecisionLogEntity>
    
    @Query("SELECT * FROM decision_logs WHERE status1D = 'PENDING' OR status3D = 'PENDING' OR status5D = 'PENDING' OR status10D = 'PENDING' OR status30D = 'PENDING'")
    suspend fun getPendingEvaluationLogs(): List<DecisionLogEntity>

    @Query("SELECT * FROM decision_logs WHERE symbol = :symbol AND status = 'FAILED' ORDER BY timestamp DESC LIMIT 5")
    suspend fun getFailedLogs(symbol: String): List<DecisionLogEntity>

    @Query("UPDATE decision_logs SET status = :status WHERE id = :logId")
    suspend fun updateLogStatus(logId: Long, status: String)

    @Query("SELECT * FROM decision_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<DecisionLogEntity>>

    @Query("SELECT * FROM decision_logs WHERE symbol = :symbol ORDER BY timestamp DESC")
    fun getLogsBySymbol(symbol: String): Flow<List<DecisionLogEntity>>

    @Query("SELECT * FROM decision_logs WHERE id IN (SELECT MAX(id) FROM decision_logs GROUP BY symbol) ORDER BY timestamp DESC")
    fun getLatestLogPerStock(): Flow<List<DecisionLogEntity>>

    @Query("SELECT * FROM decision_logs WHERE symbol = :symbol ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLogForSymbol(symbol: String): Flow<DecisionLogEntity?>

    @Query("SELECT * FROM watchlist WHERE symbol = :symbol")
    fun getStockBySymbol(symbol: String): Flow<WatchlistEntity?>

    // Missed Syncs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissedSync(sync: MissedSyncEntity)

    @Query("SELECT * FROM missed_syncs ORDER BY timestamp ASC")
    suspend fun getAllMissedSyncs(): List<MissedSyncEntity>

    @Query("DELETE FROM missed_syncs")
    suspend fun deleteAllMissedSyncs()
}

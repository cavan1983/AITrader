package com.example.aitrader

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class StockUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val dao = database.traderDao()
            val apiService = RetrofitInstance.api
            val geminiApiService = RetrofitInstance.geminiApi
            val repository = TraderRepository(dao, apiService, geminiApiService)

            repository.updateAllStockPrices()
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

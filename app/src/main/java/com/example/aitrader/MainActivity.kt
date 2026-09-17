package com.example.aitrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Arxa plan yenilənməsini başladırıq
        scheduleBackgroundPriceUpdates()

        // Database, Dao və Repository instansiyalarını yaradılması
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.traderDao()
        val apiService = RetrofitInstance.api
        val geminiApiService = RetrofitInstance.geminiApi
        val repository = TraderRepository(dao, apiService, geminiApiService)
        val factory = TraderViewModelFactory(repository)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: TraderViewModel = viewModel(factory = factory)
                    TraderHomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun scheduleBackgroundPriceUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // WorkManager minimum 15 dəqiqəlik dövrləri dəstəkləyir
        val updateRequest = PeriodicWorkRequestBuilder<StockUpdateWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "StockUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest
        )
    }
}

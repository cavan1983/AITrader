package com.example.aitrader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogHistoryScreen(
    viewModel: TraderViewModel,
    onBack: () -> Unit
) {
    val logs by viewModel.latestLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack) {
                Text("← Geri")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "AI Qərar Loqları", style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Hələ heç bir loq yoxdur.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    LogItem(log)
                }
            }
        }
    }
}

@Composable
fun LogItem(log: DecisionLogEntity) {
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(log.timestamp))
    
    val statusColor = when (log.aiDecision) {
        "BUY 🟢" -> Color(0xFF4CAF50)
        "SELL 🔴" -> Color(0xFFF44336)
        "HOLD 🟡" -> Color(0xFFFFC107)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = log.symbol, style = MaterialTheme.typography.titleLarge)
                Text(text = date, style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Qərar: ", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = log.aiDecision,
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Qiymət: $${log.entryPrice}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Həcm (24h): ${formatVolume(log.volume)}", style = MaterialTheme.typography.bodySmall)
            Text(text = "RSI: ${String.format(Locale.US, "%.1f", log.rsiValue)}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Sentiment: ${log.newsScore}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Proqnoz Dəqiqliyi:", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            TimeframeRow(
                status1D = log.status1D,
                status3D = log.status3D,
                status5D = log.status5D,
                status10D = log.status10D,
                status30D = log.status30D
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Səbəb: ${log.decisionReason}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

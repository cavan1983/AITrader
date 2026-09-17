package com.example.aitrader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun AIPerformanceScreen(
    viewModel: TraderViewModel,
    onBack: () -> Unit
) {
    val stats by viewModel.performanceStats.collectAsState()
    val selfEvaluation by viewModel.aiSelfEvaluation.collectAsState()
    val isEvaluationLoading by viewModel.isSelfEvaluationLoading.collectAsState()

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
            Text(text = "AI Performans Dashboard", style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DailyOverallPerformanceCard(stats)
            }

            item {
                ActionAccuracyCard(stats)
            }

            item {
                TimeframeAccuracyCard(stats.timeframeAccuracy)
            }

            item {
                AISelfEvaluationCard(
                    evaluation = selfEvaluation,
                    isLoading = isEvaluationLoading,
                    onRefresh = { viewModel.generateAISelfEvaluation() }
                )
            }

            item {
                Text(
                    text = "AI Özünüqiymətləndirmə və Dəqiqlik İndeksi",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(stats.symbolStats.toList()) { (symbol, perf) ->
                SymbolPerformanceItem(symbol, perf)
            }
        }
    }
}

@Composable
fun DailyOverallPerformanceCard(stats: PerformanceStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Ümumi Dəqiqlik İndeksi", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${String.format(Locale.US, "%.1f", stats.dailyWinRatio)}%",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { (stats.dailyWinRatio / 100).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatItem("Qiymətləndirilən", stats.totalDecisions.toString())
                StatItem("Uğurlu Seans", stats.successfulDecisions.toString(), Color(0xFF4CAF50))
                StatItem("Uğursuz Seans", stats.failedDecisions.toString(), Color(0xFFF44336))
            }
        }
    }
}

@Composable
fun ActionAccuracyCard(stats: PerformanceStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Qərar Tipləri Üzrə Dəqiqlik", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            AccuracyProgressBar("BUY Accuracy", stats.buyAccuracy, Color(0xFF4CAF50))
            AccuracyProgressBar("SELL Accuracy", stats.sellAccuracy, Color(0xFFF44336))
            AccuracyProgressBar("HOLD Accuracy", stats.holdAccuracy, Color(0xFFFFB300))
        }
    }
}

@Composable
fun AccuracyProgressBar(label: String, accuracy: Double, color: Color) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = "${String.format(Locale.US, "%.1f", accuracy)}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (accuracy / 100).toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun TimeframeAccuracyCard(timeframeAccuracy: Map<String, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Zaman Aralıqları Üzrə Dəqiqlik", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            val timeframes = listOf("1D", "3D", "5D", "10D", "30D")
            timeframes.forEach { tf ->
                val accuracy = timeframeAccuracy.getOrDefault(tf, 0.0)
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = tf, style = MaterialTheme.typography.bodySmall)
                        Text(text = "${String.format(Locale.US, "%.1f", accuracy)}%", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (accuracy / 100).toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = if (accuracy >= 50) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

@Composable
fun AISelfEvaluationCard(
    evaluation: String,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🤖 AI Gündəlik Seans Analizi", style = MaterialTheme.typography.titleMedium)
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = evaluation,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    Text("Analiz aparılır...")
                } else {
                    Text("📊 Seans Analizini Yenilə")
                }
            }
        }
    }
}

@Composable
fun SymbolPerformanceItem(symbol: String, perf: SymbolPerformance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = symbol, style = MaterialTheme.typography.titleMedium)
                Text(text = "${perf.total} gündəlik qərar", style = MaterialTheme.typography.bodySmall)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", perf.accuracy)}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (perf.accuracy >= 50) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Text(text = "Uğur dərəcəsi", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

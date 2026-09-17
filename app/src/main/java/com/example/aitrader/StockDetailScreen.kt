package com.example.aitrader

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun StockDetailScreen(
    symbol: String,
    viewModel: TraderViewModel,
    onBack: () -> Unit
) {
    val selectedStock by viewModel.selectedStock.collectAsStateWithLifecycle()
    val latestLog by viewModel.selectedStockLog.collectAsStateWithLifecycle()
    val loadingStates by viewModel.analysisLoadingStates.collectAsStateWithLifecycle()
    val isLoading = loadingStates[symbol] ?: false
    var marketResult by remember { mutableStateOf<MarketDataResult?>(null) }

    LaunchedEffect(symbol) {
        marketResult = viewModel.analyzeStock(symbol)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(onClick = onBack) {
            Text("← Geri")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "$symbol Analiz Hesabatı", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                text = "AI Analiz aparılır...",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            Button(
                onClick = { viewModel.performManualAIAnalysis(symbol) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(imageVector = Icons.Default.Analytics, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🤖 AI Analiz Et")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (val result = marketResult) {
            is MarketDataResult.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val currentPrice = selectedStock?.currentPrice ?: result.price
                        
                        // Cari Qiymət (Live Price) - Prominent display
                        PriceItem("Cari Qiymət (Live Price)", currentPrice, isBold = true, isLarge = true)
                        
                        // Price Change Details
                        val change = currentPrice - result.previousClose
                        val percent = if (result.previousClose > 0) (change / result.previousClose) * 100 else 0.0
                        PriceChangeRow(change, percent)

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Profit/Loss Summary relative to AI Entry
                        latestLog?.let { log ->
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            val plAmount = currentPrice - log.entryPrice
                            val plPercent = if (log.entryPrice > 0) (plAmount / log.entryPrice) * 100 else 0.0
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "AI Qazanc/İtki:", style = MaterialTheme.typography.titleSmall)
                                val plColor = if (plAmount >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                Text(
                                    text = String.format(Locale.US, "%s$%.2f (%.2f%%)", if (plAmount > 0) "+" else "", plAmount, plPercent),
                                    color = plColor,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Giriş Qiyməti: $${log.entryPrice} (${log.aiDecision})",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Secondary reference markers
                        PriceItem("Dünənki Bağlanış (Previous Close)", result.previousClose)
                        PriceItem("Günün Açılışı (Today Open)", result.openPrice)
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        
                        val displayVolume = selectedStock?.currentVolume ?: result.volume
                        Text(text = "Həcm (24h): ${formatVolume(displayVolume)}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "RSI Göstəricisi: ${String.format(Locale.US, "%.1f", result.rsi)}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Xəbər Sentiment: ${String.format(Locale.US, "%.1f", result.bullishPercent)}% Bullish (Sektor: ${String.format(Locale.US, "%.1f", result.sectorBullishPercent)}%)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(text = "Media Aktivliyi (Buzz): ${String.format(Locale.US, "%.2f", result.buzzScore)}", style = MaterialTheme.typography.bodyMedium)

                        if (result.newsHeadlines.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Son Xəbərlər (Context):", style = MaterialTheme.typography.labelMedium)
                            result.newsHeadlines.forEach { headline ->
                                Text(
                                    text = "• $headline",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "AI Tövsiyəsi: ${latestLog?.aiDecision ?: result.decision}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        latestLog?.let { log ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = log.decisionReason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(text = "Tahmin Dəqiqliyi (Timeframes):", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        TimeframeRow(
                            status1D = latestLog?.status1D ?: "PENDING",
                            status3D = latestLog?.status3D ?: "PENDING",
                            status5D = latestLog?.status5D ?: "PENDING",
                            status10D = latestLog?.status10D ?: "PENDING",
                            status30D = latestLog?.status30D ?: "PENDING"
                        )
                    }
                }

                // Gemini section removed
            }
            is MarketDataResult.Error -> {
                Text(text = "Xəta: ${result.message}", color = Color.Red)
            }
            null -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
fun PriceChangeRow(change: Double, percent: Double) {
    val isPositive = change >= 0
    val isNeutral = Math.abs(change) < 0.0001
    val color = when {
        isNeutral -> Color(0xFF9CA3AF)
        isPositive -> Color(0xFF10B981)
        else -> Color(0xFFEF4444)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPositive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = String.format(Locale.US, "%s$%.2f (%.2f%%)", if (isPositive && !isNeutral) "+" else "", change, percent),
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PriceItem(label: String, price: Double, isBold: Boolean = false, isLarge: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "$${String.format(Locale.US, "%.2f", price)}",
            style = if (isLarge) MaterialTheme.typography.headlineSmall else if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

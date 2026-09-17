package com.example.aitrader

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraderHomeScreen(viewModel: TraderViewModel) {
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val latestLogs by viewModel.latestLogs.collectAsStateWithLifecycle()
    val loadingStates by viewModel.analysisLoadingStates.collectAsStateWithLifecycle()
    var symbolInput by remember { mutableStateOf("") }
    val syncTimer by viewModel.syncTimerFlow.collectAsStateWithLifecycle()
    val marketStatus by viewModel.marketStatusFlow.collectAsStateWithLifecycle()

    // Seçilmiş səhmi saxlayan state (Analiz ekranına keçid üçün)
    val selectedStock by viewModel.selectedStock.collectAsStateWithLifecycle()
    
    // Loqların tarixçəsini göstərmək üçün state
    var showLogs by remember { mutableStateOf(false) }
    
    // AI Performans ekranını göstərmək üçün state
    var showPerformance by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Trader", fontWeight = FontWeight.Bold) },
                actions = {
                    CountdownBadge(syncTimer, marketStatus)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            // Navigation Logic
            if (showLogs) {
                LogHistoryScreen(
                    viewModel = viewModel,
                    onBack = { showLogs = false }
                )
            } else if (showPerformance) {
                AIPerformanceScreen(
                    viewModel = viewModel,
                    onBack = { showPerformance = false }
                )
            } else if (selectedStock != null) {
                StockDetailScreen(
                    symbol = selectedStock!!.symbol,
                    viewModel = viewModel,
                    onBack = { viewModel.selectSymbol(null) }
                )
            } else {
                MainWatchlistContent(
                    watchlist = watchlist,
                    symbolInput = symbolInput,
                    onSymbolChange = { symbolInput = it },
                    onAddStock = {
                        if (symbolInput.isNotBlank()) {
                            viewModel.addStock(symbolInput.trim())
                            symbolInput = ""
                        }
                    },
                    latestLogs = latestLogs,
                    loadingStates = loadingStates,
                    onAnalyzeStock = { viewModel.performManualAIAnalysis(it) },
                    onSelectStock = { viewModel.selectSymbol(it) },
                    onRemoveStock = { viewModel.removeStock(it) },
                    onShowLogs = { showLogs = true },
                    onShowPerformance = { showPerformance = true }
                )
            }
        }
    }
}

@Composable
fun CountdownBadge(seconds: Int, marketStatus: String) {
    val isMarketOpen = marketStatus == "MARKET_OPEN"
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isMarketOpen) Color(0xFF4CAF50) else Color(0xFFFFB300),
        animationSpec = tween(durationMillis = 500)
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isMarketOpen) Color.White else Color.Black,
        animationSpec = tween(durationMillis = 500)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isMarketOpen) 0.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(end = 12.dp)
            .graphicsLayer { if (isMarketOpen) this.alpha = alpha }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isMarketOpen) "Sync: ${seconds}s" else marketStatus,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PriceChangeBadge(current: Double, prevClose: Double) {
    if (prevClose <= 0) return

    val change = current - prevClose
    val percent = (change / prevClose) * 100
    val isPositive = change >= 0
    val isNeutral = Math.abs(change) < 0.0001

    val color = when {
        isNeutral -> Color(0xFF9CA3AF)
        isPositive -> Color(0xFF10B981)
        else -> Color(0xFFEF4444)
    }

    val icon = when {
        isNeutral -> null
        isPositive -> Icons.Default.ArrowDropUp
        else -> Icons.Default.ArrowDropDown
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = String.format(Locale.US, "%s$%.2f (%.2f%%)", if (isPositive && !isNeutral) "+" else "", change, percent),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MainWatchlistContent(
    watchlist: List<WatchlistEntity>,
    symbolInput: String,
    onSymbolChange: (String) -> Unit,
    onAddStock: () -> Unit,
    latestLogs: List<DecisionLogEntity>,
    loadingStates: Map<String, Boolean>,
    onAnalyzeStock: (String) -> Unit,
    onSelectStock: (String) -> Unit,
    onRemoveStock: (WatchlistEntity) -> Unit,
    onShowLogs: () -> Unit,
    onShowPerformance: () -> Unit
) {
    val logsMap = remember(latestLogs) { latestLogs.associateBy { it.symbol } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Watchlist",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Row {
                IconButton(onClick = onShowPerformance) {
                    Icon(imageVector = Icons.Default.Analytics, contentDescription = "AI Performans")
                }
                IconButton(onClick = onShowLogs) {
                    Icon(imageVector = Icons.Default.History, contentDescription = "AI Loqları")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = symbolInput,
                onValueChange = onSymbolChange,
                label = { Text("Symbol (ex: AAPL)") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = onAddStock) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(watchlist) { item ->
                val log = logsMap[item.symbol]
                val isLoading = loadingStates[item.symbol] ?: false

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectStock(item.symbol) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.symbol,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = item.companyName,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Live: $${String.format(Locale.US, "%.2f", item.currentPrice)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                PriceChangeBadge(item.currentPrice, item.previousClose)
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "Open: $${String.format(Locale.US, "%.2f", item.openPrice)}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "Vol: ${formatVolume(item.currentVolume)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        if (log != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "AI Analiz: ${log.aiDecision}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = log.decisionReason,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analiz edilir...", style = MaterialTheme.typography.labelSmall)
                            } else {
                                TextButton(
                                    onClick = { onAnalyzeStock(item.symbol) },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("🤖 AI Analiz Et", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { onRemoveStock(item) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Sil",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

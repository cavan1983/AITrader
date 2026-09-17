package com.example.aitrader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimeframeRow(
    status1D: String,
    status3D: String,
    status5D: String,
    status10D: String,
    status30D: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeframeItem("1D", status1D)
        TimeframeItem("3D", status3D)
        TimeframeItem("5D", status5D)
        TimeframeItem("10D", status10D)
        TimeframeItem("30D", status30D)
    }
}

@Composable
fun TimeframeItem(label: String, status: String) {
    val color = when (status) {
        "SUCCESS" -> Color(0xFF4CAF50)
        "FAILED" -> Color(0xFFF44336)
        "NEUTRAL" -> Color(0xFF9E9E9E)
        "PENDING" -> Color(0xFFFFC107)
        else -> Color.Gray
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
    }
}

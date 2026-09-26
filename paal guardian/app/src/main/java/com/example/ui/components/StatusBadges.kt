package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TemperatureStatusBadge(
    isSafe: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSafe) TempSafeBg else TempWarningBg
    val textColor = if (isSafe) TempSafe else TempWarning
    val borderColor = if (isSafe) TempSafeBorder else TempWarningBorder
    val label = if (isSafe) "Safe" else "Warning"
    val icon = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ThermalRiskBadge(
    riskLevel: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (riskLevel.uppercase()) {
        "LOW" -> Triple(TempSafeBg, TempSafe, TempSafeBorder)
        "MODERATE" -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), Color(0xFFFFE082))
        else -> Triple(TempWarningBg, TempWarning, TempWarningBorder)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Risk: $riskLevel",
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}

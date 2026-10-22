package com.selendang3.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selendang3.app.data.FfbCostYtd
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FfbCostPanel(data: FfbCostYtd?) {
    val budget = data?.budgetRmPerTonne
    val actual = data?.actualRmPerTonne
    val maximum = listOfNotNull(budget, actual).maxOrNull()?.takeIf { it > 0 } ?: 1.0
    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(data?.period?.let {
            "YTD · January–" + it.format(DateTimeFormatter.ofPattern("MMMM uuuu", Locale.ENGLISH))
        } ?: "YTD reporting period: Pending", fontWeight = FontWeight.Bold)
        Text("RM/tonne · bars start at zero", style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CostColumn("Budget YTD", budget, maximum, Color(0xFFD3A21A), Modifier.weight(1f))
            CostColumn("Actual YTD", actual, maximum, Color(0xFF397FBA), Modifier.weight(1f))
        }
        if (budget == null) Text("A matching YTD budget is pending. The annual budget rate is not used.",
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CostColumn(label: String, value: Double?, maximum: Double, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(value?.let { String.format(Locale.US, "%,.2f", it) } ?: "Pending", fontWeight = FontWeight.Bold)
        Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.BottomCenter) {
            if (value != null) Box(Modifier.fillMaxWidth(0.65f)
                .height((value / maximum * 144).toFloat().coerceIn(0f, 144f).dp).background(color))
            else Text("Awaiting data", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.Center))
        }
        HorizontalDivider()
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

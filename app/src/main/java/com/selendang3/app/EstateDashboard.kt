package com.selendang3.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val DashboardInk = Color(0xFF202020)
private val DashboardTeal = Color(0xFF49ACC3)
private val DashboardYellow = Color(0xFFFFC000)
private val DashboardRed = Color(0xFFE41E2B)
private val DashboardGreen = Color(0xFFBCE99A)
private const val Pending = "—"

/** Slide 1 layout only. No Berabong reference values are used as Selendang 3 data. */
@Composable
fun EstateDashboard() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            BoxWithConstraints {
                val wide = maxWidth >= 840.dp
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("DASHBOARD · SELENDANG 3", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge)
                    Text("Reporting period: pending", style = MaterialTheme.typography.bodyMedium)
                    Text("Estate figures have not been imported. — means unavailable.",
                        style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(color = DashboardYellow, thickness = 2.dp)
                    if (wide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                ProfilePanels()
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                PeopleAndRainfallPanels()
                            }
                            Column(Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                ProductionPanels()
                            }
                        }
                    } else {
                        ProfilePanels()
                        PeopleAndRainfallPanels()
                        ProductionPanels()
                    }
                    DashboardPanel("Cost Breakdown", DashboardGreen) {
                        DashboardTable(
                            listOf("Measure", "Upkeep\nRM/ha", "Har./Coll.\nRM/ton", "G.C.\nRM/ha", "Total FFB\nRM/ton"),
                            listOf("Budget", "Actual", "%").map { listOf(it, Pending, Pending, Pending, Pending) },
                            minimumWidth = 620,
                        )
                    }
                    DashboardPanel("Checkroll Average", DashboardYellow) {
                        DashboardTable(listOf("Category", "RM/Mdy", "% O/Turn"),
                            listOf(listOf("Harvester", Pending, Pending), listOf("Field Worker", Pending, Pending)),
                            minimumWidth = 380)
                    }
                    HorizontalDivider(color = DashboardRed, thickness = 4.dp)
                }
            }
        }
    }
}

@Composable
private fun ProfilePanels() {
    DashboardPanel("Age Profile", DashboardYellow) {
        ChartPending("Palm age distribution")
        DashboardTable(listOf("Age category", "Share"), listOf(
            listOf("0–3 years · Immature", Pending),
            listOf("7–18 years · Prime", Pending),
            listOf("19–25 years · Old", Pending),
        ))
        MetricRow("Total area (ha)")
    }
    DashboardPanel("Topography", DashboardTeal) {
        ChartPending("Terrain distribution")
        DashboardTable(listOf("Terrain", "Share"),
            listOf("Flat", "Hilly", "Steep Hilly").map { listOf(it, Pending) })
    }
    DashboardPanel("YPH", DashboardYellow) {
        ChartPending("Yearly yield comparison · t/ha")
        Text("Actual and budget series will appear when records are available.",
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
    }
    DashboardPanel("Average SPH", DashboardYellow) { MetricRow("Palms/ha") }
    DashboardPanel("Soil Type", DashboardTeal) {
        DashboardTable(listOf("Soil series", "Share"), listOf(listOf("Pending estate records", Pending)))
    }
}

@Composable
private fun PeopleAndRainfallPanels() {
    DashboardPanel("Rainfall", DashboardTeal) {
        ChartPending("Monthly rainfall comparison · mm")
        Text("Jan  Feb  Mar  Apr  May  Jun  Jul  Aug  Sep  Oct  Nov  Dec",
            style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(8.dp))
    }
    DashboardPanel("Workforce", DashboardTeal) {
        DashboardTable(listOf("Category", "People"),
            listOf("Staff", "Local Workers", "Foreign Workers", "Contract Workers", "Total Workforce")
                .map { listOf(it, Pending) })
    }
    DashboardPanel("Ratios", DashboardTeal) {
        DashboardTable(listOf("Maturity", "Area (ha)", "Workers:ha"),
            listOf(listOf("Immature", Pending, Pending), listOf("Mature", Pending, Pending)))
    }
}

@Composable
private fun ProductionPanels() {
    DashboardPanel("Kos FFB/TAN", DashboardYellow) { MetricRow("FFB cost (RM/ton)") }
    DashboardPanel("FFB Quality", Color(0xFFDE9999)) {
        DashboardTable(listOf("Category", "Value"), listOf(
            listOf("Ripe Bunches (%)", Pending), listOf("Unripe (%)", Pending),
            listOf("Empty & Rotten (%)", Pending), listOf("Stone/Trash (ton)", Pending),
        ))
    }
    DashboardPanel("Actual versus Budget · YTD", DashboardRed, Color.White) {
        DashboardTable(listOf("Item", "Actual", "Budget"), listOf(
            listOf("OER (%)", Pending, Pending), listOf("KER (%)", Pending, Pending),
            listOf("External Supply (t)", Pending, Pending), listOf("LF", Pending, Pending),
        ))
    }
}

@Composable
private fun DashboardPanel(
    title: String,
    color: Color,
    titleColor: Color = DashboardInk,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxWidth().border(1.dp, Color(0xFFE0E0E0))) {
        Text(title, color = titleColor, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth().background(color).padding(horizontal = 12.dp, vertical = 10.dp))
        content()
    }
}

@Composable
private fun ChartPending(label: String) {
    Column(Modifier.fillMaxWidth().heightIn(min = 140.dp).padding(12.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("Awaiting estate data", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun MetricRow(label: String) {
    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.weight(1f))
        Text(Pending, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DashboardTable(headers: List<String>, rows: List<List<String>>, minimumWidth: Int = 0) {
    // Only wide tables scroll horizontally. The page retains a single vertical scroll position.
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val tableWidth = maxOf(maxWidth, minimumWidth.dp)
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            Column(Modifier.width(tableWidth).padding(horizontal = 8.dp)) {
                (listOf(headers) + rows).forEachIndexed { index, cells ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        cells.forEachIndexed { column, cell ->
                            Text(cell, modifier = Modifier.weight(if (column == 0) 1.5f else 1f),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFD6D6D6))
                }
            }
        }
    }
}

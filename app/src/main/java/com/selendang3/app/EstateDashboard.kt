package com.selendang3.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.selendang3.app.data.EstatePackage
import com.selendang3.app.data.EstateAgeProfile
import com.selendang3.app.data.EstateTopography
import com.selendang3.app.data.YphData
import com.selendang3.app.data.estateTopography
import com.selendang3.app.data.estateAgeProfile
import java.util.Locale
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
fun EstateDashboard(estateUpdate: EstatePackage? = null) {
    val ageProfile = remember(estateUpdate) { estateAgeProfile(estateUpdate) }
    val topography = remember(estateUpdate) { estateTopography(estateUpdate) }
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
                    Text(estateUpdate?.reportingPeriod ?: "Reporting period: pending", style = MaterialTheme.typography.bodyMedium)
                    Text("Age Profile, Topography and YPH use synced estate records. Other panels may still be pending. — means unavailable.",
                        style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(color = DashboardYellow, thickness = 2.dp)
                    if (wide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                ProfilePanels(ageProfile, topography, estateUpdate?.yph)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                PeopleAndRainfallPanels()
                            }
                            Column(Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                ProductionPanels()
                            }
                        }
                    } else {
                        ProfilePanels(ageProfile, topography, estateUpdate?.yph)
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
private fun ProfilePanels(ageProfile: EstateAgeProfile?, topography: EstateTopography?, yph: YphData?) {
    DashboardPanel("Age Profile", DashboardYellow) {
        if (ageProfile == null) {
            ChartPending("Palm age distribution")
            Text("Complete field ages and areas are required.", Modifier.padding(12.dp))
            DashboardTable(listOf("Age category", "Area (ha)", "Share"), listOf(
                listOf("Young · 1–5 years", Pending, Pending),
                listOf("Prime · 6–23 years", Pending, Pending),
                listOf("Old · >23 years", Pending, Pending),
            ))
            MetricRow("Total area (ha)")
        } else {
            val colors = listOf(Color(0xFF49ACC3), Color(0xFF4B8C42), Color(0xFFA46436))
            Canvas(Modifier.size(160.dp).align(Alignment.CenterHorizontally).padding(12.dp)) {
                var angle = -90f
                ageProfile.bands.forEachIndexed { index, band ->
                    val sweep = (band.area / ageProfile.totalArea * 360).toFloat()
                    if (sweep > 0f) drawArc(colors[index], angle, sweep, true)
                    angle += sweep
                }
            }
            DashboardTable(listOf("Age category", "Area (ha)", "Share"),
                ageProfile.bands.map { band -> listOf(band.label,
                    String.format(Locale.US, "%,.2f", band.area),
                    String.format(Locale.US, "%.2f%%", band.area / ageProfile.totalArea * 100)) })
            Text("Total area: " + String.format(Locale.US, "%,.2f ha", ageProfile.totalArea),
                Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            Text("Area-weighted from supplied field ages. Review the source-record status in Estate Information.",
                Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
    }
    DashboardPanel("Topography", DashboardTeal) {
        if (topography == null) {
            ChartPending("Terrain distribution")
            DashboardTable(listOf("Terrain", "Area (ha)", "Share"),
                listOf("Flat", "Hilly", "Undulating").map { listOf(it, Pending, Pending) })
            Text("Sync complete terrain records using the current estate area.",
                Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        } else {
            val colors = listOf(Color(0xFF49ACC3), Color(0xFFA46436), Color(0xFF4B8C42))
            Canvas(Modifier.size(160.dp).align(Alignment.CenterHorizontally).padding(12.dp)) {
                var angle = -90f
                topography.bands.forEachIndexed { index, band ->
                    val sweep = (band.share * 3.6).toFloat()
                    if (sweep > 0f) drawArc(colors[index], angle, sweep, true)
                    angle += sweep
                }
            }
            DashboardTable(listOf("Terrain", "Area (ha)", "Share"),
                topography.bands.map { band -> listOf(band.label,
                    String.format(Locale.US, "%,.2f", band.area),
                    String.format(Locale.US, "%.2f%%", band.share)) })
            Text("Total area: " + String.format(Locale.US, "%,.2f ha", topography.totalArea),
                Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            Text("Source percentages applied to current field area. Rounded hectares reconcile to the total.",
                Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
    }
    DashboardPanel("YPH", DashboardYellow) {
        YphPanel(yph)
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

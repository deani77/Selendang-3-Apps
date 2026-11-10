package com.selendang3.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selendang3.app.data.AnnualRainfall
import com.selendang3.app.data.EstateAgeProfile
import com.selendang3.app.data.EstateAverageSph
import com.selendang3.app.data.EstateSoilSeries
import com.selendang3.app.data.EstateTopography
import com.selendang3.app.data.EstateWorkerRatios
import com.selendang3.app.data.EstateWorkforce
import com.selendang3.app.data.FfbCostComparison
import com.selendang3.app.data.FfbQuality
import com.selendang3.app.data.YphData
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun ProfilePanels(ageProfile: EstateAgeProfile?, topography: EstateTopography?, yph: YphData?, averageSph: EstateAverageSph?, soilSeries: EstateSoilSeries?) {
    DashboardPanel("Age Profile") {
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
            Text("Area-weighted from supplied field ages.",
                Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
    }
    DashboardPanel("Topography") {
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
    DashboardPanel("YPH") {
        YphPanel(yph)
    }
    DashboardPanel("Average SPH") {
        if (averageSph == null) {
            MetricRow("Palms/ha")
            Text("Complete field palm counts and areas are required.", Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall)
        } else {
            Text(String.format(Locale.US, "%,.2f palms/ha", averageSph.palmsPerHa),
                Modifier.padding(12.dp), style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            DashboardTable(listOf("Measure", "Total"), listOf(
                listOf("Oil palm trees · all fields", String.format(Locale.US, "%,d", averageSph.totalPalms)),
                listOf("Estate area (ha)", String.format(Locale.US, "%,.2f", averageSph.totalArea)),
            ))
            Text("Total oil palm trees ÷ total estate hectares.",
                Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
    }
    DashboardPanel("Soil Type") {
        if (soilSeries == null) {
            Text("Sync soil series records using the current estate area.", Modifier.padding(12.dp))
        } else {
            DashboardTable(listOf("Soil series", "Area (ha)", "Share", "Reference class"),
                soilSeries.bands.map { listOf(it.name, String.format(Locale.US, "%,.2f", it.area),
                    String.format(Locale.US, "%.2f%%", it.share), it.suitability) }, minimumWidth = 440)
            Text("Total area: " + String.format(Locale.US, "%,.2f ha", soilSeries.totalArea),
                Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            Text("* Agricultural suitability Class I–V. Published reference ratings; estate verification pending.",
                Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            soilSeries.bands.mapNotNull { it.qualification }.distinct().forEach {
                Text(it, Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall)
            }
            soilSeries.bands.mapNotNull { it.reference }.distinct().forEach {
                Text(it, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
internal fun PeopleAndRainfallPanels(rainfall: List<AnnualRainfall>, workforce: EstateWorkforce?, workerRatios: EstateWorkerRatios?) {
    DashboardPanel("Rainfall") {
        RainfallBarChart(rainfall)
        DashboardTable(listOf("Year", "Period", "Rainfall (mm)"), rainfall.mapIndexed { index, row ->
            val period = if (index > 0) "Full year" else if (row.throughMonth == 0) "YTD · no completed month"
                else "YTD through " + Month.of(row.throughMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            listOf(row.year.toString(), period,
                row.totalMm?.let { String.format(Locale.US, "%,.2f", it) } ?: "Pending")
        })
        Text("Five years; table newest first. Current-year YTD ends at the last completed month (Malaysia time). High/low colours compare completed full years. Tied extremes share a colour; equal totals have no distinct extreme. Missing data remains Pending.",
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
    }
    DashboardPanel("Workforce") {
        Text(workforce?.period?.format(DateTimeFormatter.ofPattern("MMMM uuuu", Locale.ENGLISH))
            ?: "Reporting month: Pending", Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        DashboardTable(listOf("Category", "People"), listOf(
            listOf("Staff", workforce?.staff?.toString() ?: "Pending"),
            listOf("Local Workers", workforce?.localWorkers?.toString() ?: "Pending"),
            listOf("Foreign Workers", workforce?.foreignWorkers?.toString() ?: "Pending"),
            listOf("Contract Workers", workforce?.contractWorkers?.toString() ?: "Pending"),
            listOf("Total Workforce", workforce?.totalWorkforce?.toString() ?: "Pending"),
        ))
    }
    DashboardPanel("Ratios") {
        DashboardTable(listOf("Maturity", "Area (ha)", "Workers:ha"), listOf(
            listOf("Immature", workerRatios?.immatureArea?.let { String.format(Locale.US, "%,.2f", it) } ?: "Pending",
                if (workerRatios?.immatureArea == 0.0) Pending else "Pending"),
            listOf("Mature", workerRatios?.matureArea?.let { String.format(Locale.US, "%,.2f", it) } ?: "Pending",
                if (workerRatios?.matureArea == 0.0) Pending else
                    workerRatios?.matureHaPerWorker?.let { String.format(Locale.US, "1:%.2f", it) } ?: "Pending"),
        ))
        workerRatios?.let {
            Text("Based on " + it.matureOperationsWorkers + " mature-area operations workers only · " +
                it.period.format(DateTimeFormatter.ofPattern("MMMM uuuu", Locale.ENGLISH)),
                Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun ProductionPanels(ffbCost: FfbCostComparison?, quality: FfbQuality) {
    DashboardPanel("Kos FFB/TAN") { FfbCostPanel(ffbCost) }
    DashboardPanel("FFB Quality") {
        DashboardTable(listOf("Category", "This Month", "YTD"), quality.rows)
        Text(quality.note, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
    }
    DashboardPanel("OER (%)") {
        DashboardTable(listOf("Period", "KPG (%)", "KPA (%)"), listOf(listOf("YTD", Pending, Pending)), minimumWidth = 320)
        Text("KPG = actual OER; KPA = budget OER. Awaiting approved OER records.", Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
    }
    DashboardPanel("Oil Yield (t/ha)") {
        DashboardTable(listOf("Period", "Actual (t/ha)", "Budget (t/ha)"), listOf(listOf("YTD", Pending, Pending)), minimumWidth = 320)
        Text("Oil tonnes per hectare. Awaiting approved oil yield records.", Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
    }

}

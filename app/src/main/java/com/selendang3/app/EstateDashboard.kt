package com.selendang3.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.selendang3.app.data.EstatePackage
import com.selendang3.app.data.estateAgeProfile
import com.selendang3.app.data.estateAnnualRainfall
import com.selendang3.app.data.estateAreaSummary
import com.selendang3.app.data.estateAverageSph
import com.selendang3.app.data.estateFfbCosts
import com.selendang3.app.data.estateFfbQuality
import com.selendang3.app.data.estateRainfallToday
import com.selendang3.app.data.estateSoilSeries
import com.selendang3.app.data.estateTopography
import com.selendang3.app.data.estateWorkerRatios
import com.selendang3.app.data.estateWorkforce
import java.util.Locale
import kotlinx.coroutines.delay

/** Estate details use saved server records; unavailable values remain Pending. */
@Composable
fun EstateDashboard(estateUpdate: EstatePackage? = null) {
    val ageProfile = remember(estateUpdate) { estateAgeProfile(estateUpdate) }
    val topography = remember(estateUpdate) { estateTopography(estateUpdate) }
    val averageSph = remember(estateUpdate) { estateAverageSph(estateUpdate) }
    val soilSeries = remember(estateUpdate) { estateSoilSeries(estateUpdate) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val today by produceState(estateRainfallToday(), lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) { value = estateRainfallToday(); delay(60_000) }
        }
    }
    val quality = remember(estateUpdate, today) { estateFfbQuality(estateUpdate, today) }
    val estateArea = remember(estateUpdate, today) { estateAreaSummary(estateUpdate, today) }
    val rainfall = remember(estateUpdate, today) { estateAnnualRainfall(estateUpdate, today) }
    val workforce = remember(estateUpdate) { estateWorkforce(estateUpdate) }
    val workerRatios = remember(estateUpdate) { estateWorkerRatios(estateUpdate) }
    val ffbCost = remember(estateUpdate) { estateFfbCosts(estateUpdate) }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
            BoxWithConstraints {
                val wide = maxWidth >= 840.dp
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(estateUpdate?.estateName?.let { "ESTATE DETAILS · ${it.uppercase(Locale.ROOT)}" } ?: "ESTATE DETAILS", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge)
                    DashboardPanel("Estate Area") {
                        DashboardTable(listOf("Measure", "Area (ha)"), listOf(
                            listOf("Perimeter Hectare", estateArea?.let { String.format(Locale.US, "%,.2f", it.perimeterHa).trim() } ?: Pending),
                            listOf("Mature Hectare", estateArea?.let { String.format(Locale.US, "%,.2f", it.matureHa).trim() } ?: Pending)
                        ), minimumWidth = 320)
                        Text(estateArea?.let { "Reporting period: ${it.period}" } ?: "Awaiting verified estate area records.",
                            Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                    }
                    Text(estateUpdate?.reportingPeriod ?: "Reporting period: pending", style = MaterialTheme.typography.bodyMedium)
                    Text("Age Profile, Topography, YPH, Average SPH, Soil Type, Rainfall, Workforce, Ratios and FFB cost use synced estate records. Other panels may still be pending. — means unavailable.",
                        style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    if (wide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                ProfilePanels(ageProfile, topography, estateUpdate?.yph, averageSph, soilSeries)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                PeopleAndRainfallPanels(rainfall, workforce, workerRatios)
                            }
                            Column(Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                ProductionPanels(ffbCost, quality)
                            }
                        }
                    } else {
                        ProfilePanels(ageProfile, topography, estateUpdate?.yph, averageSph, soilSeries)
                        PeopleAndRainfallPanels(rainfall, workforce, workerRatios)
                        ProductionPanels(ffbCost, quality)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

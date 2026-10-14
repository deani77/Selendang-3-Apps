package com.selendang3.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.selendang3.app.data.EstateDatabase
import java.text.DateFormat
import java.util.Date
import kotlin.math.cos
import kotlin.math.sin

data class Section(val route: String, val title: String, val description: String)

val estateSections = listOf(
    Section("overview", "Estate Overview", "Estate profile, location, cultivation and maturity status."),
    Section("fields", "Field Details", "Field codes, hectarage, palm population and density."),
    Section("ages", "Palm Age Profile", "Palm ages by field and age-category distribution."),
    Section("soil", "Soil & Topography", "Soil composition, terrain and corresponding areas."),
    Section("rainfall", "Rainfall", "Monthly rainfall, rainy days and historical comparisons."),
    Section("abw", "Average Bunch Weight", "Current-period and cumulative ABW in kg/bunch, with year-on-year comparison. Field-level figures are pending the supporting Excel file."),
    Section("roads", "Roads & Infrastructure", "Main, secondary and subsidiary road specifications."),
)

val mainSections = listOf(
    Section("information", "Estate Information", "Estate profile and rainfall"),
    Section("production", "Estate Production", "Production and yield information will be added later."),
    Section("upkeep", "Upkeep and Cultivation", "Weeding, manuring, pest and disease management will be added later."),
    Section("reports", "Reports", "Estate reports will be added later."),
    Section("settings", "Settings", "Offline data synchronization"),
    Section("about", "About", "Application information"),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { EstateApp() }
    }
}

@Composable
fun EstateApp() {
    val context = LocalContext.current.applicationContext
    val dao = remember(context) { EstateDatabase.get(context).estateDao() }
    val fields by remember(dao) { dao.observeFields() }.collectAsStateWithLifecycle(emptyList())
    val sync by remember(dao) { dao.observeSync() }.collectAsStateWithLifecycle(null)
    val online by remember(context) { connectivity(context) }.collectAsStateWithLifecycle(false)
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "home"
    val allSections = mainSections + estateSections
    val title = when (route) {
        "home" -> "Ladang Selendang 3"
        "dashboard" -> "Estate Dashboard"
        else -> allSections.firstOrNull { it.route == route }?.title ?: "Estate Information"
    }
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFFB5D89A), onPrimary = Color(0xFF163620),
        surface = Color(0xFF142D26), onSurface = Color(0xFFF6F5EA),
    )) {
        Box(Modifier.fillMaxSize()) {
            PlantationBackground(route)
            Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 20.dp)) {
                if (route != "home") {
                    TextButton(onClick = { nav.popBackStack() }) { Text("‹  Back") }
                } else Spacer(Modifier.height(28.dp))
                Text(title, style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    textAlign = if (route == "home") TextAlign.Center else TextAlign.Start)
                NavHost(nav, startDestination = "home", modifier = Modifier.weight(1f)) {
                    composable("home") {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Spacer(Modifier.height(18.dp))
                            Button(onClick = { nav.navigate("dashboard") },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF285A3C), contentColor = Color.White)) {
                                Text("Estate Dashboard", style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            }
                            SectionGrid(mainSections) { nav.navigate(it) }
                        }
                    }
                    composable("information") {
                        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            InfoCard("Estate summary", if (fields.isEmpty())
                                "No estate records on this device. Data has not been synchronized."
                            else "${fields.size} fields stored on this device. Source figures require validation.")
                            SectionGrid(estateSections) { nav.navigate(it) }
                        }
                    }
                    composable("dashboard") { InfoCard("Contents TBD", "Dashboard contents will be defined later.") }
                    estateSections.forEach { section ->
                        composable(section.route) {
                            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                InfoCard(section.title, section.description)
                                InfoCard("Not synced yet", "No records available. Pending figures will remain pending until source records are verified and imported.")
                            }
                        }
                    }
                    mainSections.filter { it.route != "information" }.forEach { section ->
                        composable(section.route) {
                            val message = when(section.route) {
                                "settings" -> "Offline storage is ready. Data import and synchronization are not configured yet."
                                "about" -> "Ladang Selendang 3\nv${BuildConfig.VERSION_NAME}\nAndroid estate information application. Initial Alpha interface."
                                else -> section.description
                            }
                            InfoCard(section.title, message)
                        }
                    }
                }
                val lastSync = sync?.lastSuccessEpochMillis?.let { DateFormat.getDateTimeInstance().format(Date(it)) }
                Text("v${BuildConfig.VERSION_NAME}  •  ${if (online) "Online" else "Offline"}\nLast successful sync: ${lastSync ?: "Not synced yet"}",
                    style = MaterialTheme.typography.labelMedium, color = Color(0xFFE0E8D8),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun SectionGrid(sections: List<Section>, onOpen: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sections.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { section ->
                    FilledTonalButton(onClick = { onOpen(section.route) },
                        modifier = Modifier.weight(1f).heightIn(min = 88.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xEDDFE8D5), contentColor = Color(0xFF173B2A))) {
                        Text(section.title, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xEF142D26)),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(body, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun PlantationBackground(route: String) {
    // Original lightweight illustration; replace with approved plantation photos later.
    // Estate Information subsections share the same background.
    val top = when(route) {
        "home" -> Color(0xFF254F32)
        "dashboard" -> Color(0xFF6A633E)
        "production" -> Color(0xFF65432D)
        "upkeep" -> Color(0xFF3C5827)
        "reports" -> Color(0xFF254A4A)
        "settings", "about" -> Color(0xFF18392E)
        else -> Color(0xFF365B43)
    }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, Color(0xFF0A211A))))) {
        Canvas(Modifier.fillMaxSize()) {
            for (row in 0..7) for (col in 0..4) {
                val center = Offset(size.width * (col + 0.2f + (row % 2) * 0.4f) / 4f, size.height * row / 7f)
                val radius = size.width * 0.11f
                for (leaf in 0..7) {
                    val angle = leaf * Math.PI / 4
                    drawLine(Color(0xFF71965B).copy(alpha = 0.22f), center,
                        center + Offset((cos(angle) * radius).toFloat(), (sin(angle) * radius).toFloat()),
                        strokeWidth = 12f)
                }
                drawCircle(Color(0xFF061E13).copy(alpha = 0.4f), radius * 0.18f, center)
            }
        }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)))
    }
}

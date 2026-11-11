package com.selendang3.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.selendang3.app.core.theme.estateColorScheme
import com.selendang3.app.navigation.EstateNavigation
import com.selendang3.app.navigation.estateRouteTitle
import com.selendang3.app.ui.components.DashboardSystemBars
import com.selendang3.app.ui.components.EstateAppFooter
import com.selendang3.app.ui.components.PlantationBackground

@Composable
fun EstateApp() {
    val state = rememberEstateAppState()
    val dark = when (state.themeChoice) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "home"
    val title = estateRouteTitle(route)
    MaterialTheme(colorScheme = estateColorScheme(dark)) {
        if (route == "dashboard") DashboardSystemBars(dark)
        Box(Modifier.fillMaxSize()) {
            PlantationBackground(route)
            Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = if (route == "dashboard") 12.dp else 20.dp)) {
                if (route != "home") {
                    TextButton(onClick = { nav.popBackStack() }) { Text("‹  Back") }
                } else {
                    Spacer(Modifier.height(20.dp))
                    Text(state.estateUpdate?.estateName?.uppercase(java.util.Locale.ROOT) ?: "ESTATE INFORMATION", modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                }
                Text(title, style = if (route == "home") MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().padding(vertical = if (route == "dashboard") 6.dp else 12.dp),
                    textAlign = if (route == "home") TextAlign.Center else TextAlign.Start)
                EstateNavigation(nav, state.estateUpdate, state.themeChoice, state.onThemeChange, Modifier.weight(1f))
                if (route == "home") {
                    Text("S3 Estate Info", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center)
                }
                EstateAppFooter(state.snapshot, state.lastSyncEpochMillis, state.online)
            }
        }
    }
}

package com.selendang3.app.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.selendang3.app.EstateControlHeight
import com.selendang3.app.EstateDashboard
import com.selendang3.app.EstateSectionData
import com.selendang3.app.FieldDetailsScreen
import com.selendang3.app.OperationalDashboardScreen
import com.selendang3.app.PlanterToolsScreen
import com.selendang3.app.ProductionScreen
import com.selendang3.app.UpkeepCultivationScreen
import com.selendang3.app.UserSettingsScreen
import com.selendang3.app.data.EstatePackage
import com.selendang3.app.ui.components.SectionGrid

@Composable
internal fun EstateNavigation(
    nav: NavHostController,
    estateUpdate: EstatePackage?,
    themeChoice: String,
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(nav, startDestination = "home", modifier = modifier) {
        composable("home") {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Spacer(Modifier.height(8.dp))
                SectionGrid(listOf(Section("dashboard", "Estate Dashboard", "")) + mainSections) { nav.navigate(it) }
            }
        }
        composable("information") {
            Column {
                OutlinedButton(onClick = { nav.navigate("estate-records") }, modifier = Modifier.fillMaxWidth().heightIn(min = EstateControlHeight)) { Text("All Estate Records") }
                Box(Modifier.weight(1f)) { EstateDashboard(estateUpdate) }
            }
        }
        composable("estate-records") {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SectionGrid(estateSections) { nav.navigate(it) }
            }
        }
        estateSections.forEach { section ->
            composable(section.route) { EstateSectionData(section, estateUpdate) }
        }
        composable("production") { FieldDetailsScreen(estateUpdate) }
        composable("upkeep") { UpkeepCultivationScreen(estateUpdate) }
        composable("dashboard") { OperationalDashboardScreen(estateUpdate) }
        composable("reports") { ProductionScreen() }
        composable("tools") { PlanterToolsScreen() }
        composable("settings") {
            UserSettingsScreen(themeChoice, onThemeChange)
        }
    }
}

package com.selendang3.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selendang3.app.data.*

@Composable
fun FieldDetailsScreen(update: EstatePackage?) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val codes = estateFieldCodes(update)
    val field = selected?.takeIf { it in codes }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (field == null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Field Dashboard", style = MaterialTheme.typography.titleLarge)
                    Text("Fields available: " + if (update == null) "Pending" else codes.size.toString())
                    Text("Basic field profiles · area, palms, planting and age records")
                    Text("Choose a field for its basic information. Crop and harvesting records are under Crop Production.")
                }
            }
            codes.chunked(2).forEach { group ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    group.forEach { code ->
                        FilledTonalButton(onClick = { selected = code },
                            modifier = Modifier.weight(1f).heightIn(min = 60.dp)) { Text(code) }
                    }
                    if (group.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            if (codes.isEmpty()) Text("Import estate records through Settings → Estate Data Sync to see field buttons.")
        } else {
            TextButton(onClick = { selected = null }) { Text("‹ All Fields") }
            Text(field, style = MaterialTheme.typography.titleLarge)
            if (sourceFieldProfileTables(update, field).isNotEmpty()) {
                Text("Report year 2027 · source dated 28 Oct 2026. Density and height apply to the division.", style = MaterialTheme.typography.bodySmall)
            }
            (sourceFieldProfileTables(update, field) + estateFieldTables(update, field)).forEach { table ->
                Text(estateSections.firstOrNull { it.route == table.id }?.title ?: table.id,
                    style = MaterialTheme.typography.titleMedium)
                table.rows.forEach { row ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            table.columns.zip(row).forEach { (label, value) -> Text("$label: ${value ?: "Pending"}") }
                        }
                    }
                }
            }
            Text("Basic records for this field only. Crop, harvesting and productivity records are under Crop Production.")
        }
    }
}

@Composable
fun UpkeepCultivationScreen(update: EstatePackage?) {
    var selectedWork by rememberSaveable { mutableStateOf<String?>(null) }
    val work = selectedWork
    BackHandler(enabled = work != null) { selectedWork = null }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (work == null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Upkeep and Cultivation Dashboard", style = MaterialTheme.typography.titleLarge)
                    Text("Weeding progress: Pending")
                    val manuring = operationalDashboardCards(update, estateRainfallToday())[3]
                    Text("Manuring progress: ${manuring.metrics[0].value} %")
                    Text(manuring.period, style = MaterialTheme.typography.bodySmall)
                    Text("Pest and Disease: Pending")
                    Text("Water management: Pending")
                    Text("Road management: Pending")
                }
            }
            Text("Major Upkeep Work", style = MaterialTheme.typography.titleLarge)
            listOf("Weeding", "Manuring", "Pest and Disease", "Water management", "Road management", "Others").forEach { category ->
                FilledTonalButton(onClick = { selectedWork = category }, modifier = Modifier.fillMaxWidth()) {
                    Text(category)
                }
            }
        } else {
            TextButton(onClick = { selectedWork = null }) { Text("‹ Major Upkeep Work") }
            Text(work, style = MaterialTheme.typography.titleLarge)
            if (work == "Others") {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Other Necessary Work", style = MaterialTheme.typography.titleMedium)
                        Text("Additional upkeep work will appear here when verified records are available.")
                        Text("Work records: Pending")
                    }
                }
            } else {
                UpkeepFieldWorkContent(update, work)
            }
        }
    }
}

@Composable
private fun UpkeepFieldWorkContent(update: EstatePackage?, work: String) {
    var selectedField by rememberSaveable(work) { mutableStateOf<String?>(null) }
    val codes = estateFieldCodes(update)
    val field = selectedField?.takeIf { it in codes } ?: codes.firstOrNull()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Fields", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            codes.forEach { code ->
                FilterChip(selected = field == code, onClick = { selectedField = code },
                    label = { Text(code) }, shape = androidx.compose.foundation.shape.CircleShape)
            }
        }
        if (codes.isEmpty()) {
            Text("Import estate records through Settings → Estate Data Sync to see fields.")
        } else if (field == null) {
            Text("Select a field to view its details and upkeep and cultivation records.")
        }
        if (field != null) {
            Text("Field $field", style = MaterialTheme.typography.titleLarge)
            estateFieldTables(update, field).forEach { table ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(estateSections.firstOrNull { it.route == table.id }?.title ?: table.id,
                            style = MaterialTheme.typography.titleMedium)
                        table.rows.forEach { row ->
                            table.columns.forEachIndexed { index, label ->
                                Text("$label: ${row.getOrNull(index) ?: "Pending"}")
                            }
                        }
                    }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("$work · $field", style = MaterialTheme.typography.titleMedium)
                    Text("$work work: Pending")
                    Text("Awaiting verified ${work.lowercase()} records for this field.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

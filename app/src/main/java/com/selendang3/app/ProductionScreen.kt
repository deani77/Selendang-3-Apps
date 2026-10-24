package com.selendang3.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.selendang3.app.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun ProductionScreen() {
    val context = LocalContext.current
    val store = remember(context) { ProductionDraftStore(context) }
    var records by remember { mutableStateOf<List<ProductionDraft>>(emptyList()) }
    var storageError by remember { mutableStateOf<String?>(null) }
    var ready by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(store) {
        try { records = withContext(Dispatchers.IO) { store.load() }; ready = true }
        catch (_: Exception) { storageError = "Saved production drafts could not be read. Editing is unavailable to protect your data." }
    }
    var section by rememberSaveable { mutableStateOf("Today Crop Production") }
    var field by rememberSaveable { mutableStateOf(productionFields.first()) }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var dirty by rememberSaveable { mutableStateOf(false) }
    var pendingChange by remember { mutableStateOf<(() -> Unit)?>(null) }
    fun change(action: () -> Unit) { if (dirty) pendingChange = action else action() }
    val parsedDate = runCatching { LocalDate.parse(date).takeIf { it.toString() == date } }.getOrNull()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Crop Production Dashboard", style = MaterialTheme.typography.titleLarge)
                CropDashboardSummary()
            }
        }
        Text("Production by field", style = MaterialTheme.typography.titleLarge)
        if (ready) Text(store.status(), style = MaterialTheme.typography.bodySmall)
        Text("Drafts are saved on this device. Use Settings → Sync Now to upload and download production. PDF capture is not connected yet.")
        listOf("Crop History by Field", "Today Crop Production", "Month Crop Production", "Year Todate Crop Production").forEach { name ->
            OutlinedButton(onClick = { change { section = name; dirty = false } }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text(if (name == section) "✓ $name" else name)
            }
        }
        Text(section, style = MaterialTheme.typography.titleMedium)
        if (section == "Today Crop Production" || section == "Crop History by Field") {
            productionFields.chunked(3).forEach { group ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    group.forEach { code ->
                        FilterChip(selected = code == field, onClick = { change { field = code; dirty = false } }, label = { Text(code) }, enabled = !busy)
                    }
                }
            }
        }
        OutlinedTextField(date, { value -> change { date = value; dirty = false } }, label = { Text("Reporting date / summary cutoff (YYYY-MM-DD)") },
            singleLine = true, isError = parsedDate == null, enabled = !busy, modifier = Modifier.fillMaxWidth())
        storageError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (parsedDate == null) Text("Enter a valid date, for example 2026-10-20.")
        else when (section) {
            "Crop History by Field" -> {
                Text("$field · five years including ${parsedDate.year} · read-only")
                (parsedDate.year - 4..parsedDate.year).forEach { year ->
                    ProductionCard("$year") { Text("Crop (t): Pending\nYield (t/ha): Pending") }
                }
                Text("Verified field history is not connected. Estate-wide YPH is not substituted for field history.")
            }
            "Today Crop Production" -> key(date, field) {
                val record = records.firstOrNull { it.date == date && it.field == field }
                if (ready) ProductionEditor(record, date, field, busy, { dirty = it }) { draft, done ->
                    busy = true
                    scope.launch {
                        val result = runCatching { withContext(Dispatchers.IO) { store.save(draft) } }
                        result.onSuccess { records = it; dirty = false }
                        done(result.exceptionOrNull()?.message)
                        busy = false
                    }
                }
                OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text("Upload PDF · coming next") }
                Text("PDF field aliases are defined. Pengeluaran Hasil means Crop Sent to Mill. Harvested crop and backlog require manual entry or another source.", style = MaterialTheme.typography.bodySmall)
                Text("Upload/download: Settings → Sync Now. Conflicting edits are reviewed there before replacement.", style = MaterialTheme.typography.bodySmall)
            }
            else -> {
                val monthly = section == "Month Crop Production"
                val from = if (monthly) parsedDate.withDayOfMonth(1) else parsedDate.withDayOfYear(1)
                val expectedDays = ChronoUnit.DAYS.between(from, parsedDate) + 1
                Text("$from through $parsedDate · provisional totals from local saved drafts only. Missing dates are not assumed to be zero.")
                if (monthly) Text("Harvested hectares accumulate work across rounds. Harvester-days add daily attendance; they are not distinct workers. Backlog shows the latest saved balance.", style = MaterialTheme.typography.bodySmall)
                else Text("Only Crop Sent to Mill carries forward from January through this date. Other measures are shown for the selected month only.", style = MaterialTheme.typography.bodySmall)
                if (ready) productionFields.forEach { code ->
                    val summary = productionSummary(records, code, from, parsedDate)
                    ProductionCard(code) {
                        Text("${summary.days} of $expectedDays dates saved")
                        Text("Crop sent to mill: ${summary.sentToMill.label("t")}")
                        if (monthly) {
                            Text("Crop harvested: ${summary.harvested.label("t")}")
                            Text("Hectares harvested: ${summary.hectares.label("ha")}")
                            Text("Harvester-days: ${summary.harvesterDays.label("")}")
                            Text("Backlog: ${summary.backlog?.let { number(it) + " t" } ?: "Pending"}${summary.backlogDate?.let { " · as of $it" } ?: ""}")
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
    if (pendingChange != null) AlertDialog(onDismissRequest = { pendingChange = null },
        title = { Text("Discard unsaved changes?") }, text = { Text("Save the draft before switching if you want to keep these changes.") },
        confirmButton = { TextButton(onClick = { val action = pendingChange; pendingChange = null; dirty = false; action?.invoke() }) { Text("Discard changes") } },
        dismissButton = { TextButton(onClick = { pendingChange = null }) { Text("Keep editing") } })
}

private fun number(value: Double): String = String.format(Locale.US, "%.2f", value)
private fun ProductionTotal.label(unit: String): String = value?.let {
    "${number(it)} $unit · $reportedDays/$savedDays saved days reported"
} ?: "Pending"

@Composable
private fun ProductionCard(title: String, body: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium); body()
    } }
}

@Composable
private fun ProductionEditor(record: ProductionDraft?, date: String, field: String, busy: Boolean,
    onDirty: (Boolean) -> Unit, save: (ProductionDraft, (String?) -> Unit) -> Unit) {
    val initial = listOf(record?.harvested?.toString() ?: "", record?.sentToMill?.toString() ?: "", record?.backlog?.toString() ?: "",
        record?.hectares?.toString() ?: "", record?.harvesters?.toString() ?: "")
    var harvested by rememberSaveable { mutableStateOf(initial[0]) }
    var mill by rememberSaveable { mutableStateOf(initial[1]) }
    var backlog by rememberSaveable { mutableStateOf(initial[2]) }
    var hectares by rememberSaveable { mutableStateOf(initial[3]) }
    var workers by rememberSaveable { mutableStateOf(initial[4]) }
    var notice by rememberSaveable { mutableStateOf<String?>(null) }
    fun changed() { onDirty(true); notice = null }
    Text("$field · $date · ${if (record == null) "New local draft" else "Saved on this device"}")
    Text("Leave unknown values blank. Enter 0 only when zero is confirmed. Saving again replaces this field's draft for this date.")
    listOf(Triple("Crop harvested (t)", harvested, { value: String -> harvested = value }),
        Triple("Crop sent to mill (t)", mill, { value: String -> mill = value }),
        Triple("Crop backlog balance (t)", backlog, { value: String -> backlog = value }),
        Triple("Hectares harvested (ha)", hectares, { value: String -> hectares = value }),
        Triple("Number of harvesters present", workers, { value: String -> workers = value })).forEachIndexed { index, (label, value, update) ->
        OutlinedTextField(value, { update(it); changed() }, label = { Text(label) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = if (index == 4) KeyboardType.Number else KeyboardType.Decimal),
            enabled = !busy, modifier = Modifier.fillMaxWidth())
    }
    Button(onClick = {
        try {
            fun numeric(text: String): Double? = text.trim().takeIf { it.isNotEmpty() }?.let { value ->
                value.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 } ?: throw IllegalArgumentException("Enter non-negative numbers using a decimal point.")
            }
            val count = workers.trim().takeIf { it.isNotEmpty() }?.let {
                it.toIntOrNull()?.takeIf { value -> value >= 0 } ?: throw IllegalArgumentException("Harvesters must be a non-negative whole number.")
            }
            val draft = ProductionDraft(date, field, numeric(harvested), numeric(mill), numeric(backlog), numeric(hectares), count)
            draft.validate()
            save(draft) { failure -> notice = failure ?: "Draft saved on this device. Pending upload in Settings → Sync Now." }
        } catch (failure: Exception) { notice = failure.message ?: "Check the entered values." }
    }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "Saving…" else "Save Draft") }
    notice?.let { Text(it) }
}

@Composable
private fun CropDashboardSummary() {
    val context = LocalContext.current
    val dao = remember(context) { EstateDatabase.get(context).estateDao() }
    val snapshot by dao.observeSnapshot().collectAsState(initial = null)
    val update = remember(snapshot?.content) { snapshot?.let { EstatePackages.parse(it.content) } }
    val today by produceState(estateRainfallToday()) {
        while (true) { value = estateRainfallToday(); kotlinx.coroutines.delay(60_000) }
    }
    CropProductionDashboardTable(cropProductionTable(update, today))
    FieldCropRecords(update)
}

@Composable
private fun FieldCropRecords(update: EstatePackage?) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    Text("Field Crop Records", style = MaterialTheme.typography.titleMedium)
    estateFieldCodes(update).chunked(3).forEach { group ->
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            group.forEach { code ->
                FilterChip(selected = selected == code, onClick = { selected = if (selected == code) null else code }, label = { Text(code) })
            }
        }
    }
    selected?.let { code ->
        val tables = estateFieldCropTables(update, code)
        if (tables.isEmpty()) Text("Crop records pending for this field.")
        tables.forEach { table ->
            ProductionCard("$code · Crop Records") {
                table.rows.forEach { row ->
                    table.columns.zip(row).forEach { (label, value) -> Text("$label: ${value ?: "Pending"}") }
                }
            }
        }
    }
}

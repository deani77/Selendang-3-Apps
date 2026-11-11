package com.selendang3.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.selendang3.app.BuildConfig
import com.selendang3.app.data.EstateSnapshot
import java.text.DateFormat
import java.util.Date

@Composable
internal fun EstateAppFooter(snapshot: EstateSnapshot?, lastSyncEpochMillis: Long?, online: Boolean) {
    val lastSync = lastSyncEpochMillis?.let { DateFormat.getDateTimeInstance().format(Date(it)) }
    Text("v${BuildConfig.VERSION_NAME}  •  ${if (online) "Online" else "Offline"}\nLast update: ${snapshot?.let { DateFormat.getDateTimeInstance().format(Date(it.updatedAt)) + " · " + it.source } ?: "No data yet"}\nLast VPS sync: ${lastSync ?: "Not synced yet"}",
        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), textAlign = TextAlign.Center)
}

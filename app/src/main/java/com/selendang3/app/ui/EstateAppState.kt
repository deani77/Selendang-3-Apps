package com.selendang3.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.selendang3.app.connectivity
import com.selendang3.app.data.EstateDatabase
import com.selendang3.app.data.EstatePackage
import com.selendang3.app.data.EstatePackages
import com.selendang3.app.data.EstateSnapshot

internal data class EstateAppState(
    val themeChoice: String,
    val estateUpdate: EstatePackage?,
    val snapshot: EstateSnapshot?,
    val lastSyncEpochMillis: Long?,
    val online: Boolean,
    val onThemeChange: (String) -> Unit,
)

@Composable
internal fun rememberEstateAppState(): EstateAppState {
    val context = LocalContext.current.applicationContext
    val preferences = remember(context) { context.getSharedPreferences("user_settings", 0) }
    var themeChoice by remember { mutableStateOf(preferences.getString("theme", "system") ?: "system") }
    val dao = remember(context) { EstateDatabase.get(context).estateDao() }
    val snapshot by remember(dao) { dao.observeSnapshot() }.collectAsStateWithLifecycle(null)
    val estateUpdate = remember(snapshot?.content) { snapshot?.let { EstatePackages.parse(it.content) } }
    val sync by remember(dao) { dao.observeSync() }.collectAsStateWithLifecycle(null)
    val online by remember(context) { connectivity(context) }.collectAsStateWithLifecycle(false)
    return EstateAppState(themeChoice, estateUpdate, snapshot, sync?.lastSuccessEpochMillis, online) { choice ->
        themeChoice = choice
        preferences.edit().putString("theme", choice).apply()
    }
}

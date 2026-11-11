package com.selendang3.app.ui.components

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Match system bars to the estate palette while this dashboard is visible.
@Suppress("DEPRECATION")
@Composable
internal fun DashboardSystemBars(dark: Boolean) {
    val window = (LocalContext.current as? Activity)?.window ?: return
    val view = LocalView.current
    val background = MaterialTheme.colorScheme.background.toArgb()
    DisposableEffect(window, view, background, dark) {
        val controller = WindowCompat.getInsetsController(window, view)
        val oldStatus = window.statusBarColor
        val oldNavigation = window.navigationBarColor
        val oldLightStatus = controller.isAppearanceLightStatusBars
        val oldLightNavigation = controller.isAppearanceLightNavigationBars
        window.statusBarColor = background
        window.navigationBarColor = background
        controller.isAppearanceLightStatusBars = !dark
        controller.isAppearanceLightNavigationBars = !dark
        onDispose {
            window.statusBarColor = oldStatus
            window.navigationBarColor = oldNavigation
            controller.isAppearanceLightStatusBars = oldLightStatus
            controller.isAppearanceLightNavigationBars = oldLightNavigation
        }
    }
}

package com.selendang3.app

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.selendang3.app.ui.components.HomeSectionGrid
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EstatePillsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun fillsRowsAndFinalPillAndUsesEqualHeights() {
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(320.dp).testTag("boundary")) {
                    EstatePillSelector(listOf("Crop (mt)", "Yield (mt/ha)", "Achievement (%)"), "Crop (mt)", {}) { it }
                }
            }
        }
        val boundary = compose.onNodeWithTag("boundary").fetchSemanticsNode().boundsInRoot
        val pills = listOf("Crop (mt)", "Yield (mt/ha)", "Achievement (%)").map {
            compose.onNodeWithText(it).fetchSemanticsNode().boundsInRoot
        }
        assertEquals(boundary.left, pills[0].left, 1f)
        assertEquals(boundary.right, pills[1].right, 1f)
        assertEquals(boundary.left, pills[2].left, 1f)
        assertEquals(boundary.right, pills[2].right, 1f)
        pills.forEach { assertEquals(pills[0].height, it.height, 1f) }
    }

    @Test fun landscapeHomePlacesFullWidthDashboardAboveEqualSizedShortcuts() {
        val opened = mutableListOf<String>()
        compose.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                orientation = Configuration.ORIENTATION_LANDSCAPE
                fontScale = 1f
            }
            val density = Density(LocalDensity.current.density, 1f)
            CompositionLocalProvider(LocalConfiguration provides configuration, LocalDensity provides density) {
                MaterialTheme {
                    Box(Modifier.width(320.dp).testTag("boundary")) {
                        HomeSectionGrid { opened.add(it) }
                    }
                }
            }
        }
        val boundary = compose.onNodeWithTag("boundary").fetchSemanticsNode().boundsInRoot
        val dashboard = compose.onNodeWithContentDescription("Open Estate Dashboard")
        val tools = compose.onNodeWithText("Planter Tools")
        val dashboardBounds = dashboard.fetchSemanticsNode().boundsInRoot
        val toolsBounds = tools.fetchSemanticsNode().boundsInRoot
        assertEquals(boundary.left, dashboardBounds.left, 1f)
        assertEquals(boundary.right, dashboardBounds.right, 1f)
        assertTrue(toolsBounds.width < dashboardBounds.width)
        assertTrue(toolsBounds.top >= dashboardBounds.bottom)
        for (title in listOf("Estate Details", "Field Details", "Upkeep and Cultivation", "Crop Production", "Settings")) {
            val bounds = compose.onNodeWithText(title).fetchSemanticsNode().boundsInRoot
            assertTrue(bounds.top >= dashboardBounds.bottom)
            assertTrue(bounds.top <= toolsBounds.top)
            assertEquals(toolsBounds.width, bounds.width, 1f)
            assertEquals(toolsBounds.height, bounds.height, 1f)
        }
        dashboard.performClick()
        tools.performClick()
        compose.runOnIdle { assertEquals(listOf("dashboard", "tools"), opened) }
    }

    @Test fun largeTextStacksWithoutTruncationAndSelectionStillWorks() {
        val selection = mutableStateOf("Short")
        var overflow = false
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                MaterialTheme {
                    Box(Modifier.width(280.dp).testTag("boundary")) {
                        EstatePillGrid(2) { index ->
                            val label = if (index == 0) "Short" else "Roads and Infrastructure Management"
                            androidx.compose.material3.FilledTonalButton(onClick = { selection.value = label }) {
                                androidx.compose.material3.Text(label, onTextLayout = { if (it.hasVisualOverflow) overflow = true })
                            }
                        }
                    }
                }
            }
        }
        val first = compose.onNodeWithText("Short").fetchSemanticsNode().boundsInRoot
        val last = compose.onNodeWithText("Roads and Infrastructure Management").fetchSemanticsNode().boundsInRoot
        assertEquals(first.width, last.width, 1f)
        assertEquals(first.height, last.height, 1f)
        assert(last.top >= first.bottom)
        compose.onNodeWithText("Roads and Infrastructure Management").performClick()
        compose.runOnIdle {
            assertFalse(overflow)
            assertEquals("Roads and Infrastructure Management", selection.value)
        }
    }
}

package com.selendang3.app.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.selendang3.app.EstateControlHeight
import com.selendang3.app.EstateModuleIcon
import com.selendang3.app.EstatePillGrid
import com.selendang3.app.navigation.Section
import com.selendang3.app.navigation.mainSections

@Composable
internal fun HomeSectionGrid(onOpen: (String) -> Unit) {
    val dashboard = Section("dashboard", "Estate Dashboard", "")
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        val tools = mainSections.first { it.route == "tools" }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionButton(dashboard, onOpen, minHeight = 96.dp)
            SectionGrid(mainSections - tools, maxColumns = 5, fillLastRow = false, onOpen = onOpen)
            SectionButton(tools, onOpen, minHeight = 96.dp)
        }
    } else {
        SectionGrid(listOf(dashboard) + mainSections, onOpen = onOpen)
    }
}

@Composable
internal fun SectionGrid(
    sections: List<Section>,
    maxColumns: Int = 2,
    fillLastRow: Boolean = true,
    onOpen: (String) -> Unit,
) {
    EstatePillGrid(sections.size, maxColumns = maxColumns, fillLastRow = fillLastRow) { index ->
        SectionButton(sections[index], onOpen)
    }
}

@Composable
private fun SectionButton(section: Section, onOpen: (String) -> Unit, minHeight: Dp = EstateControlHeight) {
    FilledTonalButton(
        onClick = { onOpen(section.route) },
        modifier = Modifier.fillMaxWidth().heightIn(min = minHeight).semantics {
            if (section.route == "dashboard") contentDescription = "Open Estate Dashboard"
        },
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EstateModuleIcon(section.route, Modifier.size(26.dp), MaterialTheme.colorScheme.primary)
            Text(section.title, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
        }
    }
}

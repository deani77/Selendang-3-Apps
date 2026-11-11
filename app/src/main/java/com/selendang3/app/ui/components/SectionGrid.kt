package com.selendang3.app.ui.components

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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.selendang3.app.EstateControlHeight
import com.selendang3.app.EstateModuleIcon
import com.selendang3.app.EstatePillGrid
import com.selendang3.app.navigation.Section

@Composable
internal fun SectionGrid(sections: List<Section>, onOpen: (String) -> Unit) {
    EstatePillGrid(sections.size, maxColumns = 2) { index ->
        val section = sections[index]
        FilledTonalButton(onClick = { onOpen(section.route) },
            modifier = Modifier.fillMaxWidth().heightIn(min = EstateControlHeight).semantics {
                if (section.route == "dashboard") contentDescription = "Open Estate Dashboard"
            },
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EstateModuleIcon(section.route, Modifier.size(26.dp), MaterialTheme.colorScheme.primary)
                Text(section.title, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

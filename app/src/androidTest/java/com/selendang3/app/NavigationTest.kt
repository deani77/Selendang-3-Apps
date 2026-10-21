package com.selendang3.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun informationOpensRainfallAndBackReturnsToHome() {
        compose.onNodeWithText("Estate Information").performScrollTo().performClick()
        compose.onNodeWithText("Rainfall").performScrollTo().performClick()
        compose.onNodeWithText("Monthly rainfall, rainy days and historical comparisons.").assertIsDisplayed()
        compose.onNodeWithText("‹  Back").performClick()
        compose.onNodeWithText("‹  Back").performClick()
        compose.onNodeWithText("S3 Estate Info").assertIsDisplayed()
    }
    @Test fun productionOpensAndCanSaveLocalDraft() {
        compose.onNodeWithText("Estate Production").performScrollTo().performClick()
        compose.onNodeWithText("Production by field").assertIsDisplayed()
        compose.onNodeWithText("PM04D").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Crop sent to mill (t)").performScrollTo().performTextReplacement("2.5")
        compose.onNodeWithText("Save Draft").performScrollTo().performClick()
        compose.onNodeWithText("Draft saved on this device. Pending upload in Settings → Sync Now.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Month Crop Production").performScrollTo().performClick()
        compose.onNodeWithText("Crop sent to mill: 2.50 t · 1/1 saved days reported").performScrollTo().assertIsDisplayed()
    }
}

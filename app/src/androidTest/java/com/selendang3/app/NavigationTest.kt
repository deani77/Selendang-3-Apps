package com.selendang3.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun detailsShowsMovedPanelsAndDashboardIsPlaceholder() {
        compose.onNodeWithText("Estate Details").performScrollTo().performClick()
        compose.onNodeWithText("ESTATE DETAILS · SELENDANG 3").assertIsDisplayed()
        compose.onNodeWithText("Age Profile").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Rainfall").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Checkroll Average").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("‹  Back").performClick()
        compose.onNodeWithContentDescription("Open Estate Dashboard").performScrollTo().performClick()
        compose.onNodeWithText("Dashboard contents will be added later. Estate panels are now in Estate Details.").assertIsDisplayed()
        compose.onNodeWithText("Age Profile").assertDoesNotExist()
        compose.onNodeWithText("‹  Back").performClick()
        compose.onNodeWithText("S3 Estate Info").assertIsDisplayed()
    }
    @Test fun homeModulesAndAboutInSettings() {
        compose.onNodeWithText("Reports").assertDoesNotExist()
        compose.onNodeWithText("About").assertDoesNotExist()
        compose.onNodeWithText("Field Details").performScrollTo().performClick()
        compose.onNodeWithText("Field Dashboard").assertIsDisplayed()
        compose.onNodeWithText("Production by field").assertDoesNotExist()
        compose.onNodeWithText("‹  Back").performClick()
        compose.onNodeWithText("Planter Tools").performScrollTo().performClick()
        compose.onNodeWithText("Planter tools will be added later.").assertIsDisplayed()
        compose.onNodeWithText("‹  Back").performClick()
        compose.onNodeWithText("Settings").performScrollTo().performClick()
        compose.onNodeWithText("Suggestions").assertDoesNotExist()
        compose.onNodeWithText("Estate Data Sync").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("About").performScrollTo().performClick()
        compose.onNodeWithText("Android estate information application. Initial Alpha interface.").assertIsDisplayed()
        compose.onNodeWithText("‹ Settings").performScrollTo().performClick()
        compose.onNodeWithText("User Profile").performScrollTo().assertIsDisplayed()
    }
    @Test fun cropProductionOpensAndCanSaveLocalDraft() {
        compose.onNodeWithText("Crop Production").performScrollTo().performClick()
        compose.onNodeWithText("Crop Production Dashboard").assertIsDisplayed()
        compose.onNodeWithText("Production by field").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("PM04D").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Crop sent to mill (t)").performScrollTo().performTextReplacement("2.5")
        compose.onNodeWithText("Save Draft").performScrollTo().performClick()
        compose.onNodeWithText("Draft saved on this device. Pending upload in Settings → Sync Now.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Month Crop Production").performScrollTo().performClick()
        compose.onNodeWithText("Crop sent to mill: 2.50 t · 1/1 saved days reported").performScrollTo().assertIsDisplayed()
    }
}

package com.selendang3.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun detailsShowsPanelsAndPhoneDashboardOpensSummary() {
        compose.onNodeWithText("ESTATE INFORMATION").assertIsDisplayed()
        compose.onNodeWithText("Crop Data (mt/ha)").assertDoesNotExist()
        compose.onNodeWithText("Estate Details").performScrollTo().performClick()
        compose.onNodeWithText("ESTATE DETAILS").assertIsDisplayed()
        compose.onNodeWithText("Age Profile").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Rainfall").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("OER (%)").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Checkroll Average").assertDoesNotExist()
        compose.onNodeWithText("Cost Breakdown").assertDoesNotExist()
        compose.onNodeWithText("Actual versus Budget · YTD").assertDoesNotExist()
        compose.onNodeWithText("KER (%)").assertDoesNotExist()
        compose.onNodeWithText("External Supply (t)").assertDoesNotExist()
        compose.onNodeWithText("LF").assertDoesNotExist()
        compose.onNodeWithText("‹  Back").performClick()
        compose.onNodeWithContentDescription("Open Estate Dashboard").performScrollTo().performClick()
        compose.onNodeWithText("Crop Production").assertIsDisplayed()
        compose.onNodeWithText("Manuring").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Estate Cost").performScrollTo().assertIsDisplayed()
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
        compose.onNodeWithText("Plant Density").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Chemical Dosage per Pump").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fertilizer Requirement").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Nozzle Calibration").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Work Costing").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Unit Conversion").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("‹  Back").performClick()
        compose.onNodeWithText("Settings").performScrollTo().performClick()
        compose.onNodeWithText("Suggestions").assertDoesNotExist()
        compose.onNodeWithText("Estate Data Sync").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("About").performScrollTo().performClick()
        compose.onNodeWithText("Android estate information application. Initial Alpha interface.").assertIsDisplayed()
        compose.onNodeWithText("‹ Settings").performScrollTo().performClick()
        compose.onNodeWithText("User Profile").performScrollTo().assertIsDisplayed()
    }
    @Test fun cropProductionIsServerManagedAndHasNoDataEntry() {
        compose.onNodeWithText("Crop Production").performScrollTo().performClick()
        compose.onNodeWithText("Crop Production Dashboard").assertIsDisplayed()
        compose.onNodeWithText("Field Crop Records").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Save Draft").assertDoesNotExist()
        compose.onNodeWithText("Crop sent to mill (t)").assertDoesNotExist()
        compose.onNodeWithText("Today Crop Production").assertDoesNotExist()
        compose.onNodeWithText("Upload PDF · coming next").assertDoesNotExist()
    }
    @Test fun estateRecordRoutesReturnToTheirGrid() {
        compose.onNodeWithText("Estate Details").performScrollTo().performClick()
        compose.onNodeWithText("All Estate Records").performClick()
        val records = listOf(
            "Estate Overview" to "Estate profile, location, cultivation and maturity status.",
            "Field Details" to "Field codes, hectarage, palm population and density.",
            "Palm Age Profile" to "Palm ages by field and age-category distribution.",
            "Soil & Topography" to "Soil composition, terrain and corresponding areas.",
            "Rainfall" to "Monthly rainfall, rainy days and historical comparisons.",
            "Average Bunch Weight" to "Current-period and cumulative ABW in kg/bunch, with year-on-year comparison. Field-level figures are pending the supporting Excel file.",
            "Roads & Infrastructure" to "Main, secondary and subsidiary road specifications.",
        )
        for ((title, description) in records) {
            compose.onNodeWithText(title).performScrollTo().performClick()
            compose.onNodeWithText(description).assertIsDisplayed()
            compose.onNodeWithText("‹  Back").performClick()
            compose.onNodeWithText("All Estate Records").assertIsDisplayed()
        }
        compose.onNodeWithText("‹  Back").performClick()
        compose.onNodeWithText("ESTATE DETAILS").assertIsDisplayed()
        compose.onNodeWithText("‹  Back").performClick()
        compose.onNodeWithText("Planter’s Log").assertIsDisplayed()
    }

    @Test fun appearanceChoiceSurvivesNavigationAndActivityRecreation() {
        val preferences = InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("user_settings", 0)
        val original = preferences.getString("theme", null)
        try {
            compose.onNodeWithText("Settings").performScrollTo().performClick()
            compose.onNodeWithText("Appearance").performScrollTo().performClick()
            compose.onNodeWithText("Light", substring = true).performScrollTo().performClick()
            compose.onNodeWithText("✓ Light").assertIsDisplayed()
            compose.onNodeWithText("Dark", substring = true).performScrollTo().performClick()
            compose.onNodeWithText("✓ Dark").assertIsDisplayed()
            compose.onNodeWithText("‹  Back").performClick()
            compose.onNodeWithText("Settings").performScrollTo().performClick()
            compose.onNodeWithText("Appearance").performScrollTo().performClick()
            compose.onNodeWithText("✓ Dark").assertIsDisplayed()
            compose.activityRule.scenario.recreate()
            compose.onNodeWithText("✓ Dark").assertIsDisplayed()
        } finally {
            val editor = preferences.edit()
            if (original == null) editor.remove("theme") else editor.putString("theme", original)
            editor.commit()
        }
    }

}

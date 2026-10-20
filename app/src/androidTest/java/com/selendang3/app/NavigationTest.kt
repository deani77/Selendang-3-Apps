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
}

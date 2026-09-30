package com.interviewprep.brokencalc

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class CalculatorScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun press(vararg keys: String) {
        for (key in keys) {
            composeRule.onNodeWithText(key).performClick()
        }
    }

    @Test
    fun twoPlusThreeIsFive() {
        press("2", "+", "3", "=")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("5").assertIsDisplayed()
    }

    @Test
    fun tenMinusFiveMinusTwoIsThree() {
        press("1", "0", "−", "5", "−", "2", "=")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("3").assertIsDisplayed()
    }
}

package cz.smycka.example

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalTestApi
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun checkUserNameInputVerifications() {
        composeTestRule.apply {
            onNodeWithTag("password").performTextInput("password")
            onNodeWithTag("Confirm").performClick()
            onNodeWithText(composeTestRule.activity.getString(R.string.error_empty_input)).assertExists()

            onNodeWithTag("userName").performTextInput("Hello")
            onNodeWithTag("Confirm").performClick()
            onNodeWithText(composeTestRule.activity.getString(R.string.error_lower_caps_only)).assertExists()
        }
    }

    @Test
    fun checkPasswordInputVerifications() {
        composeTestRule.apply {
            onNodeWithTag("userName").performTextInput("username")
            onNodeWithTag("Confirm").performClick()
            onNodeWithText(composeTestRule.activity.getString(R.string.error_empty_input)).assertExists()

            onNodeWithTag("password").performTextInput("Hello")
            onNodeWithTag("Confirm").performClick()
            onNodeWithText(composeTestRule.activity.getString(R.string.error_lower_caps_only)).assertExists()
        }
    }

    @Test
    fun signInWithValidCredentials() {
        val username = "success"
        composeTestRule.apply {
            onNodeWithTag("userName").performTextInput(username)
            onNodeWithTag("password").performTextInput("password")
            onNodeWithTag("Confirm").performClick()

            waitUntilDoesNotExist(hasTestTag("progress"))

            onNodeWithText(username).assertExists()
        }
    }

    @Test
    fun signInWithInvalidCredentials() {
        composeTestRule.apply {
            onNodeWithTag("userName").performTextInput("wrongp")
            onNodeWithTag("password").performTextInput("invalid")
            onNodeWithTag("Confirm").performClick()

            waitUntilDoesNotExist(hasTestTag("progress"))

            onNodeWithText(composeTestRule.activity.getString(R.string.error_unauthorized)).assertExists()
        }
    }
}

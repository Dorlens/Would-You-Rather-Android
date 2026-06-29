package com.example.wouldyourather

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResultScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testResultScreenHeader() {
        val question = FirestoreQuestion(
            optionA = "Option A",
            optionB = "Option B",
            votesA = 10,
            votesB = 20,
            totalVotes = 30
        )
        
        composeTestRule.setContent {
            ResultScreen(
                question = question,
                selectedOption = "A",
                onNext = {},
                onHome = {}
            )
        }

        // ResultScreen.kt uses "Results" in ResultHeader
        composeTestRule.onNodeWithText("Results").assertIsDisplayed()
    }

    @Test
    fun testVotingPercentages() {
        val votesA = 10L
        val votesB = 40L
        val total = votesA + votesB // 50
        
        // Percentages: A = (10/50)*100 = 20%, B = (40/50)*100 = 80%
        
        val question = FirestoreQuestion(
            optionA = "Apple",
            optionB = "Banana",
            votesA = votesA,
            votesB = votesB,
            totalVotes = total
        )

        composeTestRule.setContent {
            ResultScreen(
                question = question,
                selectedOption = "A",
                onNext = {},
                onHome = {}
            )
        }

        // VoteBar displays "$percentage%"
        composeTestRule.onNodeWithText("20%").assertIsDisplayed()
        composeTestRule.onNodeWithText("80%").assertIsDisplayed()

    }
}

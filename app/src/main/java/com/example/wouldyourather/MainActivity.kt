package com.example.wouldyourather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wouldyourather.ui.theme.WouldYouRatherTheme

class MainActivity : ComponentActivity() {
    companion object {
        const val ROUTE_HOME = "home"
        const val ROUTE_PLAY = "play"
        const val ROUTE_RESULTS = "results/{questionId}/{selectedOption}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WouldYouRatherTheme {
                val viewModel: GameViewModel = viewModel()
                val navController = rememberNavController()
                val questions by viewModel.questions
                val currentQuestion by viewModel.currentQuestion

                NavHost(navController = navController, startDestination = ROUTE_HOME) {
                    composable(ROUTE_HOME) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                GameBackground(
                                    onPlayNow = {
                                        navController.navigate(ROUTE_PLAY) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                    composable(ROUTE_PLAY) {
                        PlayScreen(
                            question = currentQuestion,
                            onBack = { navController.popBackStack() },
                            onOptionSelected = { question, option ->
                                viewModel.submitVote(question, option)
                                navController.navigate("results/${question.questionId}/$option")
                            }
                        )
                    }
                    composable(
                        route = ROUTE_RESULTS,
                        arguments = listOf(
                            navArgument("questionId") { type = NavType.StringType },
                            navArgument("selectedOption") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val questionId = backStackEntry.arguments?.getString("questionId")
                        val selectedOption = backStackEntry.arguments?.getString("selectedOption") ?: "A"
                        val question = questions.find { it.questionId == questionId }
                        
                        if (question != null) {
                            ResultScreen(
                                question = question,
                                selectedOption = selectedOption,
                                onNext = {
                                    viewModel.loadNextQuestion()
                                    navController.popBackStack(ROUTE_HOME, false)
                                    navController.navigate(ROUTE_PLAY)
                                },
                                onHome = {
                                    navController.popBackStack(ROUTE_HOME, false)
                                }
                            )
                        } else {
                            // Fallback if question not found
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Result not found", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameBackground(onPlayNow: () -> Unit) {
    val scrollState = rememberScrollState()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showSheet) {
        HowToPlayBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            onLetGo = {
                showSheet = false
                onPlayNow()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0E17))
    ) {

        // --- BACKGROUND DECORATIONS ---
        // Orange glow (Top Right)
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-80).dp)
                .background(
                    Color(0xFFFF6B35).copy(alpha = 0.2f),
                    CircleShape
                )
        )

        // Teal glow (Bottom Left)
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 80.dp)
                .background(
                    Color(0xFF4ECDC4).copy(alpha = 0.2f),
                    CircleShape
                )
        )

        // Yellow square accent
        Box(
            modifier = Modifier
                .size(40.dp)
                .offset(x = 300.dp, y = 400.dp)
                .rotate(12f)
                .background(
                    Color(0xFFFFE66D).copy(alpha = 0.15f),
                    RectangleShape
                )
        )

        // --- MAIN CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Emoji
            Text(
                text = "🤔",
                fontSize = 40.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFF6B35), Color(0xFFFF3CAC))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Would You",
                    color = Color.White,
                    fontSize = 40.sp,
                    lineHeight = 52.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rather?",
                    color = Color(0xFFFF6B35),
                    fontSize = 40.sp,
                    lineHeight = 52.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline
            Text(
                text = "Impossible choices. No Wrong Answers. Pure chaos.",
                color = Color.Gray,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Options Row (Responsive)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Option A
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .background(Color(0xFFFF6B35), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Option A", color = Color(0xFF0F0E17), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // OR badge
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(44.dp)
                        .background(Color(0xFFFFE66D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("OR", color = Color(0xFF0F0E17), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Option B
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .background(Color(0xFF4ECDC4), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Option B", color = Color(0xFF0F0E17), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(100.dp))

            // Play Now Button
            Button(
                onClick = onPlayNow,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFF6B35), Color(0xFFFF3CAC))
                        ),
                        shape = CircleShape
                    )
            ) {
                Text("Play Now", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // How to Play Button
            Button(
                onClick = { showSheet = true },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.06f),
                    contentColor = Color.White.copy(alpha = 0.75f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.75f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionMark,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White.copy(alpha = 0.75f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("How to Play", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameBackgroundPreview() {
    WouldYouRatherTheme {
        GameBackground(onPlayNow = {})
    }
}

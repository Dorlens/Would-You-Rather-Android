package com.example.wouldyourather

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Question(
    val optionA: String,
    val optionB: String,
    val colorA: Color,
    val colorB: Color
)

@Composable
fun PlayScreen(onBack: () -> Unit) {
    val questions = listOf(
        Question("Option A", "Option B", Color(0xFFFF6B35), Color(0xFF4ECDC4)),
        Question("Option A", "Option B", Color(0xFFFF3CAC), Color(0xFFFFE66D)),
        Question("Option A", "Option B", Color(0xFFFF6B35), Color(0xFF784BA0)),
        Question("Option A", "Option B", Color(0xFF4ECDC4), Color(0xFFFF3CAC)),
        Question("Option A", "Option B", Color(0xFFFFE66D), Color(0xFFFF6835))
    )

    var currentQuestionIndex by remember { mutableStateOf(0) }
    val currentQuestion = questions[currentQuestionIndex]

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0F0E17)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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

            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Would You Rather?",
                color = Color.Gray,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 115.dp, top = 30.dp)
            )
            Text(
                text = "Would You rather.....",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 230.dp)
            )

            // Options Section
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 40.dp)
                    .fillMaxWidth(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Option A Button
                Button(
                    onClick = {
                        currentQuestionIndex = (currentQuestionIndex + 1) % questions.size
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E26)),
                    border = BorderStroke(0.1.dp, Color.Gray),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = currentQuestion.optionA,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = currentQuestion.colorA,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 8.dp, top = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 0.1.dp,
                        color = Color.Gray
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(36.dp)
                            .background(Color(0xFFFFE66D), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OR",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 0.1.dp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option B Button
                Button(
                    onClick = {
                        currentQuestionIndex = (currentQuestionIndex + 1) % questions.size
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E26)),
                    border = BorderStroke(0.1.dp, Color.Gray),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = currentQuestion.optionB,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = currentQuestion.colorB,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 8.dp, top = 8.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    Text(
                        text = "Tap a card to choose",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

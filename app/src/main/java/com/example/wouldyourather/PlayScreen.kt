package com.example.wouldyourather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.delay

@Composable
fun PlayScreen(
    question: FirestoreQuestion?,
    history: List<HistoryEntry>,
    isLoading: Boolean,
    isGameOver: Boolean,
    timerSeconds: Int,
    onBack: () -> Unit,
    onOptionSelected: (FirestoreQuestion, String) -> Unit,
    onVote: (FirestoreQuestion, String) -> Unit,
    onNavigateToGameOver: () -> Unit,
    onResetGame: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Navigation trigger for Game Over
    LaunchedEffect(isGameOver, history.size, question) {
        if (isGameOver && question == null) {
            if (history.isNotEmpty()) {
                onNavigateToGameOver()
            } else {
                // If they never played anything, just go back
                onBack()
            }
        }
    }

    // Reset selection when question changes
    LaunchedEffect(question?.questionId) {
        selectedOption = null
        isSubmitting = false
    }

    LaunchedEffect(selectedOption) {
        if (selectedOption != null && question != null && !isSubmitting) {
            isSubmitting = true
            delay(500)
            onOptionSelected(question, selectedOption!!)
            onVote(question, selectedOption!!)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0F0E17)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PlayScreenBackground()

            if (isLoading && question == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFFFF6B35))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Fetching impossible choices...", color = Color.Gray)
                }
            } else if (question == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFFF6B35))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading choices...", color = Color.Gray)
                    }
                }
            } else {
                val colorA = try { Color(question.colorA.toColorInt()) } catch (e: Exception) { Color(0xFFFF6B35) }
                val colorB = try { Color(question.colorB.toColorInt()) } catch (e: Exception) { Color(0xFF3A86FF) }

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
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
                        modifier = Modifier.padding(start = 45.dp)
                    )
                }

                // Options Section
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.8f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Inactivity Timer Bar
                    if (selectedOption == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(timerSeconds / 30f)
                                    .fillMaxHeight()
                                    .background(
                                        if (timerSeconds < 10) Color.Red else Color(0xFF00FF00), // Brighter Green
                                        CircleShape
                                    )
                            )
                        }
                        Text(
                            text = "Ends in ${timerSeconds}s",
                            color = if (timerSeconds < 10) Color.Red else Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                        )
                    }

                    Text(
                        text = question.question,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    OptionCard(
                        label = "Option A",
                        text = question.optionA,
                        accentColor = colorA,
                        isSelected = selectedOption == "A",
                        isDimmed = selectedOption == "B",
                        isEnabled = selectedOption == null && timerSeconds > 0,
                        onClick = { selectedOption = "A" }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .alpha(if (selectedOption != null) 0.4f else 1f)
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

                    OptionCard(
                        label = "Option B",
                        text = question.optionB,
                        accentColor = colorB,
                        isSelected = selectedOption == "B",
                        isDimmed = selectedOption == "A",
                        isEnabled = selectedOption == null && timerSeconds > 0,
                        onClick = { selectedOption = "B" }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tap a card to choose",
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayScreenBackground() {
    Box(
        modifier = Modifier
            .size(260.dp)
            .offset(x = 180.dp, y = (-80).dp)
            .background(Color(0xFFFF6B35).copy(alpha = 0.2f), CircleShape)
    )
    Box(
        modifier = Modifier
            .size(220.dp)
            .offset(x = (-80).dp, y = 500.dp)
            .background(Color(0xFF4ECDC4).copy(alpha = 0.2f), CircleShape)
    )
}

@Composable
fun OptionCard(
    label: String,
    text: String,
    accentColor: Color,
    isSelected: Boolean,
    isDimmed: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.4f else 1f,
        animationSpec = tween(400),
        label = "AlphaAnimation"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color.White.copy(alpha = 0.1f),
        animationSpec = tween(400),
        label = "BorderColorAnimation"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f),
        animationSpec = tween(400),
        label = "BackgroundColorAnimation"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.15f else 0f,
        animationSpec = tween(400),
        label = "GlowAlphaAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .alpha(alpha)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = isEnabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(accentColor.copy(alpha = glowAlpha), CircleShape)
            )
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            color = backgroundColor,
            border = BorderStroke(if (isSelected) 1.dp else 0.1.dp, borderColor)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 12.dp)
                )

                Text(
                    text = if (text.isEmpty()) "Missing Text in DB" else text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (text.isEmpty()) Color.Red else Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp)
                )

                AnimatedVisibility(
                    visible = isSelected,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    enter = fadeIn() + scaleIn(initialScale = 0.5f),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (accentColor == Color(0xFFFFE66D)) Color.Black else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

package com.example.wouldyourather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

@Composable
fun ResultScreen(
    question: FirestoreQuestion,
    selectedOption: String,
    onNext: () -> Unit,
    onHome: () -> Unit
) {
    val totalVotes = question.totalVotes.coerceAtLeast(1)
    val percentageA = ((question.votesA.toFloat() / totalVotes) * 100).toInt()
    val percentageB = ((question.votesB.toFloat() / totalVotes) * 100).toInt()
    
    val colorA = try { Color(question.colorA.toColorInt()) } catch (e: Exception) { Color(0xFFFF6B35) }
    val colorB = try { Color(question.colorB.toColorInt()) } catch (e: Exception) { Color(0xFF3A86FF) }

    val isMajority = if (selectedOption == "A") percentageA > 50 else percentageB > 50
    
    // Define colors based on user choice
    val primaryResultColor = if (selectedOption == "A") colorA else colorB
    val secondaryResultColor = if (selectedOption == "A") colorB else colorA

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { it / 10 }, animationSpec = tween(500))
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFF0F0E17)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Pass the primary selected color to the background
                ResultScreenBackground(primaryResultColor)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ResultHeader(onHome)

                    Spacer(modifier = Modifier.height(24.dp))

                    ResultStatus(isMajority, primaryResultColor)

                    Spacer(modifier = Modifier.height(32.dp))

                    ChoiceCard(
                        text = if (selectedOption == "A") question.optionA else question.optionB,
                        accentColor = primaryResultColor
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    VoteBar(
                        label = "A",
                        text = question.optionA,
                        percentage = percentageA,
                        color = colorA,
                        isSelected = selectedOption == "A"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    VoteBar(
                        label = "B",
                        text = question.optionB,
                        percentage = percentageB,
                        color = colorB,
                        isSelected = selectedOption == "B"
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    ResultButtons(onNext, onHome)

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ResultScreenBackground(selectedColor: Color) {
    // Top Right glow uses the color of the choice you picked
    Box(
        modifier = Modifier
            .size(260.dp)
            .offset(x = 180.dp, y = (-80).dp)
            .background(selectedColor.copy(alpha = 0.2f), CircleShape)
            .blur(60.dp)
    )

    // Teal glow (Bottom Left) - Keep this as a constant secondary accent
    Box(
        modifier = Modifier
            .size(220.dp)
            .offset(x = (-80).dp, y = 500.dp)
            .background(Color(0xFF4ECDC4).copy(alpha = 0.15f), CircleShape)
            .blur(60.dp)
    )
}

@Composable
fun ResultHeader(onHome: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onHome,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Home",
                tint = Color.White
            )
        }
        Text(
            text = "Results",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ResultStatus(isMajority: Boolean, accentColor: Color) {
    val emoji = if (isMajority) "🔥" else "🦄"
    val message = if (isMajority) "YOU'RE WITH THE CROWD!" else "YOU'RE ONE OF A KIND!"
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(600)) + scaleIn(spring(dampingRatio = 0.5f, stiffness = 200f))
        ) {
            Text(text = emoji, fontSize = 64.sp)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            color = accentColor, // Text color now matches the choice color
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChoiceCard(text: String, accentColor: Color) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(800)) + scaleIn(initialScale = 0.9f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Soft Centered Outer Glow (No Offset)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(70.dp)
                    .blur(35.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1E1E26),
                border = BorderStroke(1.dp, Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.8f), accentColor.copy(alpha = 0.2f))))
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Check Circle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(accentColor.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "YOUR CHOICE",
                            color = accentColor.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = text,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 26.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoteBar(label: String, text: String, percentage: Int, color: Color, isSelected: Boolean) {
    val progress = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = percentage / 100f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = Modifier.fillMaxWidth().alpha(if (isSelected) 1f else 0.7f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically // Better centering for text
        ) {
            Row(
                modifier = Modifier.weight(1f), // Take all space except for the percentage
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = color,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(18.dp) // Fixed width for A/B labels
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$percentage%",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.End
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.value)
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}

@Composable
fun ResultButtons(onNext: () -> Unit, onHome: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFF6B35), Color(0xFFFF3CAC))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Next Question",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        ) {
            Text(
                text = "Back to Home",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

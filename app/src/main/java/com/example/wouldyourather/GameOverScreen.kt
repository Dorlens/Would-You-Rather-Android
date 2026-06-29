package com.example.wouldyourather

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import kotlin.math.roundToInt

@Composable
fun GameOverScreen(
    history: List<HistoryEntry>,
    onReplay: () -> Unit,
    onHome: () -> Unit
) {
    // Capture the state when history was NOT empty to prevent flickering when history is cleared during transition
    val finalHistory = remember(history) {
        if (history.isNotEmpty()) history else null
    } ?: return // Don't render anything if we never had history (shouldn't happen)

    val withCrowd = finalHistory.count { h ->
        (h.chosen == "A" && h.percentageA >= 50) || (h.chosen == "B" && h.percentageB >= 50)
    }
    val score = ((withCrowd.toFloat() / finalHistory.size) * 100).roundToInt()
    
    val (emoji, label) = when {
        score == 100 -> "👑" to "Crowd Pleaser"
        score >= 60 -> "🔥" to "Trendsetter"
        score >= 40 -> "⚡" to "Wild Card"
        else -> "🦄" to "One of a Kind"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0F0E17)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = emoji,
                    fontSize = 52.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "GAME OVER",
                    color = Color(0xFFFFE66D),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 32.sp
                )
                Text(
                    text = buildAnnotatedString {
                        append("Agreed with crowd on ")
                        withStyle(style = SpanStyle(color = Color(0xFFFF6B35), fontWeight = FontWeight.Bold)) {
                            append("$withCrowd")
                        }
                        append(" of ")
                        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                            append("${finalHistory.size}")
                        }
                        append(" questions")
                    },
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                // Circular Progress
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        val strokeWidth = 6.dp.toPx()
                        val innerRadius = (size.minDimension - strokeWidth) / 2
                        
                        // Background Circle
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = innerRadius,
                            style = Stroke(width = strokeWidth)
                        )
                        
                        // Progress Arc
                        drawArc(
                            color = Color(0xFFFF6B35),
                            startAngle = -90f,
                            sweepAngle = 360f * (score / 100f),
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$score%",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = "CROWD",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Answers List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "YOUR ANSWERS",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(finalHistory) { index, entry ->
                        AnswerCard(index + 1, entry)
                    }
                }
            }

            // Bottom Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.02f))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onReplay,
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
                            text = "🔄 Play Again",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Button(
                    onClick = onHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.06f)),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = "Back to Home",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AnswerCard(index: Int, entry: HistoryEntry) {
    val chosenColor = try {
        Color((if (entry.chosen == "A") entry.question.colorA else entry.question.colorB).toColorInt())
    } catch (e: Exception) {
        if (entry.chosen == "A") Color(0xFFFF6B35) else Color(0xFF3A86FF)
    }
    
    val chosenText = if (entry.chosen == "A") entry.question.optionA else entry.question.optionB
    val withMajority = (entry.chosen == "A" && entry.percentageA >= 50) || (entry.chosen == "B" && entry.percentageB >= 50)
    val pct = if (entry.chosen == "A") entry.percentageA else entry.percentageB

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(chosenColor.copy(alpha = 0.13f))
                    .border(1.5.dp, chosenColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (withMajority) "🔥" else "🦄",
                    fontSize = 14.sp
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Q$index — Option ${entry.chosen}",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 10.sp
                )
                Text(
                    text = chosenText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Text(
                text = "$pct%",
                color = chosenColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

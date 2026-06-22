package com.example.wouldyourather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wouldyourather.ui.theme.WouldYouRatherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WouldYouRatherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        GameBackground()
                    }
                }
            }
        }
    }
}

@Composable
fun GameBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0E17))
    ) {

        // Orange glow
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 220.dp, y = (-80).dp)
                .background(
                    Color(0xFFFF6B35).copy(alpha = 0.1f),
                    CircleShape
                )
        )

        // Teal glow
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-80).dp, y = 650.dp)
                .background(
                    Color(0xFF4ECDC4).copy(alpha = 0.1f),
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
                    RoundedCornerShape(4.dp)
                )
        )

        //Would you rather text
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Would You",
                color = Color.White,
                fontSize = 40.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 200.dp)

            )
            Text(
                text = "Rather?",
                color = Color(0xFFFF6B35), // Using a vibrant orange
                fontSize = 40.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 9.dp)
            )
        }

        //Tagline
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 300.dp),
            horizontalAlignment = Alignment.CenterHorizontally

        )
        {
           Text(
               text = "Impossible choices. No Wrong",
               color = Color.Gray,
               fontSize = 20.sp,
               fontWeight = FontWeight.Bold,
               modifier = Modifier
                   .padding(top = 112.dp)
           )
            Text(
                text = "Answers. Pure chaos.",
                color = Color.Gray,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 14.dp)
            )
        }


        //Play Now Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 660.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .size(width = 150.dp, height = 50.dp)

            )
            {
                Text("Play Now")
            }
            
        }

        //How to play Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 730.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .size(width = 150.dp, height = 50.dp)

            ) {
                Text("How to Play")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameBackgroundPreview() {
    WouldYouRatherTheme {
        GameBackground()
    }
}

// FILE: app/src/main/java/com/sandeep/ganitabigyan/WelcomeScreen.kt
package com.sandeep.ganitabigyan

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sandeep.ganitabigyan.ui.theme.OdiaFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(onStartClick: () -> Unit) {

    val scope = rememberCoroutineScope()
    // This state will track whether the button has been clicked and is in "loading" mode.
    var isLoading by remember { mutableStateOf(false) }

    fun proceed() {
        // Prevent clicking again while loading
        if (isLoading) return

        scope.launch {
            isLoading = true // Trigger the loading animation
            delay(1500L) // Wait for 1.5 seconds to show the animation
            onStartClick() // Navigate to the next screen
        }
    }

    // The main welcome text, now on a single line
    val welcomeText = "ସ୍କ୍ରୋଲ୍ କରନ୍ତୁ, କିନ୍ତୁ ବୁଦ୍ଧି ଲଗେଇ"

    // Animate the button's width. It will be wide initially and shrink to a square when loading.
    val buttonWidth by animateDpAsState(
        targetValue = if (isLoading) 60.dp else 220.dp,
        label = "button width"
    )

    // Animate the button's corner radius. It will go from rounded corners to a full circle.
    val cornerRadius by animateDpAsState(
        targetValue = if (isLoading) 30.dp else 16.dp, // 30dp is half of the 60dp height, making it a circle
        label = "button corner radius"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_welcome_mascot),
            contentDescription = "Welcome Mascot",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-120).dp)
                .fillMaxWidth(0.7f),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp, start = 32.dp, end = 32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = welcomeText,
                fontFamily = OdiaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp,
                color = Color.Black.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(50.dp))

            // The animated button
            Button(
                onClick = { proceed() },
                modifier = Modifier
                    .height(60.dp)
                    .width(buttonWidth), // Use the animated width
                shape = RoundedCornerShape(cornerRadius), // Use the animated corner radius
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8A54D4)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                // AnimatedContent smoothly transitions between the text and the loading indicator
                AnimatedContent(
                    targetState = isLoading,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "button content animation"
                ) { loadingState ->
                    if (loadingState) {
                        // --- Loading State ---
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        // --- Initial State ---
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ଆଗକୁ ବଢନ୍ତୁ",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                fontFamily = OdiaFontFamily
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null, // Content description is on the button itself
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
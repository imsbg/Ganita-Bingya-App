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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val dataStore = remember { SettingsDataStore(context) }
    var isLoading by remember { mutableStateOf(false) }

    // This is your original, correct function that saves the state
    fun proceed() {
        if (isLoading) return

        scope.launch {
            isLoading = true
            dataStore.setWelcomeCompleted() // Save state so it doesn't show again
            delay(1500L)                 // Wait for animation to finish
            onStartClick()               // Navigate to the next screen
        }
    }

    val welcomeText = "ସ୍କ୍ରୋଲ୍ କରନ୍ତୁ, କିନ୍ତୁ ବୁଦ୍ଧି ଲଗେଇ"

    val buttonWidth by animateDpAsState(
        targetValue = if (isLoading) 60.dp else 220.dp,
        label = "button width"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isLoading) 30.dp else 16.dp,
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
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp,
                color = Color.Black.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = { proceed() },
                modifier = Modifier
                    .height(60.dp)
                    .width(buttonWidth),
                shape = RoundedCornerShape(cornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8A54D4)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                AnimatedContent(
                    targetState = isLoading,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "button content animation"
                ) { loadingState ->
                    if (loadingState) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
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
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
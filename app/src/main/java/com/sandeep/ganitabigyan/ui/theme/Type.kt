package com.sandeep.ganitabigyan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sandeep.ganitabigyan.R

val OdiaFontFamily = FontFamily(
    Font(R.font.anekodia_semiexpanded_bold, FontWeight.Normal),
    Font(R.font.anekodia_semiexpanded_bold, FontWeight.Bold)
)

// FIX: We are now defining our own larger font sizes.
val Typography = Typography(
    // For Splash Screen: "ଜୟ ଜଗନ୍ନାଥ", "ସ୍ବାଗତ"
    displayLarge = TextStyle(
        fontFamily = OdiaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp // Large size for splash
    ),
    displayMedium = TextStyle(
        fontFamily = OdiaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp // Medium-large size for splash
    ),
    // For Question Text: "୧୨ + ୩ = ?"
    displaySmall = TextStyle(
        fontFamily = OdiaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp
    ),
    // For Solution Dialog: "୧୨ + ୩ = ୧୫"
    headlineLarge = TextStyle(
        fontFamily = OdiaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp // Large size for dialogs
    ),
    headlineMedium = TextStyle(
        fontFamily = OdiaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = OdiaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp
    ),
    // For other standard text
    titleLarge = TextStyle(
        fontFamily = OdiaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = OdiaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    ),
    // All other styles will use the custom font by default
    titleMedium = TextStyle(fontFamily = OdiaFontFamily, fontWeight = FontWeight.Bold),
    titleSmall = TextStyle(fontFamily = OdiaFontFamily, fontWeight = FontWeight.Bold),
    bodyMedium = TextStyle(fontFamily = OdiaFontFamily, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontFamily = OdiaFontFamily, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontFamily = OdiaFontFamily, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontFamily = OdiaFontFamily, fontWeight = FontWeight.Bold),
    labelSmall = TextStyle(fontFamily = OdiaFontFamily, fontWeight = FontWeight.Bold)
)
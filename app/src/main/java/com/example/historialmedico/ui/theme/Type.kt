package com.example.historialmedico.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.historialmedico.R

val interFontFamily = FontFamily(
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private val base = Typography()

//los 15 estilos usan Inter: botones, pestañas y dialogos toman los que antes faltaban
val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = interFontFamily),
    displayMedium = base.displayMedium.copy(fontFamily = interFontFamily),
    displaySmall = base.displaySmall.copy(fontFamily = interFontFamily),
    headlineLarge = base.headlineLarge.copy(fontFamily = interFontFamily),
    headlineMedium = base.headlineMedium.copy(fontFamily = interFontFamily),
    headlineSmall = base.headlineSmall.copy(fontFamily = interFontFamily, fontWeight = FontWeight.Medium),
    titleLarge = base.titleLarge.copy(fontFamily = interFontFamily, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = interFontFamily, fontWeight = FontWeight.Medium),
    titleSmall = base.titleSmall.copy(fontFamily = interFontFamily, fontWeight = FontWeight.Medium),
    bodyLarge = base.bodyLarge.copy(fontFamily = interFontFamily, letterSpacing = 0.1.sp),
    bodyMedium = base.bodyMedium.copy(fontFamily = interFontFamily, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp),
    bodySmall = base.bodySmall.copy(fontFamily = interFontFamily, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp),
    labelLarge = base.labelLarge.copy(fontFamily = interFontFamily, fontWeight = FontWeight.Medium),
    labelMedium = base.labelMedium.copy(fontFamily = interFontFamily),
    labelSmall = base.labelSmall.copy(fontFamily = interFontFamily)
)
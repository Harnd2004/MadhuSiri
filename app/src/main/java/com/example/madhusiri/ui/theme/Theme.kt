package com.example.madhusiri.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── All Brand Colors ─────────────────────────────────────────────────────────
val HoneyGold       = Color(0xFFE8920A)
val HoneyLight      = Color(0xFFFFC85C)
val HoneyDark       = Color(0xFFB56A00)
val HoneyCream      = Color(0xFFFFF8EC)
val HoneyCreamDark  = Color(0xFFFFF0D0)
val ForestGreen     = Color(0xFF2E7D32)
val ForestMid       = Color(0xFF43A047)
val ForestLight     = Color(0xFFE8F5E9)
val BrownInk        = Color(0xFF3E2723)
val BrownMid        = Color(0xFF795548)
val BrownLight      = Color(0xFFEFEBE9)
val AlertRed        = Color(0xFFD32F2F)
val AlertLight      = Color(0xFFFFEBEE)
val SafeGreen       = Color(0xFF388E3C)
val WarnOrange      = Color(0xFFF57C00)
val SkyBlue         = Color(0xFF0288D1)

// ─── Material3 Color Scheme ───────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary            = HoneyGold,
    onPrimary          = Color.White,
    primaryContainer   = HoneyCream,
    onPrimaryContainer = HoneyDark,
    secondary          = ForestGreen,
    onSecondary        = Color.White,
    secondaryContainer = ForestLight,
    background         = Color(0xFFFFFBF2),
    surface            = Color.White,
    surfaceVariant     = HoneyCream,
    onBackground       = BrownInk,
    onSurface          = BrownInk,
    onSurfaceVariant   = BrownMid,
    error              = AlertRed,
    outline            = Color(0xFFD4B896)
)

// ─── Typography ───────────────────────────────────────────────────────────────
val MadhuTypography = Typography(
    displayLarge   = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 32.sp, letterSpacing = (-0.5).sp),
    headlineLarge  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 26.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 16.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.5.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 11.sp, letterSpacing = 0.5.sp)
)

// ─── Theme Entry Point ────────────────────────────────────────────────────────
@Composable
fun MadhuSiriTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = MadhuTypography,
        content     = content
    )
}
package ir.softwaretrain.soghatyar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// پالت برند سوغات‌یار
val Zereshki = Color(0xFF8E1B2B)      // زرشکی (رنگ اصلی)
val ZereshkiDark = Color(0xFF6A1220)
val Saffron = Color(0xFFF6C85F)       // زعفرانی
val Cream = Color(0xFFFBF7F1)         // کرم (پس‌زمینه)
val Ink = Color(0xFF2A1E1A)           // قهوه‌ای تیره (متن)
val Sand = Color(0xFFE6DCCF)          // حاشیه‌ها
val Olive = Color(0xFF3D5A33)         // سبز (برای وضعیت پرداخت‌شده)
val Clay = Color(0xFFB5651D)

private val LightColors = lightColorScheme(
    primary = Zereshki,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6E3E6),
    onPrimaryContainer = ZereshkiDark,
    secondary = Clay,
    onSecondary = Color.White,
    tertiary = Olive,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEFE7DC),
    onSurfaceVariant = Color(0xFF6B5A52),
    outline = Sand,
    error = Color(0xFFC0392B)
)

private val DarkColors = darkColorScheme(
    primary = Saffron,
    onPrimary = Ink,
    background = Color(0xFF1A1412),
    onBackground = Cream,
    surface = Color(0xFF241C18),
    onSurface = Cream,
    surfaceVariant = Color(0xFF2E2420),
    onSurfaceVariant = Color(0xFFCBBBB0),
    outline = Color(0xFF4A3D36),
    error = Color(0xFFE57368)
)

@Composable
fun SoghatyarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}

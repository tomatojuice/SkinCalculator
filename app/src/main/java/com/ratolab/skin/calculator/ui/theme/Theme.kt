package com.ratolab.skin.calculator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 既存のテーマ色
val Indigo500 = Color(0xFF3F51B5); val Indigo900 = Color(0xFF1A237E)
val Pink500   = Color(0xFFE91E63); val Pink900   = Color(0xFF880E4F)
val Teal500   = Color(0xFF009688); val Teal900   = Color(0xFF004D40)
val Orange500 = Color(0xFFFF9800); val Orange900 = Color(0xFFE65100)
val Brown500  = Color(0xFF795548); val Brown900  = Color(0xFF3E2723)
val Green500  = Color(0xFF4CAF50); val Green900  = Color(0xFF1B5E20)
val Grey500   = Color(0xFF607D8B); val Grey900   = Color(0xFF212121)
val MikuBase  = Color(0xFF39C5BB); val MikuDark  = Color(0xFF137A7F)

// パステルテーマ7種の色定義
val MacaronTop = Color(0xFFFF9CB3); val MacaronBottom = Color(0xFF80C4FF)
val CottonTop  = Color(0xFFE0BBE4); val CottonBottom  = Color(0xFFFFDFD3)
val UnicornTop = Color(0xFFA8E6CF); val UnicornBottom = Color(0xFFDCD3FF)
val SherbetTop = Color(0xFFFFF6B2); val SherbetBottom = Color(0xFFFFD8A8)
// ★ 追加の3種
val PeachTop   = Color(0xFFFFDAB9); val PeachBottom   = Color(0xFFFFE4E1)
val PistachioTop= Color(0xFFE2F0CB); val PistachioBottom= Color(0xFFB5EAD7)
val LavenderTop = Color(0xFFE6E6FA); val LavenderBottom = Color(0xFFD8BFD8)

// ★ C・ACボタン用の色（通常用とパステル用）
val NormalAccentTop = Pink500; val NormalAccentBottom = Pink900
val PastelAccentTop = Color(0xFFFFA0A0); val PastelAccentBottom = Color(0xFFFF8A8A) // ふんわりコーラル

enum class AppThemeType {
    MACARON, COTTON_CANDY, UNICORN, SHERBET, PEACH_MILK, PISTACHIO, LAVENDER,
    INDIGO, PINK, TEAL, ORANGE, BROWN, GREEN, GREY, MIKU
}

@Composable
fun TomaCalculatorTheme(
    themeType: AppThemeType = AppThemeType.INDIGO,
    content: @Composable () -> Unit
) {
    val (primaryColor, darkColor) = when (themeType) {
        AppThemeType.INDIGO       -> Indigo500 to Indigo900
        AppThemeType.PINK         -> Pink500 to Pink900
        AppThemeType.TEAL         -> Teal500 to Teal900
        AppThemeType.ORANGE       -> Orange500 to Orange900
        AppThemeType.BROWN        -> Brown500 to Brown900
        AppThemeType.GREEN        -> Green500 to Green900
        AppThemeType.GREY         -> Grey500 to Grey900
        AppThemeType.MIKU         -> MikuBase to MikuDark
        AppThemeType.MACARON      -> MacaronTop to MacaronBottom
        AppThemeType.COTTON_CANDY -> CottonTop to CottonBottom
        AppThemeType.UNICORN      -> UnicornTop to UnicornBottom
        AppThemeType.SHERBET      -> SherbetTop to SherbetBottom
        AppThemeType.PEACH_MILK   -> PeachTop to PeachBottom
        AppThemeType.PISTACHIO    -> PistachioTop to PistachioBottom
        AppThemeType.LAVENDER     -> LavenderTop to LavenderBottom
    }

    // パステルテーマかどうかを判定して、C・ACボタンの色を切り替え
    val isPastel = themeType in listOf(
        AppThemeType.MACARON, AppThemeType.COTTON_CANDY, AppThemeType.UNICORN,
        AppThemeType.SHERBET, AppThemeType.PEACH_MILK, AppThemeType.PISTACHIO, AppThemeType.LAVENDER
    )
    val accentTop = if (isPastel) PastelAccentTop else NormalAccentTop
    val accentBottom = if (isPastel) PastelAccentBottom else NormalAccentBottom

    val colorScheme = lightColorScheme(
        primary = primaryColor,
        primaryContainer = darkColor,
        tertiary = accentTop,          // ★ アクセント色（上）を登録
        tertiaryContainer = accentBottom, // ★ アクセント色（下）を登録
        onPrimary = Color.White,
        surface = Color(0xFFF5F5F5),
        onSurface = Color.Black
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
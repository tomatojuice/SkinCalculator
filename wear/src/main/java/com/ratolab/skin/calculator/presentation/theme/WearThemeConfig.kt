package com.ratolab.skin.calculator.presentation.theme

import androidx.compose.ui.graphics.Color

// ★ Wear OS版の全テーマ (TETO, YUKARIをしっかり追加！)
enum class AppThemeType {
    INDIGO, PINK, TEAL, ORANGE, BROWN, GREEN, GREY, MIKU,
    MACARON, COTTON_CANDY, UNICORN, SHERBET, PEACH_MILK, PISTACHIO, LAVENDER, MARBLE,
    GUMI, LUKA, LIN, REN,
    TETO, YUKARI // ← ここが抜けていました！
}

// ★ Wear OS用の色の詰め合わせ
data class WearThemeColors(
    val bg: Color,
    val btnTop: Color,
    val btnBottom: Color,
    val accentTop: Color,
    val accentBottom: Color
)

// ★ テーマを渡すと色を返す関数
fun getWearThemeColors(type: AppThemeType): WearThemeColors {
    val defAccTop = Color(0xFFFF8A80)
    val defAccBot = Color(0xFFFF5252)

    return when (type) {
        // --- 3個指定 ---
        AppThemeType.INDIGO -> WearThemeColors(Color(0xFFE8EAF6), Color(0xFFE1F5FE), Color(0xFF9FA8DA), defAccTop, defAccBot)
        AppThemeType.PINK -> WearThemeColors(Color(0xFFFFF0F5), Color(0xFFFFF0F5), Color(0xFFF48FB1), defAccTop, defAccBot)
        AppThemeType.TEAL -> WearThemeColors(Color(0xFFE0F2F1), Color(0xFFE0F7FA), Color(0xFF80CBC4), defAccTop, defAccBot)
        AppThemeType.ORANGE -> WearThemeColors(Color(0xFFFFF3E0), Color(0xFFFFF9C4), Color(0xFFFFCC80), defAccTop, defAccBot)
        AppThemeType.BROWN -> WearThemeColors(Color(0xFFEFEBE9), Color(0xFFFFF3E0), Color(0xFFBCAAA4), defAccTop, defAccBot)
        AppThemeType.GREEN -> WearThemeColors(Color(0xFFE8F5E9), Color(0xFFF1F8E9), Color(0xFFA5D6A7), defAccTop, defAccBot)
        AppThemeType.GREY -> WearThemeColors(Color(0xFFFAFAFA), Color(0xFFFFFFFF), Color(0xFFCFD8DC), defAccTop, defAccBot)
        AppThemeType.MACARON -> WearThemeColors(Color(0xFFFFF0F5), Color(0xFFFCE4EC), Color(0xFFF8BBD0), defAccTop, defAccBot)
        AppThemeType.COTTON_CANDY -> WearThemeColors(Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFB3E5FC), defAccTop, defAccBot)
        AppThemeType.UNICORN -> WearThemeColors(Color(0xFFEDE7F6), Color(0xFFD1C4E9), Color(0xFFF8BBD0), defAccTop, defAccBot)
        AppThemeType.SHERBET -> WearThemeColors(Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFFFCC80), defAccTop, defAccBot)
        AppThemeType.PEACH_MILK -> WearThemeColors(Color(0xFFFBE9E7), Color(0xFFFFE0B2), Color(0xFFFFAB91), defAccTop, defAccBot)
        AppThemeType.PISTACHIO -> WearThemeColors(Color(0xFFF1F8E9), Color(0xFFF0F4C3), Color(0xFFC5E1A5), defAccTop, defAccBot)
        AppThemeType.LAVENDER -> WearThemeColors(Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFCE93D8), defAccTop, defAccBot)

        AppThemeType.MIKU -> WearThemeColors(Color(0xFFE0F7FA), Color(0xFF80DEEA), Color(0xFF1DE9B6), Color(0xFFFF80AB), Color(0xFFF50057))
        AppThemeType.MARBLE -> WearThemeColors(Color(0xFFFAFAFA), Color(0xFFFFFFFF), Color(0xFFE0E0E0), Color(0xFFBCAAA4), Color(0xFF8D6E63))
        AppThemeType.GUMI -> WearThemeColors(Color(0xFFF1F8E9), Color(0xFFB2FF59), Color(0xFFFFAB40), Color(0xFFFF4081), Color(0xFFF50057))
        AppThemeType.LUKA -> WearThemeColors(Color(0xFFFCE4EC), Color(0xFFF48FB1), Color(0xFFFFD54F), Color(0xFFF06292), Color(0xFFC2185B))
        AppThemeType.LIN -> WearThemeColors(Color(0xFFFFFDE7), Color(0xFFFFF176), Color(0xFFFFB74D), Color(0xFFFFCA28), Color(0xFFFF8F00))
        AppThemeType.REN -> WearThemeColors(Color(0xFFFFFDE7), Color(0xFFFFF59D), Color(0xFFFFCA28), Color(0xFF757575), Color(0xFF424242))
        AppThemeType.TETO -> WearThemeColors(Color(0xFFFFF0F5), Color(0xFFFFCDD2), Color(0xFFE57373), Color(0xFF90A4AE), Color(0xFF607D8B))
        AppThemeType.YUKARI -> WearThemeColors(Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFCE93D8), Color(0xFFF48FB1), Color(0xFFD81B60))
    }
}
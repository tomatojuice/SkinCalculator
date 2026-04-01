package com.ratolab.skin.calculator.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppThemeType {
    INDIGO, PINK, TEAL, ORANGE, BROWN, GREEN, GREY, MIKU,
    MACARON, COTTON_CANDY, UNICORN, SHERBET, PEACH_MILK, PISTACHIO, LAVENDER, MARBLE,
    GUMI, LUKA, LIN, REN, TETO, YUKARI
}
data class AppThemeConfig(
    val displayBg: Color,      // ディスプレイ（上半分）の背景色
    val keypadBg: Color,       // キーパッド（下半分）の背景色
    val btnTop: Color,         // 通常ボタンの上グラデーション
    val btnBottom: Color,      // 通常ボタンの下グラデーション
    val accentTop: Color,      // C・ACボタン（アクセント）の上グラデーション
    val accentBottom: Color    // C・ACボタン（アクセント）の下グラデーション
)

// テーマを渡すと、色のセットを返してくれる関数
fun getAppThemeConfig(theme: AppThemeType): AppThemeConfig {
    val defAccTop = Color(0xFFFF8A80)
    val defAccBot = Color(0xFFFF5252)

    return when (theme) {
        // --- スタンダード ---
        AppThemeType.INDIGO -> AppThemeConfig(Color(0xFFE8EAF6), Color(0xFFC5CAE9), Color(0xFFE1F5FE), Color(0xFF9FA8DA), defAccTop, defAccBot)
        AppThemeType.PINK -> AppThemeConfig(Color(0xFFFFF0F5), Color(0xFFFCE4EC), Color(0xFFFFF0F5), Color(0xFFF48FB1), defAccTop, defAccBot)
        AppThemeType.TEAL -> AppThemeConfig(Color(0xFFE0F2F1), Color(0xFFB2DFDB), Color(0xFFE0F7FA), Color(0xFF80CBC4), defAccTop, defAccBot)
        AppThemeType.ORANGE -> AppThemeConfig(Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFFFF9C4), Color(0xFFFFCC80), defAccTop, defAccBot)
        AppThemeType.BROWN -> AppThemeConfig(Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFFFFF3E0), Color(0xFFBCAAA4), defAccTop, defAccBot)
        AppThemeType.GREEN -> AppThemeConfig(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFF1F8E9), Color(0xFFA5D6A7), defAccTop, defAccBot)
        AppThemeType.GREY -> AppThemeConfig(Color(0xFFFAFAFA), Color(0xFFEEEEEE), Color(0xFFFFFFFF), Color(0xFFCFD8DC), defAccTop, defAccBot)

        // --- パステル ---
        AppThemeType.MACARON -> AppThemeConfig(Color(0xFFFFF0F5), Color(0xFFFCE4EC), Color(0xFFFEF0F4), Color(0xFFF8BBD0), defAccTop, defAccBot)
        AppThemeType.COTTON_CANDY -> AppThemeConfig(Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFF0E0F4), Color(0xFFB3E5FC), defAccTop, defAccBot)
        AppThemeType.UNICORN -> AppThemeConfig(Color(0xFFEDE7F6), Color(0xFFD1C4E9), Color(0xFFE8E1F4), Color(0xFFF8BBD0), defAccTop, defAccBot)
        AppThemeType.SHERBET -> AppThemeConfig(Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFFFFCDE), Color(0xFFFFCC80), defAccTop, defAccBot)
        AppThemeType.PEACH_MILK -> AppThemeConfig(Color(0xFFFBE9E7), Color(0xFFFFE0B2), Color(0xFFFFF1DB), Color(0xFFFFAB91), defAccTop, defAccBot)
        AppThemeType.PISTACHIO -> AppThemeConfig(Color(0xFFF1F8E9), Color(0xFFF0F4C3), Color(0xFFF8FAE6), Color(0xFFC5E1A5), defAccTop, defAccBot)
        AppThemeType.LAVENDER -> AppThemeConfig(Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFF0E0F4), Color(0xFFCE93D8), defAccTop, defAccBot)
        AppThemeType.MARBLE -> AppThemeConfig(Color(0xFFFFFFFF), Color(0xFFF5F5F5), Color(0xFFFFFFFF), Color(0xFFE0E0E0), Color(0xFFBCAAA4), Color(0xFF8D6E63))

        // --- ボカロスペシャル ---
        AppThemeType.MIKU -> AppThemeConfig(Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFF80DEEA), Color(0xFF1DE9B6), Color(0xFFFF80AB), Color(0xFFF50057))
        AppThemeType.GUMI -> AppThemeConfig(Color(0xFFF9FBE7), Color(0xFFF1F8E9), Color(0xFFB2FF59), Color(0xFFFFAB40), Color(0xFFFF4081), Color(0xFFF50057))
        AppThemeType.LUKA -> AppThemeConfig(Color(0xFFFCE4EC), Color(0xFFF8BBD0), Color(0xFFF48FB1), Color(0xFFFFD54F), Color(0xFFF06292), Color(0xFFC2185B))
        AppThemeType.LIN -> AppThemeConfig(Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFFFF176), Color(0xFFFFB74D), Color(0xFFFFCA28), Color(0xFFFF8F00))
        AppThemeType.REN -> AppThemeConfig(Color(0xFFFFFDE7), Color(0xFFE1F5FE), Color(0xFFFFF59D), Color(0xFFFFCA28), Color(0xFF757575), Color(0xFF424242))
        AppThemeType.TETO -> AppThemeConfig(Color(0xFFFFF0F5), Color(0xFFFFEBEE), Color(0xFFFFCDD2), Color(0xFFE57373), Color(0xFF90A4AE), Color(0xFF607D8B))
        AppThemeType.YUKARI -> AppThemeConfig(Color(0xFFFAFAFA), Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFCE93D8), Color(0xFFF48FB1), Color(0xFFD81B60))
    }
}
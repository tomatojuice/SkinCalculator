package com.ratolab.skin.calculator

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch
import com.ratolab.skin.calculator.ui.theme.AppThemeType
import com.ratolab.skin.calculator.ui.theme.TomaCalculatorTheme
import java.util.Locale
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(this) {}

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val locale = Locale(uiState.languageCode)
            Locale.setDefault(locale)

            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }
            val context = LocalContext.current
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            val newContext = context.createConfigurationContext(configuration)

            CompositionLocalProvider(
                LocalConfiguration provides configuration,
                LocalContext provides newContext
            ) {
                key(uiState.languageCode) {
                    val navController = rememberNavController()

                    TomaCalculatorTheme(themeType = uiState.currentTheme) {
                        Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                            NavHost(navController = navController, startDestination = "calculator") {
                                composable("calculator") {
                                    CalculatorScreen(
                                        uiState = uiState,
                                        onLanguageChange = { viewModel.changeLanguage(it) },
                                        onNavigateToHelp = { navController.navigate("help") },
                                        onThemeChange = { viewModel.changeTheme(it) },
                                        onShapeToggle = { viewModel.toggleShape(it) },
                                        onVibToggle = { viewModel.toggleVibration(it) },
                                        onSaveTaxRate = { viewModel.saveTaxRate(it) },
                                        onClearHistory = { viewModel.clearHistory() },
                                        onNumberClick = { viewModel.onInputNumber(it) },
                                        onOperatorClick = { viewModel.onOperatorClick(it) },
                                        onEqualClick = { viewModel.onEqualClick() },
                                        onClearClick = { viewModel.onClearC() },
                                        onClearAllClick = { viewModel.onClearAC() },
                                        onBackspaceClick = { viewModel.onBackspace() },
                                        onMemoryPlusClick = { viewModel.onMemoryPlus() },
                                        onMemoryMinusClick = { viewModel.onMemoryMinus() },
                                        onMemoryClearClick = { viewModel.onMemoryClear() },
                                        onMemoryRecallClick = { viewModel.onMemoryRecall() },
                                        onToggleSignClick = { viewModel.onToggleSign() },
                                        onSquareRootClick = { viewModel.onSquareRoot() },
                                        onPercentClick = { viewModel.onPercent() },
                                        onTaxPlusClick = { viewModel.onTaxPlus() },
                                        onTaxMinusClick = { viewModel.onTaxMinus() }
                                    )
                                }
                                composable("help") {
                                    HelpScreen(
                                        currentTheme = uiState.currentTheme,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    uiState: CalculatorUiState,
    onLanguageChange: (String) -> Unit,
    onNavigateToHelp: () -> Unit,
    onThemeChange: (AppThemeType) -> Unit,
    onShapeToggle: (Boolean) -> Unit,
    onVibToggle: (Boolean) -> Unit,
    onSaveTaxRate: (String) -> Unit,
    onClearHistory: () -> Unit,
    onNumberClick: (String) -> Unit,
    onOperatorClick: (Int) -> Unit,
    onEqualClick: () -> Unit,
    onClearClick: () -> Unit,
    onClearAllClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onMemoryPlusClick: () -> Unit,
    onMemoryMinusClick: () -> Unit,
    onMemoryClearClick: () -> Unit,
    onMemoryRecallClick: () -> Unit,
    onToggleSignClick: () -> Unit,
    onSquareRootClick: () -> Unit,
    onPercentClick: () -> Unit,
    onTaxPlusClick: () -> Unit,
    onTaxMinusClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var showMenu by remember { mutableStateOf(false) }
    var showTaxDialog by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var currentTaxRate by remember(uiState.taxRate) { mutableStateOf(uiState.taxRate) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF0F5))) {
                DisplayArea(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    uiState = uiState,
                    showMenu = showMenu,
                    isRoundShape = uiState.isRoundShape,
                    onMenuToggle = { showMenu = it },
                    onShowHistory = { showHistorySheet = true },
                    onNavigateToHelp = onNavigateToHelp,
                    onLanguageChange = onLanguageChange,
                    onThemeChange = onThemeChange,
                    onShowTaxDialog = { showTaxDialog = true },
                    onShapeToggle = onShapeToggle,
                    onVibToggle = onVibToggle
                )
                KeypadArea(
                    modifier = Modifier.weight(1.5f).fillMaxHeight(),
                    currentTheme = uiState.currentTheme,
                    isRoundShape = uiState.isRoundShape,
                    vibrationEnabled = uiState.vibrationEnabled,
                    onNumberClick = onNumberClick,
                    onOperatorClick = onOperatorClick,
                    onEqualClick = onEqualClick,
                    onClearClick = onClearClick,
                    onClearAllClick = onClearAllClick,
                    onBackspaceClick = onBackspaceClick,
                    onMemoryPlusClick = onMemoryPlusClick,
                    onMemoryMinusClick = onMemoryMinusClick,
                    onMemoryClearClick = onMemoryClearClick,
                    onMemoryRecallClick = onMemoryRecallClick,
                    onToggleSignClick = onToggleSignClick,
                    onSquareRootClick = onSquareRootClick,
                    onPercentClick = onPercentClick,
                    onTaxPlusClick = onTaxPlusClick,
                    onTaxMinusClick = onTaxMinusClick
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF0F5))) {
                DisplayArea(
                    modifier = Modifier.weight(1.0f).fillMaxWidth(),
                    uiState = uiState,
                    showMenu = showMenu,
                    isRoundShape = uiState.isRoundShape,
                    onMenuToggle = { showMenu = it },
                    onShowHistory = { showHistorySheet = true },
                    onNavigateToHelp = onNavigateToHelp,
                    onLanguageChange = onLanguageChange,
                    onThemeChange = onThemeChange,
                    onShowTaxDialog = { showTaxDialog = true },
                    onShapeToggle = onShapeToggle,
                    onVibToggle = onVibToggle
                )
                KeypadArea(
                    modifier = Modifier.weight(1.3f).fillMaxWidth(),
                    currentTheme = uiState.currentTheme,
                    isRoundShape = uiState.isRoundShape,
                    vibrationEnabled = uiState.vibrationEnabled,
                    onNumberClick = onNumberClick,
                    onOperatorClick = onOperatorClick,
                    onEqualClick = onEqualClick,
                    onClearClick = onClearClick,
                    onClearAllClick = onClearAllClick,
                    onBackspaceClick = onBackspaceClick,
                    onMemoryPlusClick = onMemoryPlusClick,
                    onMemoryMinusClick = onMemoryMinusClick,
                    onMemoryClearClick = onMemoryClearClick,
                    onMemoryRecallClick = onMemoryRecallClick,
                    onToggleSignClick = onToggleSignClick,
                    onSquareRootClick = onSquareRootClick,
                    onPercentClick = onPercentClick,
                    onTaxPlusClick = onTaxPlusClick,
                    onTaxMinusClick = onTaxMinusClick
                )
            }
        }

        // ★ キラキラ背景はすべてのテーマで適用する
        TwinkleBackground(modifier = Modifier.fillMaxSize())
    }

    if (showTaxDialog) {
        AlertDialog(
            onDismissRequest = { showTaxDialog = false },
            title = { Text(stringResource(R.string.dialog_tax_title), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            text = {
                OutlinedTextField(
                    value = currentTaxRate,
                    onValueChange = { currentTaxRate = it },
                    label = { Text(stringResource(R.string.dialog_tax_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onSaveTaxRate(currentTaxRate); showTaxDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) { Text(stringResource(R.string.dialog_save)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTaxDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) { Text(stringResource(R.string.dialog_cancel)) }
            }
        )
    }

    if (showHistorySheet) {
        ModalBottomSheet(onDismissRequest = { showHistorySheet = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().fillMaxHeight(0.6f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.history_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { onClearHistory() }) { Text(stringResource(R.string.history_clear), color = Color.Red) }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                if (uiState.history.isEmpty()) {
                    Text(stringResource(R.string.history_empty), color = Color.Gray, modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn {
                        items(uiState.history.reversed()) {
                            Text(
                                text = it,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                textAlign = TextAlign.End,
                                color = Color.DarkGray, // ★履歴も黒に戻す
                                style = TextStyle(shadow = null) // 影は不要
                            )
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayArea(modifier: Modifier, uiState: CalculatorUiState, showMenu: Boolean, isRoundShape: Boolean, onMenuToggle: (Boolean) -> Unit, onShowHistory: () -> Unit, onNavigateToHelp: () -> Unit, onLanguageChange: (String) -> Unit, onThemeChange: (AppThemeType) -> Unit, onShowTaxDialog: () -> Unit, onShapeToggle: (Boolean) -> Unit, onVibToggle: (Boolean) -> Unit) {
    var menuMode by remember(showMenu) { mutableStateOf("MAIN") }

    Box(modifier = modifier.padding(start = 16.dp, top = 16.dp, end = 4.dp, bottom = 16.dp)) {
        Row(modifier = Modifier.align(Alignment.TopEnd)) {
            IconButton(onClick = onShowHistory) { Icon(Icons.Default.List, stringResource(R.string.cd_history), tint = Color.Gray) }
            Box {
                IconButton(onClick = { onMenuToggle(!showMenu) }) { Icon(Icons.Default.MoreVert, stringResource(R.string.cd_menu), tint = Color.Gray) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { onMenuToggle(false) }) {
                    if (menuMode == "MAIN") {
                        DropdownMenuItem(text = { Text(stringResource(R.string.menu_skin)) }, onClick = { menuMode = "SKIN" })
                        DropdownMenuItem(text = { Text(stringResource(R.string.menu_language)) }, onClick = { menuMode = "LANG" })
                        DropdownMenuItem(text = { Text(if (isRoundShape) stringResource(R.string.menu_shape_square) else stringResource(R.string.menu_shape_round)) }, onClick = { onShapeToggle(!isRoundShape); onMenuToggle(false) })
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.menu_vibration))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(checked = uiState.vibrationEnabled, onCheckedChange = null, modifier = Modifier.scale(0.7f))
                                }
                            },
                            onClick = { onVibToggle(!uiState.vibrationEnabled) }
                        )
                        Divider()
                        DropdownMenuItem(text = { Text(stringResource(R.string.menu_tax_setting)) }, onClick = { onShowTaxDialog(); onMenuToggle(false) })
                        DropdownMenuItem(text = { Text(stringResource(R.string.menu_help)) }, onClick = { onMenuToggle(false); onNavigateToHelp() })
                    } else if (menuMode == "SKIN") {
                        DropdownMenuItem(text = { Text(stringResource(R.string.menu_back), color = Color.Gray) }, onClick = { menuMode = "MAIN" })
                        Divider()
                        AppThemeType.values().forEach { theme ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = getThemeNameString(theme),
                                        fontWeight = if (uiState.currentTheme == theme) FontWeight.Bold else FontWeight.Normal,
                                        color = if (uiState.currentTheme == theme) MaterialTheme.colorScheme.primary else Color.Unspecified
                                    )
                                },
                                onClick = { onThemeChange(theme); onMenuToggle(false) }
                            )
                        }
                    } else if (menuMode == "LANG") {
                        DropdownMenuItem(text = { Text(stringResource(R.string.menu_back), color = Color.Gray) }, onClick = { menuMode = "MAIN" })
                        Divider()
                        listOf("ja", "en", "es", "de", "ru", "zh", "ko").forEach { lang ->
                            val labelRes = when(lang) {
                                "ja" -> R.string.lang_japanese; "en" -> R.string.lang_english
                                "es" -> R.string.lang_spanish; "de" -> R.string.lang_german
                                "ru" -> R.string.lang_russian; "zh" -> R.string.lang_chinese
                                else -> R.string.lang_korean
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(labelRes), fontWeight = if (uiState.languageCode == lang) FontWeight.Bold else FontWeight.Normal, color = if (uiState.languageCode == lang) MaterialTheme.colorScheme.primary else Color.Unspecified) },
                                onClick = { onLanguageChange(lang); onMenuToggle(false) }
                            )
                        }
                    }
                }
            }
        }
        var fontSizeMultiplier by remember(uiState.displayText) { mutableFloatStateOf(1f) }
        Text(
            text = uiState.displayText,
            fontSize = 64.sp * fontSizeMultiplier,
            color = Color.DarkGray, // ★ディスプレイの文字は黒に戻す！
            style = TextStyle(shadow = null), // ★ディスプレイには影を適用しない
            textAlign = TextAlign.End,
            maxLines = 1,
            softWrap = false,
            onTextLayout = { textLayoutResult ->
                if (textLayoutResult.hasVisualOverflow) { fontSizeMultiplier *= 0.9f }
            },
            modifier = Modifier.align(Alignment.BottomEnd).fillMaxWidth()
        )
    }
}

@Composable
fun KeypadArea(
    modifier: Modifier,
    currentTheme: AppThemeType,
    isRoundShape: Boolean,
    vibrationEnabled: Boolean,
    onNumberClick: (String) -> Unit,
    onOperatorClick: (Int) -> Unit,
    onEqualClick: () -> Unit,
    onClearClick: () -> Unit,
    onClearAllClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onMemoryPlusClick: () -> Unit,
    onMemoryMinusClick: () -> Unit,
    onMemoryClearClick: () -> Unit,
    onMemoryRecallClick: () -> Unit,
    onToggleSignClick: () -> Unit,
    onSquareRootClick: () -> Unit,
    onPercentClick: () -> Unit,
    onTaxPlusClick: () -> Unit,
    onTaxMinusClick: () -> Unit
) {
    val spacing = if (isRoundShape) 3.dp else 1.dp
    val outerPadding = if (isRoundShape) 3.dp else 0.dp

    Column(modifier = modifier.background(Color(0xFFFCE4EC)).padding(outerPadding), verticalArrangement = Arrangement.spacedBy(spacing)) {
        val rowMod = Modifier.weight(1f).fillMaxWidth()

        Row(modifier = rowMod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            CalcGridButton("TAX-", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme, onTaxMinusClick)
            CalcGridButton("TAX+", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme, onTaxPlusClick)
            CalcGridButton("▶", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme, onBackspaceClick)
            CalcGridButton("C", Modifier.weight(1f), true, isRoundShape, vibrationEnabled, currentTheme, onClearClick)
            CalcGridButton("AC", Modifier.weight(1f), true, isRoundShape, vibrationEnabled, currentTheme, onClearAllClick)
        }
        Row(modifier = rowMod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            CalcGridButton("M+", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme, onMemoryPlusClick)
            CalcGridButton("M-", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme, onMemoryMinusClick)
            CalcGridButton("CM", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme, onMemoryClearClick)
            CalcGridButton("RM", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme, onMemoryRecallClick)
            CalcGridButton("+/-", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme, onToggleSignClick)
        }
        Row(modifier = rowMod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            CalcGridButton("7", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("7") }
            CalcGridButton("8", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("8") }
            CalcGridButton("9", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("9") }
            CalcGridButton("%", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme, onPercentClick)
            CalcGridButton("√", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme, onSquareRootClick)
        }
        Row(modifier = rowMod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            CalcGridButton("4", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("4") }
            CalcGridButton("5", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("5") }
            CalcGridButton("6", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("6") }
            CalcGridButton("x", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onOperatorClick(3) } // (3 = x)
            CalcGridButton("÷", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onOperatorClick(4) } // (4 = ÷)
        }
        Row(modifier = rowMod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            CalcGridButton("1", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("1") }
            CalcGridButton("2", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("2") }
            CalcGridButton("3", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("3") }
            CalcGridButton("+", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onOperatorClick(1) } // (1 = +)
            CalcGridButton("-", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onOperatorClick(2) } // (2 = -)
        }
        Row(modifier = rowMod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            CalcGridButton("0", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("0") }
            CalcGridButton("00", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("00") }
            CalcGridButton(".", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick(".") }
            CalcGridButton("=", Modifier.weight(2f), false, isRoundShape, vibrationEnabled, currentTheme, onEqualClick)
        }
    }
}

@Composable
fun CalcGridButton(
    text: String,
    modifier: Modifier,
    isAccent: Boolean,
    isRoundShape: Boolean,
    vibrationEnabled: Boolean,
    currentTheme: AppThemeType,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // パステルテーマかどうかの判定
    val isPastel = currentTheme in listOf(
        AppThemeType.MACARON, AppThemeType.COTTON_CANDY, AppThemeType.UNICORN,
        AppThemeType.SHERBET, AppThemeType.PEACH_MILK, AppThemeType.PISTACHIO, AppThemeType.LAVENDER
    )

    // ★ ビー玉のような立体感を出すための薄いグラデーションを定義
    // MaterialThemeの色を直接使うのではなく、テーマごとに定義された色（薄い色）を使うように変更
    var topColor = MaterialTheme.colorScheme.primary
    var bottomColor = MaterialTheme.colorScheme.primaryContainer

    if (isAccent) {
        // AC, C ボタンはアクセントカラー（濃い色）を使う
        topColor = MaterialTheme.colorScheme.tertiary
        bottomColor = MaterialTheme.colorScheme.tertiaryContainer
    } else {
        // 通常ボタン
        if (isPastel) {
            // パステルテーマはMaterialThemeの色（薄い色）をそのまま使う
            topColor = MaterialTheme.colorScheme.primary
            bottomColor = MaterialTheme.colorScheme.primaryContainer
        } else {
            // ★ 非パステルテーマ（INDIGO, PINKなど）に対して、テーマの色相を保ちつつ、パステルテーマのような薄いグラデーションの色（top/bottom）を直接定義（ハードコード）
            when (currentTheme) {
                AppThemeType.INDIGO -> {
                    // 水色から深いインディゴへ（ガラスの透明感を強調）
                    topColor = Color(0xFFE1F5FE) // Light Blue 50
                    bottomColor = Color(0xFF9FA8DA) // Indigo 200
                }
                AppThemeType.PINK -> {
                    // ほんのりピーチから華やかなピンクへ
                    topColor = Color(0xFFFFF0F5) // Lavender Blush
                    bottomColor = Color(0xFFF48FB1) // Pink 200
                }
                AppThemeType.TEAL -> {
                    // ミントグリーンから深いティールへ
                    topColor = Color(0xFFE0F7FA) // Cyan 50
                    bottomColor = Color(0xFF80CBC4) // Teal 200
                }
                AppThemeType.ORANGE -> {
                    // 黄色からオレンジへ（フルーツキャンディのような発光感）
                    topColor = Color(0xFFFFF9C4) // Yellow 100
                    bottomColor = Color(0xFFFFCC80) // Orange 200
                }
                AppThemeType.BROWN -> {
                    // 温かいサンドカラーからミルクチョコへ
                    topColor = Color(0xFFFFF3E0) // Orange 50
                    bottomColor = Color(0xFFBCAAA4) // Brown 200
                }
                AppThemeType.GREEN -> {
                    // イエローグリーンから葉っぱの緑へ
                    topColor = Color(0xFFF1F8E9) // Light Green 50
                    bottomColor = Color(0xFFA5D6A7) // Green 200
                }
                AppThemeType.GREY -> {
                    // 真っ白からクールなブルーグレーへ（アクリルガラス風）
                    topColor = Color(0xFFFFFFFF) // Pure White
                    bottomColor = Color(0xFFCFD8DC) // Blue Grey 200
                }
                AppThemeType.MIKU -> {
                    // ユーザーさんのお気に入り！サイバー感のあるミクカラー
                    topColor = Color(0xFF80DEEA) // サイバー感のある明るいシアン
                    bottomColor = Color(0xFF1DE9B6) // 鮮やかなエメラルド系のティール
                }
                else -> {
                    topColor = MaterialTheme.colorScheme.primary
                    bottomColor = MaterialTheme.colorScheme.primaryContainer
                }
            }
        }
    }

    // 文字サイズ
    val fontSize = when {
        text in listOf("+", "-", "x", "÷", "=") -> 40.sp
        text in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "00", ".", "C", "AC") -> 32.sp
        else -> 20.sp
    }

    // ★ 文字色はすべて焦げ茶色に。影をつける。アクセントボタン以外。影をつける。
    val textColor = if (!isAccent) Color(0xFF5D4037) else Color.White
    val textShadow = if (!isAccent) Shadow(color = Color(0x66000000), offset = Offset(2f, 2f), blurRadius = 4f) else null

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(if (isRoundShape) CircleShape else RectangleShape)
            .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
            .clickable {
                if (vibrationEnabled) haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            color = textColor,
            style = TextStyle(shadow = textShadow), // ★影を適用
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun HelpKeyItem(keyText: String, description: String, currentTheme: AppThemeType) {
    // パステルテーマかどうかの判定 (上と同じロジック。ハードコードする)
    val isPastel = currentTheme in listOf(
        AppThemeType.MACARON, AppThemeType.COTTON_CANDY, AppThemeType.UNICORN,
        AppThemeType.SHERBET, AppThemeType.PEACH_MILK, AppThemeType.PISTACHIO, AppThemeType.LAVENDER
    )

    // グラデーションのベース色を取得（テーマごとの色を定義する。）
    var topColor = MaterialTheme.colorScheme.primary
    var bottomColor = MaterialTheme.colorScheme.primaryContainer

    if (!isPastel) {
        when (currentTheme) {
            AppThemeType.INDIGO -> { topColor = Color(0xFFE8EAF6); bottomColor = Color(0xFFC5CAE9) }
            AppThemeType.PINK -> { topColor = Color(0xFFFCE4EC); bottomColor = Color(0xFFF8BBD0) }
            AppThemeType.TEAL -> { topColor = Color(0xFFE0F2F1); bottomColor = Color(0xFFB2DFDB) }
            AppThemeType.ORANGE -> { topColor = Color(0xFFFFF3E0); bottomColor = Color(0xFFFFE0B2) }
            AppThemeType.BROWN -> { topColor = Color(0xFFEFEBE9); bottomColor = Color(0xFFD7CCC8) }
            AppThemeType.GREEN -> { topColor = Color(0xFFE8F5E9); bottomColor = Color(0xFFC8E6C9) }
            AppThemeType.GREY -> { topColor = Color(0xFFFAFAFA); bottomColor = Color(0xFFF5F5F5) }
            AppThemeType.MIKU -> { topColor = Color(0xFFE0F7FA); bottomColor = Color(0xFFB2EBF2) }
            else -> {} // 既にMaterialThemeの色
        }
    }

    // ★焦げ茶色と影にする
    val textColor = Color(0xFF5D4037)
    val textShadow = Shadow(color = Color(0x66000000), offset = Offset(2f, 2f), blurRadius = 4f)

    Row(modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(listOf(topColor, bottomColor))) // ★グラデーションに変更
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                keyText,
                fontWeight = FontWeight.Bold,
                color = textColor,
                style = TextStyle(shadow = textShadow), // ★影を適用
                fontSize = 14.sp,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        // 説明文の色は DarkGray のまま（焦げ茶より少し濃い）
        Text(description, fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
    }
}

@Composable
fun HelpScreen(currentTheme: AppThemeType, onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val errorMsg = stringResource(R.string.error_link_app_not_found)

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, containerColor = Color(0xFFFFF0F5)) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text(stringResource(R.string.help_title), fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 24.dp).verticalScroll(scrollState)) {
                HelpKeyItem("TAX±", stringResource(R.string.help_tax_desc), currentTheme)
                HelpKeyItem(stringResource(R.string.btn_settings), stringResource(R.string.help_tax_setting), currentTheme)
                HelpKeyItem(stringResource(R.string.btn_history), stringResource(R.string.help_history), currentTheme)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                HelpKeyItem("▶", stringResource(R.string.help_backspace), currentTheme)
                HelpKeyItem("C", stringResource(R.string.help_c), currentTheme)
                HelpKeyItem("AC", stringResource(R.string.help_ac), currentTheme)
                HelpKeyItem("M+", stringResource(R.string.help_m_plus), currentTheme)
                HelpKeyItem("M-", stringResource(R.string.help_m_minus), currentTheme)
                HelpKeyItem("CM", stringResource(R.string.help_cm), currentTheme)
                HelpKeyItem("RM", stringResource(R.string.help_rm), currentTheme)
                HelpKeyItem("+/-", stringResource(R.string.help_sign), currentTheme)
                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(R.string.help_credit), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = { try { uriHandler.openUri("https://www.youtube.com/@Tomato_Juice") } catch (e: Exception) { scope.launch { snackbarHostState.showSnackbar(errorMsg) } } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCD201F)), modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.help_youtube), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { try { uriHandler.openUri("https://tomatojuice.github.io/SkinCalculator/") } catch (e: Exception) { scope.launch { snackbarHostState.showSnackbar(errorMsg) } } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292E)), modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Icon(painterResource(R.drawable.ic_github), contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.help_github), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { try { uriHandler.openUri("https://tomatojuice.github.io/SkinCalculator/privacy") } catch (e: Exception) { scope.launch { snackbarHostState.showSnackbar(errorMsg) } } }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray), modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 12.dp)) {
                    Icon(Icons.Default.List, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.help_privacy), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            AdMobBanner()
        }
    }
}

@Composable
fun AdMobBanner() {
    Box(modifier = Modifier.fillMaxWidth().height(50.dp), contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.wrapContentSize(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = BuildConfig.ADMOB_BANNER_UNIT_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
fun getThemeNameString(theme: AppThemeType): String {
    val resId = when (theme) {
        AppThemeType.INDIGO -> R.string.theme_indigo
        AppThemeType.PINK -> R.string.theme_pink
        AppThemeType.TEAL -> R.string.theme_teal
        AppThemeType.ORANGE -> R.string.theme_orange
        AppThemeType.BROWN -> R.string.theme_brown
        AppThemeType.GREEN -> R.string.theme_green
        AppThemeType.GREY -> R.string.theme_grey
        AppThemeType.MIKU -> R.string.theme_miku
        AppThemeType.MACARON -> R.string.theme_macaron
        AppThemeType.COTTON_CANDY -> R.string.theme_cotton_candy
        AppThemeType.UNICORN -> R.string.theme_unicorn
        AppThemeType.SHERBET -> R.string.theme_sherbet
        AppThemeType.PEACH_MILK -> R.string.theme_peach_milk
        AppThemeType.PISTACHIO -> R.string.theme_pistachio
        AppThemeType.LAVENDER -> R.string.theme_lavender
    }
    return stringResource(resId)
}
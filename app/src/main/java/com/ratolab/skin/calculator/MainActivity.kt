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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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

class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                                        currentTheme = uiState.currentTheme, // ★この行を追加
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

    // ★ 全体をBoxで囲んでキラキラを重ねる
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF0F5))) {
                DisplayArea(Modifier.weight(1f).fillMaxHeight(), uiState, showMenu, uiState.isRoundShape, { showMenu = it }, { showHistorySheet = true }, onNavigateToHelp, onLanguageChange, onThemeChange, { showTaxDialog = true }, onShapeToggle, onVibToggle)
                KeypadArea(Modifier.weight(1.5f).fillMaxHeight(), uiState.isRoundShape, uiState.vibrationEnabled, onNumberClick, onOperatorClick, onEqualClick, onClearClick, onClearAllClick, onBackspaceClick, onMemoryPlusClick, onMemoryMinusClick, onMemoryClearClick, onMemoryRecallClick, onToggleSignClick, onSquareRootClick, onPercentClick, onTaxPlusClick, onTaxMinusClick)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF0F5))) {
                DisplayArea(Modifier.weight(1.0f).fillMaxWidth(), uiState, showMenu, uiState.isRoundShape, { showMenu = it }, { showHistorySheet = true }, onNavigateToHelp, onLanguageChange, onThemeChange, { showTaxDialog = true }, onShapeToggle, onVibToggle)
                KeypadArea(Modifier.weight(1.3f).fillMaxWidth(), uiState.isRoundShape, uiState.vibrationEnabled, onNumberClick, onOperatorClick, onEqualClick, onClearClick, onClearAllClick, onBackspaceClick, onMemoryPlusClick, onMemoryMinusClick, onMemoryClearClick, onMemoryRecallClick, onToggleSignClick, onSquareRootClick, onPercentClick, onTaxPlusClick, onTaxMinusClick)
            }
        }

        // ★ パステルテーマの時だけ、一番上にキラキラを描画する
        val isPastel = uiState.currentTheme in listOf(
            AppThemeType.MACARON, AppThemeType.COTTON_CANDY, AppThemeType.UNICORN,
            AppThemeType.SHERBET, AppThemeType.PEACH_MILK, AppThemeType.PISTACHIO, AppThemeType.LAVENDER
        )
        if (isPastel) {
            TwinkleBackground(modifier = Modifier.fillMaxSize())
        }
    }

    if (showTaxDialog) {
        AlertDialog(
            onDismissRequest = { showTaxDialog = false },
            title = { Text(stringResource(R.string.dialog_tax_title),color = MaterialTheme.colorScheme.onSurfaceVariant) },
            text = { OutlinedTextField(value = currentTaxRate, onValueChange = { currentTaxRate = it }, label = { Text(stringResource(R.string.dialog_tax_label)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )) },
            confirmButton = { TextButton(onClick = { onSaveTaxRate(currentTaxRate); showTaxDialog = false },colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )) { Text(stringResource(R.string.dialog_save)) } },
            dismissButton = { TextButton(onClick = { showTaxDialog = false },colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )) { Text(stringResource(R.string.dialog_cancel)) } }
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
                if (uiState.history.isEmpty()) Text(stringResource(R.string.history_empty), color = Color.Gray, modifier = Modifier.padding(16.dp))
                else LazyColumn { items(uiState.history.reversed()) { Text(it, fontSize = 24.sp, modifier = Modifier.padding(8.dp).fillMaxWidth(), textAlign = TextAlign.End); Divider(color = Color.LightGray.copy(alpha = 0.5f)) } }
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
                        // 日本語
                        DropdownMenuItem(text = { Text(stringResource(R.string.lang_japanese), fontWeight = if (uiState.languageCode == "ja") FontWeight.Bold else FontWeight.Normal, color = if (uiState.languageCode == "ja") MaterialTheme.colorScheme.primary else Color.Unspecified) }, onClick = { onLanguageChange("ja"); onMenuToggle(false) })
                        // 英語
                        DropdownMenuItem(text = { Text(stringResource(R.string.lang_english), fontWeight = if (uiState.languageCode == "en") FontWeight.Bold else FontWeight.Normal, color = if (uiState.languageCode == "en") MaterialTheme.colorScheme.primary else Color.Unspecified) }, onClick = { onLanguageChange("en"); onMenuToggle(false) })
                        // ★ ここから追加: 新しい5言語
                        // スペイン語 (es)
                        DropdownMenuItem(text = { Text(stringResource(R.string.lang_spanish), fontWeight = if (uiState.languageCode == "es") FontWeight.Bold else FontWeight.Normal, color = if (uiState.languageCode == "es") MaterialTheme.colorScheme.primary else Color.Unspecified) }, onClick = { onLanguageChange("es"); onMenuToggle(false) })
                        // ドイツ語 (de)
                        DropdownMenuItem(text = { Text(stringResource(R.string.lang_german), fontWeight = if (uiState.languageCode == "de") FontWeight.Bold else FontWeight.Normal, color = if (uiState.languageCode == "de") MaterialTheme.colorScheme.primary else Color.Unspecified) }, onClick = { onLanguageChange("de"); onMenuToggle(false) })
                        // ロシア語 (ru)
                        DropdownMenuItem(text = { Text(stringResource(R.string.lang_russian), fontWeight = if (uiState.languageCode == "ru") FontWeight.Bold else FontWeight.Normal, color = if (uiState.languageCode == "ru") MaterialTheme.colorScheme.primary else Color.Unspecified) }, onClick = { onLanguageChange("ru"); onMenuToggle(false) })
                        // 中国語 (zh)
                        DropdownMenuItem(text = { Text(stringResource(R.string.lang_chinese), fontWeight = if (uiState.languageCode == "zh") FontWeight.Bold else FontWeight.Normal, color = if (uiState.languageCode == "zh") MaterialTheme.colorScheme.primary else Color.Unspecified) }, onClick = { onLanguageChange("zh"); onMenuToggle(false) })
                        // 韓国語 (ko)
                        DropdownMenuItem(text = { Text(stringResource(R.string.lang_korean), fontWeight = if (uiState.languageCode == "ko") FontWeight.Bold else FontWeight.Normal, color = if (uiState.languageCode == "ko") MaterialTheme.colorScheme.primary else Color.Unspecified) }, onClick = { onLanguageChange("ko"); onMenuToggle(false) })
                    }
                }
            }
        }
// テキストが変わるたびに倍率を1.0 (元のサイズ) にリセットする
        var fontSizeMultiplier by remember(uiState.displayText) { mutableFloatStateOf(1f) }

        Text(
            text = uiState.displayText,
            fontSize = 64.sp * fontSizeMultiplier, // 基本サイズ × 倍率
            color = Color.DarkGray,
            textAlign = TextAlign.End,
            maxLines = 1,
            softWrap = false,
            onTextLayout = { textLayoutResult ->
                // もし描画領域（左端）をはみ出したら、文字サイズを0.9倍にして再描画ループ
                if (textLayoutResult.hasVisualOverflow) {
                    fontSizeMultiplier *= 0.9f
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd).fillMaxWidth()
        )
    }
}

@Composable
fun KeypadArea(modifier: Modifier, isRoundShape: Boolean, vibrationEnabled: Boolean, onNumberClick: (String) -> Unit, onOperatorClick: (Int) -> Unit, onEqualClick: () -> Unit, onClearClick: () -> Unit, onClearAllClick: () -> Unit, onBackspaceClick: () -> Unit, onMemoryPlusClick: () -> Unit, onMemoryMinusClick: () -> Unit, onMemoryClearClick: () -> Unit, onMemoryRecallClick: () -> Unit, onToggleSignClick: () -> Unit, onSquareRootClick: () -> Unit, onPercentClick: () -> Unit, onTaxPlusClick: () -> Unit, onTaxMinusClick: () -> Unit) {
    val spacing = if (isRoundShape) 3.dp else 1.dp
    val outerPadding = if (isRoundShape) 3.dp else 0.dp
    Column(modifier = modifier.background(Color(0xFFFCE4EC)).padding(outerPadding), verticalArrangement = Arrangement.spacedBy(spacing)) {
        val rowMod = Modifier.weight(1f).fillMaxWidth()
        Row(rowMod, Arrangement.spacedBy(spacing)) { CalcGridButton("TAX-", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, onTaxMinusClick); CalcGridButton("TAX+", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, onTaxPlusClick); CalcGridButton("▶", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, onBackspaceClick); CalcGridButton("C", Modifier.weight(1f), true, isRoundShape, vibrationEnabled, onClearClick); CalcGridButton("AC", Modifier.weight(1f), true, isRoundShape, vibrationEnabled, onClearAllClick) }
        Row(rowMod, Arrangement.spacedBy(spacing)) { CalcGridButton("M+", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, onMemoryPlusClick); CalcGridButton("M-", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, onMemoryMinusClick); CalcGridButton("CM", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, onMemoryClearClick); CalcGridButton("RM", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, onMemoryRecallClick); CalcGridButton("+/-", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, onToggleSignClick) }
        Row(rowMod, Arrangement.spacedBy(spacing)) { CalcGridButton("7", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onNumberClick("7") }; CalcGridButton("8", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onNumberClick("8") }; CalcGridButton("9", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onNumberClick("9") }; CalcGridButton("%", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, onPercentClick); CalcGridButton("√", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, onSquareRootClick) }
        Row(rowMod, Arrangement.spacedBy(spacing)) { CalcGridButton("4", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onNumberClick("4") }; CalcGridButton("5", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onNumberClick("5") }; CalcGridButton("6", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onNumberClick("6") }; CalcGridButton("x", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onOperatorClick(3) }; CalcGridButton("÷", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onOperatorClick(4) } }
        Row(rowMod, Arrangement.spacedBy(spacing)) { CalcGridButton("1", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onNumberClick("1") }; CalcGridButton("2", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onNumberClick("2") }; CalcGridButton("3", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onNumberClick("3") }; CalcGridButton("+", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onOperatorClick(1) }; CalcGridButton("-", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onOperatorClick(2) } }
        Row(rowMod, Arrangement.spacedBy(spacing)) { CalcGridButton("0", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onNumberClick("0") }; CalcGridButton("00", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onNumberClick("00") }; CalcGridButton(".", Modifier.weight(1f), false, isRoundShape, vibrationEnabled) { onNumberClick(".") }; CalcGridButton("=", Modifier.weight(2f), false, isRoundShape, vibrationEnabled, onEqualClick) }
    }
}

@Composable
fun CalcGridButton(text: String, modifier: Modifier, isAccent: Boolean, isRoundShape: Boolean, vibrationEnabled: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val topColor = if (isAccent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val bottomColor = if (isAccent) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
    val fontSize = when { text in listOf("+", "-", "x", "÷", "=") -> 40.sp; text in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "00", ".", "C", "AC") -> 32.sp; else -> 20.sp }
    Box(modifier = modifier.fillMaxHeight().clip(if (isRoundShape) CircleShape else RectangleShape).background(Brush.verticalGradient(listOf(topColor, bottomColor))).clickable { if (vibrationEnabled) haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap); onClick() }, contentAlignment = Alignment.Center) { Text(text, fontSize = fontSize, color = Color.White, maxLines = 1, softWrap = false) }
}

@Composable
fun HelpKeyItem(keyText: String, description: String, currentTheme: AppThemeType) { // ★引数を追加
    // パステルテーマかどうかの判定
    val isPastel = currentTheme in listOf(
        AppThemeType.MACARON, AppThemeType.COTTON_CANDY, AppThemeType.UNICORN,
        AppThemeType.SHERBET, AppThemeType.PEACH_MILK, AppThemeType.PISTACHIO, AppThemeType.LAVENDER
    )

    // グラデーションのベース色を取得
    val topColor = MaterialTheme.colorScheme.primary
    val bottomColor = MaterialTheme.colorScheme.primaryContainer
    // パステルなら暗い文字色、それ以外は白文字にする
    val textColor = if (isPastel) Color.DarkGray else Color.White

    Row(modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(listOf(topColor, bottomColor))), // ★グラデーションに変更
            contentAlignment = Alignment.Center
        ) {
            Text(keyText, fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp) // ★文字色を適用
        }
        Spacer(modifier = Modifier.width(12.dp))
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFFFF0F5)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(stringResource(R.string.help_title), fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 24.dp).verticalScroll(scrollState)) {
                HelpKeyItem("TAX±", stringResource(R.string.help_tax_desc), currentTheme)
                HelpKeyItem("設定", stringResource(R.string.help_tax_setting), currentTheme)
                HelpKeyItem("履歴", stringResource(R.string.help_history), currentTheme)
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

                Button(
                    onClick = {
                        try { uriHandler.openUri("https://www.youtube.com/@Tomato_Juice") } catch (e: Exception) { scope.launch { snackbarHostState.showSnackbar(errorMsg) } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCD201F)),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.help_youtube), color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        try { uriHandler.openUri("https://tomatojuice.github.io/Skin-Calculator/") } catch (e: Exception) { scope.launch { snackbarHostState.showSnackbar(errorMsg) } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292E)),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_github), contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.help_github), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        try { uriHandler.openUri("https://tomatojuice.github.io/Skin-Calculator/privacy") } catch (e: Exception) { scope.launch { snackbarHostState.showSnackbar(errorMsg) } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 12.dp)
                ) {
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
    Box(
        modifier = Modifier.fillMaxWidth().height(50.dp),
        contentAlignment = Alignment.Center
    ) {
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
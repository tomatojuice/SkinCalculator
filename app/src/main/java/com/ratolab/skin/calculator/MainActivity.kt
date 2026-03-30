package com.ratolab.skin.calculator

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.ratolab.skin.calculator.ui.theme.getAppThemeConfig
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

                    MaterialTheme {
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
    var showThemeSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var currentTaxRate by remember(uiState.taxRate) { mutableStateOf(uiState.taxRate) }

    val themeConfig = getAppThemeConfig(uiState.currentTheme)

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize().background(themeConfig.displayBg)) {
                DisplayArea(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    uiState = uiState,
                    showMenu = showMenu,
                    isRoundShape = uiState.isRoundShape,
                    onMenuToggle = { showMenu = it },
                    onShowHistory = { showHistorySheet = true },
                    onNavigateToHelp = onNavigateToHelp,
                    onShowThemeSheet = { showThemeSheet = true },
                    onShowLanguageSheet = { showLanguageSheet = true },
                    onShowTaxDialog = { showTaxDialog = true },
                    onShapeToggle = onShapeToggle,
                    onVibToggle = onVibToggle
                )
                KeypadArea(
                    modifier = Modifier.weight(1.5f).fillMaxHeight(),
                    currentTheme = uiState.currentTheme,
                    isRoundShape = uiState.isRoundShape,
                    vibrationEnabled = uiState.vibrationEnabled,
                    keypadBg = themeConfig.keypadBg,
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
            Column(modifier = Modifier.fillMaxSize().background(themeConfig.displayBg)) {
                DisplayArea(
                    modifier = Modifier.weight(1.0f).fillMaxWidth(),
                    uiState = uiState,
                    showMenu = showMenu,
                    isRoundShape = uiState.isRoundShape,
                    onMenuToggle = { showMenu = it },
                    onShowHistory = { showHistorySheet = true },
                    onNavigateToHelp = onNavigateToHelp,
                    onShowThemeSheet = { showThemeSheet = true },
                    onShowLanguageSheet = { showLanguageSheet = true },
                    onShowTaxDialog = { showTaxDialog = true },
                    onShapeToggle = onShapeToggle,
                    onVibToggle = onVibToggle
                )
                KeypadArea(
                    modifier = Modifier.weight(1.3f).fillMaxWidth(),
                    currentTheme = uiState.currentTheme,
                    isRoundShape = uiState.isRoundShape,
                    vibrationEnabled = uiState.vibrationEnabled,
                    keypadBg = themeConfig.keypadBg,
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
        TwinkleBackground(modifier = Modifier.fillMaxSize())
    }

    if (showThemeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ThemeSelectionSheetContent(
                currentTheme = uiState.currentTheme,
                onThemeSelected = { selectedTheme ->
                    onThemeChange(selectedTheme)
                    showThemeSheet = false
                }
            )
        }
    }

    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            LanguageSelectionSheetContent(
                currentLanguageCode = uiState.languageCode,
                onLanguageSelected = { selectedLang ->
                    onLanguageChange(selectedLang)
                    showLanguageSheet = false
                }
            )
        }
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
                                color = Color.DarkGray,
                                style = TextStyle(shadow = null)
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
fun LanguageSelectionSheetContent(currentLanguageCode: String, onLanguageSelected: (String) -> Unit) {
    val languages = listOf(
        "ja" to R.string.lang_japanese, "en" to R.string.lang_english, "es" to R.string.lang_spanish,
        "de" to R.string.lang_german, "ru" to R.string.lang_russian, "zh" to R.string.lang_chinese, "ko" to R.string.lang_korean,
        "hi" to R.string.lang_hindi, "fr" to R.string.lang_french
    )

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(stringResource(R.string.menu_language), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, modifier = Modifier.padding(bottom = 16.dp))
        LazyColumn(modifier = Modifier.padding(bottom = 32.dp)) {
            items(languages) { (code, resId) ->
                val isSelected = currentLanguageCode == code
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onLanguageSelected(code) }.padding(vertical = 16.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(resId), fontSize = 18.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray)
                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun ThemeSelectionSheetContent(currentTheme: AppThemeType, onThemeSelected: (AppThemeType) -> Unit) {
    val pastelThemes = listOf(AppThemeType.MACARON, AppThemeType.COTTON_CANDY, AppThemeType.UNICORN, AppThemeType.SHERBET, AppThemeType.PEACH_MILK, AppThemeType.PISTACHIO, AppThemeType.LAVENDER, AppThemeType.MARBLE)
    val standardThemes = listOf(AppThemeType.INDIGO, AppThemeType.PINK, AppThemeType.TEAL, AppThemeType.ORANGE, AppThemeType.BROWN, AppThemeType.GREEN, AppThemeType.GREY)

    // ★ リストに TETO, YUKARI を追加！
    val specialThemes = listOf(AppThemeType.MIKU, AppThemeType.GUMI, AppThemeType.LUKA, AppThemeType.LIN, AppThemeType.REN, AppThemeType.TETO, AppThemeType.YUKARI)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(stringResource(R.string.menu_skin), fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            ThemeCategoryRow(title = stringResource(R.string.category_pastel), themes = pastelThemes, currentTheme = currentTheme, onThemeSelected = onThemeSelected)
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
            ThemeCategoryRow(title = stringResource(R.string.category_standard), themes = standardThemes, currentTheme = currentTheme, onThemeSelected = onThemeSelected)
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
            ThemeCategoryRow(title = stringResource(R.string.category_special), themes = specialThemes, currentTheme = currentTheme, onThemeSelected = onThemeSelected)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ThemeCategoryRow(title: String, themes: List<AppThemeType>, currentTheme: AppThemeType, onThemeSelected: (AppThemeType) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, modifier = Modifier.padding(bottom = 12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            items(themes) { theme ->
                ThemeSwatch(theme = theme, isSelected = currentTheme == theme, onClick = { onThemeSelected(theme) })
            }
        }
    }
}

@Composable
fun ThemeSwatch(theme: AppThemeType, isSelected: Boolean, onClick: () -> Unit) {
    val config = getAppThemeConfig(theme)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(config.btnTop, config.btnBottom)))
                .clickable { onClick() }
                .then(if (isSelected) Modifier.border(4.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) { }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = getThemeNameString(theme),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DisplayArea(modifier: Modifier, uiState: CalculatorUiState, showMenu: Boolean, isRoundShape: Boolean, onMenuToggle: (Boolean) -> Unit, onShowHistory: () -> Unit, onNavigateToHelp: () -> Unit, onShowThemeSheet: () -> Unit, onShowLanguageSheet: () -> Unit, onShowTaxDialog: () -> Unit, onShapeToggle: (Boolean) -> Unit, onVibToggle: (Boolean) -> Unit) {
    Box(modifier = modifier.padding(start = 16.dp, top = 16.dp, end = 4.dp, bottom = 16.dp)) {
        Row(modifier = Modifier.align(Alignment.TopEnd)) {
            IconButton(onClick = onShowHistory) { Icon(Icons.Default.List, stringResource(R.string.cd_history), tint = Color.Gray) }
            Box {
                IconButton(onClick = { onMenuToggle(!showMenu) }) { Icon(Icons.Default.MoreVert, stringResource(R.string.cd_menu), tint = Color.Gray) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { onMenuToggle(false) }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.menu_skin)) }, onClick = { onMenuToggle(false); onShowThemeSheet() })
                    DropdownMenuItem(text = { Text(stringResource(R.string.menu_language)) }, onClick = { onMenuToggle(false); onShowLanguageSheet() })
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
                }
            }
        }
        var fontSizeMultiplier by remember(uiState.displayText) { mutableFloatStateOf(1f) }
        Text(
            text = uiState.displayText,
            fontSize = 64.sp * fontSizeMultiplier,
            color = Color.DarkGray,
            style = TextStyle(shadow = null),
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
    keypadBg: Color,
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

    Column(modifier = modifier.background(keypadBg).padding(outerPadding), verticalArrangement = Arrangement.spacedBy(spacing)) {
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
            CalcGridButton("×", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onOperatorClick(3) }
            CalcGridButton("÷", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onOperatorClick(4) }
        }
        Row(modifier = rowMod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            CalcGridButton("1", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("1") }
            CalcGridButton("2", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("2") }
            CalcGridButton("3", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onNumberClick("3") }
            CalcGridButton("+", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onOperatorClick(1) }
            CalcGridButton("-", Modifier.weight(1f), false, isRoundShape, vibrationEnabled, currentTheme) { onOperatorClick(2) }
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
    val config = getAppThemeConfig(currentTheme)
    val topColor = if (isAccent) config.accentTop else config.btnTop
    val bottomColor = if (isAccent) config.accentBottom else config.btnBottom

    val fontSize = when {
        text in listOf("+", "-", "×", "÷", "=") -> 40.sp
        text in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "00", ".", "C", "AC") -> 32.sp
        else -> 20.sp
    }

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
            style = TextStyle(shadow = textShadow),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun HelpKeyItem(keyText: String, description: String, currentTheme: AppThemeType) {
    val config = getAppThemeConfig(currentTheme)
    val textColor = Color(0xFF5D4037)
    val textShadow = Shadow(color = Color(0x66000000), offset = Offset(2f, 2f), blurRadius = 4f)

    Row(modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(listOf(config.btnTop, config.btnBottom)))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                keyText,
                fontWeight = FontWeight.Bold,
                color = textColor,
                style = TextStyle(shadow = textShadow),
                fontSize = 14.sp,
                maxLines = 1
            )
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
    val config = getAppThemeConfig(currentTheme)

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, containerColor = config.displayBg) { paddingValues ->
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
        AppThemeType.MARBLE -> R.string.theme_marble
        AppThemeType.GUMI -> R.string.theme_gumi
        AppThemeType.LUKA -> R.string.theme_luka
        AppThemeType.LIN -> R.string.theme_lin
        AppThemeType.REN -> R.string.theme_ren

        // ★追加
        AppThemeType.TETO -> R.string.theme_teto
        AppThemeType.YUKARI -> R.string.theme_yukari
    }
    return stringResource(resId)
}
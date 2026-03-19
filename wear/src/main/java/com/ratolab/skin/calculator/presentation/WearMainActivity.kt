package com.ratolab.skin.calculator.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material3.Text
import com.ratolab.skin.calculator.presentation.theme.AppThemeType
import com.ratolab.skin.calculator.presentation.theme.WearThemeColors
import com.ratolab.skin.calculator.presentation.theme.getWearThemeColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class WearMainActivity : ComponentActivity() {
    private val viewModel: WearCalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            var currentTheme by remember { mutableStateOf(AppThemeType.MIKU) }
            val themeColors = getWearThemeColors(currentTheme)
            val context = LocalContext.current
            var targetRotation by remember { mutableFloatStateOf(0f) }
            val rotation by animateFloatAsState(
                targetValue = targetRotation,
                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
                label = "sensor_rotation"
            )

            DisposableEffect(Unit) {
                val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
                val gravitySensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_GRAVITY)
                val listener = object : android.hardware.SensorEventListener {
                    override fun onSensorChanged(event: android.hardware.SensorEvent) {
                        targetRotation = (event.values[0] * 12f) - (event.values[1] * 8f)
                    }
                    override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
                }
                sensorManager.registerListener(listener, gravitySensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
                onDispose { sensorManager.unregisterListener(listener) }
            }

            val focusRequester = remember { FocusRequester() }
            var rotaryAccumulator by remember { mutableFloatStateOf(0f) }
            val rotaryThreshold = 50f
            LaunchedEffect(Unit) { focusRequester.requestFocus() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(themeColors.bg)
                    .onRotaryScrollEvent { event ->
                        rotaryAccumulator += event.verticalScrollPixels
                        if (rotaryAccumulator > rotaryThreshold) {
                            val nextOrdinal = (currentTheme.ordinal + 1) % AppThemeType.entries.size
                            currentTheme = AppThemeType.entries[nextOrdinal]
                            rotaryAccumulator = 0f
                        } else if (rotaryAccumulator < -rotaryThreshold) {
                            val prevOrdinal = (currentTheme.ordinal - 1 + AppThemeType.entries.size) % AppThemeType.entries.size
                            currentTheme = AppThemeType.entries[prevOrdinal]
                            rotaryAccumulator = 0f
                        }
                        true
                    }
                    .focusRequester(focusRequester)
                    .focusable()
            ) {
                val configuration = LocalConfiguration.current
                val isRound = configuration.isScreenRound
                val screenWidth = configuration.screenWidthDp.dp
                val screenHeight = configuration.screenHeightDp.dp

                val safeRatio = 0.72f
                val safeAreaSide = if (isRound) screenWidth * safeRatio else screenWidth

                val horizontalMargin = (screenWidth - safeAreaSide) / 2
                val verticalMargin = (screenHeight - safeAreaSide) / 2

                TwinkleBackground(modifier = Modifier.fillMaxSize())
                RichWatchBackground(themeColors = themeColors, rotation = rotation)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = horizontalMargin, end = horizontalMargin, top = verticalMargin, bottom = verticalMargin),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var fontSizeMultiplier by remember(uiState.displayText) { mutableFloatStateOf(1f)}
                    val baseTextSize = (safeAreaSide.value * 0.22f).sp

                    Text(
                        text = uiState.displayText,
                        fontSize = baseTextSize * fontSizeMultiplier,
                        color = Color.DarkGray,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        softWrap = false,
                        onTextLayout = { textLayoutResult ->
                            if (textLayoutResult.hasVisualOverflow) {
                                fontSizeMultiplier *= 0.9f
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val nextOrdinal = (currentTheme.ordinal + 1) % AppThemeType.entries.size
                                currentTheme = AppThemeType.entries[nextOrdinal]
                            }
                            .padding(bottom = (safeAreaSide.value * 0.05f).dp, end = 4.dp)
                    )

                    WearKeypadArea(
                        modifier = Modifier.weight(1f),
                        themeColors = themeColors,
                        rotation = rotation,
                        buttonFontSize = (safeAreaSide.value * 0.12f).sp,
                        onNumberClick = { viewModel.onInputNumber(it) },
                        onOperatorClick = { viewModel.onOperatorClick(it) },
                        onEqualClick = { viewModel.onEqualClick() },
                        onClearClick = { viewModel.onClearC() },
                        onClearAllClick = { viewModel.onClearAC() },
                        onBackspaceClick = { viewModel.onBackspace() }
                    )
                }
            }
        }
    }
}

@Composable
fun WearKeypadArea(
    modifier: Modifier = Modifier,
    themeColors: WearThemeColors,
    rotation: Float,
    buttonFontSize: androidx.compose.ui.unit.TextUnit,
    onNumberClick: (String) -> Unit,
    onOperatorClick: (Int) -> Unit,
    onEqualClick: () -> Unit,
    onClearClick: () -> Unit,
    onClearAllClick: () -> Unit,
    onBackspaceClick: () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            WearCalcButton("AC", Modifier.weight(1f), true, themeColors, rotation, buttonFontSize) { onClearAllClick() }
            WearCalcButton("C", Modifier.weight(1f), true, themeColors, rotation, buttonFontSize) { onClearClick() }
            WearCalcButton("⌫", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onBackspaceClick() }
            WearCalcButton("÷", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onOperatorClick(4) }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            WearCalcButton("7", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onNumberClick("7") }
            WearCalcButton("8", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onNumberClick("8") }
            WearCalcButton("9", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onNumberClick("9") }
            WearCalcButton("×", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onOperatorClick(3) }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            WearCalcButton("4", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onNumberClick("4") }
            WearCalcButton("5", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onNumberClick("5") }
            WearCalcButton("6", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onNumberClick("6") }
            WearCalcButton("-", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onOperatorClick(2) }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            WearCalcButton("1", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onNumberClick("1") }
            WearCalcButton("2", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onNumberClick("2") }
            WearCalcButton("3", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onNumberClick("3") }
            WearCalcButton("+", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onOperatorClick(1) }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            WearCalcButton("00", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onNumberClick("00") }
            WearCalcButton("0", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onNumberClick("0") }
            WearCalcButton(".", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onNumberClick(".") }
            WearCalcButton("=", Modifier.weight(1f), false, themeColors, rotation, buttonFontSize) { onEqualClick() }
        }
    }
}

@Composable
fun WearCalcButton(
    text: String,
    modifier: Modifier,
    isAccent: Boolean,
    themeColors: WearThemeColors,
    rotation: Float,
    baseFontSize: androidx.compose.ui.unit.TextUnit,
    onClick: () -> Unit
) {
    val textColor = if (isAccent) Color.White else Color(0xFF5D4037)
    val baseBrush = if (isAccent) {
        Brush.verticalGradient(listOf(themeColors.accentTop, themeColors.accentBottom))
    } else {
        Brush.verticalGradient(listOf(themeColors.btnTop, themeColors.btnBottom))
    }

    val highlight = Color.White.copy(alpha = 0.25f)
    val midTone = Color.Transparent
    val shadow = Color.Black.copy(alpha = 0.15f)

    val finalFontSize = when {
        text.length >= 2 -> baseFontSize * 0.8f
        text == "⌫" -> baseFontSize * 1.1f
        else -> baseFontSize
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.width / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(brush = baseBrush, radius = radius, center = center)

            val sweepBrush = Brush.sweepGradient(
                0.0f to midTone, 0.15f to highlight, 0.3f to midTone, 0.5f to shadow,
                0.65f to highlight, 0.8f to midTone, 1.0f to midTone, center = center
            )

            withTransform({ rotate(rotation, center) }) {
                drawCircle(brush = sweepBrush, radius = radius, center = center)
            }

            drawCircle(color = Color.White.copy(alpha = 0.3f), radius = radius - 1.dp.toPx(), center = center.copy(x = center.x - 1.dp.toPx(), y = center.y - 1.dp.toPx()), style = Stroke(width = 1.dp.toPx()))
            drawCircle(color = Color.Black.copy(alpha = 0.2f), radius = radius - 1.dp.toPx(), center = center.copy(x = center.x + 1.dp.toPx(), y = center.y + 1.dp.toPx()), style = Stroke(width = 1.dp.toPx()))
        }

        Text(text = text, fontSize = finalFontSize, color = textColor, maxLines = 1, softWrap = false)
    }
}

@Composable
fun RichWatchBackground(themeColors: WearThemeColors, rotation: Float) {
    val baseBrush = Brush.verticalGradient(listOf(themeColors.btnTop, themeColors.btnBottom))
    val highlight = Color.White.copy(alpha = 0.25f)
    val midTone = Color.Transparent
    val shadow = Color.Black.copy(alpha = 0.15f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.width / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(brush = baseBrush, radius = radius, center = center)

        val sweepBrush = Brush.sweepGradient(
            0.0f to midTone, 0.15f to highlight, 0.3f to midTone, 0.5f to shadow,
            0.65f to highlight, 0.8f to midTone, 1.0f to midTone, center = center
        )

        withTransform({ rotate(rotation, center) }) {
            drawCircle(brush = sweepBrush, radius = radius, center = center)
        }

        drawCircle(
            brush = Brush.radialGradient(colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.2f)), center = center, radius = radius),
            radius = radius, center = center
        )
    }
}
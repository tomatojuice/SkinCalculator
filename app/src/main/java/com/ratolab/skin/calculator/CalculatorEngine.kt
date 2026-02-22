package com.ratolab.skin.calculator

import android.util.Log
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.sqrt

// ★ 変更：履歴（newHistoryLine）をUIに渡せるように追加
data class EngineState(
    val displayText: String,
    val hasMemory: Boolean,
    val newHistoryLine: String? = null
)

class CalculatorEngine {
    private var currentInput = "0"
    private var previousValue = BigDecimal.ZERO
    private var currentOperator = 0 // 0:なし, 1:+, 2:-, 3:x, 4:÷
    private var memoryValue = BigDecimal.ZERO

    private var isWaitingForNextNumber = true
    private var lastActionWasOperator = false

    private val TAG = "CalcEngine"

    private fun logState(action: String) {
        Log.d(TAG, "[$action] Disp: $currentInput, Prev: $previousValue, Op: $currentOperator, Mem: $memoryValue, Wait: $isWaitingForNextNumber, LastOp: $lastActionWasOperator")
    }

    private fun getState(): EngineState {
        return EngineState(
            displayText = addCommas(formatDisplay(currentInput)),
            hasMemory = memoryValue.compareTo(BigDecimal.ZERO) != 0
        )
    }

    private fun addCommas(value: String): String {
        if (value == "Error" || value == "-") return value
        val parts = value.split(".")
        val integerPart = parts[0]
        val fractionalPart = if (value.contains(".")) {
            if (parts.size > 1) "." + parts[1] else "."
        } else ""

        val isNegative = integerPart.startsWith("-")
        val absoluteInteger = if (isNegative) integerPart.substring(1) else integerPart
        val formattedInteger = absoluteInteger.reversed().chunked(3).joinToString(",").reversed()
        val resultInteger = if (isNegative) "-$formattedInteger" else formattedInteger
        return resultInteger + fractionalPart
    }

    private fun formatBigDecimal(bd: BigDecimal): String {
        val stripped = bd.stripTrailingZeros()
        if (stripped.compareTo(BigDecimal.ZERO) == 0) return "0"
        val rounded = stripped.round(MathContext(10, RoundingMode.HALF_UP))
        return rounded.stripTrailingZeros().toPlainString()
    }

    private fun formatDisplay(value: String): String {
        if (value == "Error") return value
        if (value.endsWith(".")) return value
        return try {
            formatBigDecimal(BigDecimal(value))
        } catch (e: Exception) {
            value
        }
    }

    fun inputNumber(number: String): EngineState {
        if (isWaitingForNextNumber) {
            currentInput = if (number == ".") "0." else number
            isWaitingForNextNumber = false
        } else {
            if (number == "." && currentInput.contains(".")) return getState()
            if (currentInput == "0" && number != ".") {
                currentInput = number
            } else {
                currentInput += number
            }
        }
        lastActionWasOperator = false
        logState("inputNumber($number)")
        return getState()
    }

    fun operatorClick(operator: Int): EngineState {
        if (!lastActionWasOperator) {
            calculatePending()
        }
        currentOperator = operator
        isWaitingForNextNumber = true
        lastActionWasOperator = true
        logState("operatorClick($operator)")
        return getState()
    }

    // ★ 変更：イコールが押された瞬間に「計算式」の文字列を作る
    fun equalClick(): EngineState {
        val prevStr = addCommas(formatBigDecimal(previousValue))
        val opStr = when (currentOperator) {
            1 -> "+"
            2 -> "-"
            3 -> "x"
            4 -> "÷"
            else -> ""
        }
        val currentStr = addCommas(try { formatBigDecimal(BigDecimal(currentInput)) } catch(e:Exception){"0"})
        val isCalculable = currentOperator != 0 && !isWaitingForNextNumber

        calculatePending()

        var historyLine: String? = null
        if (isCalculable && currentInput != "Error") {
            val resultStr = addCommas(formatDisplay(currentInput))
            historyLine = "$prevStr $opStr $currentStr = $resultStr"
        }

        currentOperator = 0
        isWaitingForNextNumber = true
        lastActionWasOperator = false
        logState("equalClick")

        return EngineState(
            displayText = addCommas(formatDisplay(currentInput)),
            hasMemory = memoryValue.compareTo(BigDecimal.ZERO) != 0,
            newHistoryLine = historyLine
        )
    }

    private fun calculatePending() {
        val currentVal = try { BigDecimal(currentInput) } catch (e: Exception) { BigDecimal.ZERO }
        try {
            previousValue = when (currentOperator) {
                1 -> previousValue.add(currentVal)
                2 -> previousValue.subtract(currentVal)
                3 -> previousValue.multiply(currentVal)
                4 -> {
                    if (currentVal.compareTo(BigDecimal.ZERO) == 0) {
                        currentInput = "Error"
                        return
                    }
                    previousValue.divide(currentVal, 10, RoundingMode.HALF_UP)
                }
                else -> currentVal
            }
            if (currentInput != "Error") {
                currentInput = formatBigDecimal(previousValue)
            }
        } catch (e: Exception) {
            currentInput = "Error"
            Log.e(TAG, "Calculate Error", e)
        }
        logState("calculatePending")
    }

    fun clearC(): EngineState {
        currentInput = "0"
        isWaitingForNextNumber = true
        logState("clearC (CE)")
        return getState()
    }

    fun clearAC(): EngineState {
        currentInput = "0"
        previousValue = BigDecimal.ZERO
        currentOperator = 0
        memoryValue = BigDecimal.ZERO
        isWaitingForNextNumber = true
        lastActionWasOperator = false
        logState("clearAC (CA)")
        return getState()
    }

    fun backspace(): EngineState {
        if (isWaitingForNextNumber) return getState()
        currentInput = if (currentInput.length > 1) {
            currentInput.dropLast(1)
        } else {
            "0"
        }
        if (currentInput == "-") currentInput = "0"
        logState("backspace")
        return getState()
    }

    fun memoryPlus(): EngineState {
        val currentVal = try { BigDecimal(currentInput) } catch(e: Exception) { BigDecimal.ZERO }
        memoryValue = memoryValue.add(currentVal)
        isWaitingForNextNumber = true
        lastActionWasOperator = false
        logState("memoryPlus")
        return getState()
    }

    fun memoryMinus(): EngineState {
        val currentVal = try { BigDecimal(currentInput) } catch(e: Exception) { BigDecimal.ZERO }
        memoryValue = memoryValue.subtract(currentVal)
        isWaitingForNextNumber = true
        lastActionWasOperator = false
        logState("memoryMinus")
        return getState()
    }

    fun memoryClear(): EngineState {
        memoryValue = BigDecimal.ZERO
        logState("memoryClear")
        return getState()
    }

    fun memoryRecall(): EngineState {
        currentInput = formatBigDecimal(memoryValue)
        isWaitingForNextNumber = true
        lastActionWasOperator = false
        logState("memoryRecall")
        return getState()
    }

    fun toggleSign(): EngineState {
        if (currentInput == "0" || currentInput == "Error") return getState()
        currentInput = if (currentInput.startsWith("-")) {
            currentInput.drop(1)
        } else {
            "-$currentInput"
        }
        lastActionWasOperator = false
        logState("toggleSign")
        return getState()
    }

    fun squareRoot(): EngineState {
        try {
            val d = currentInput.toDoubleOrNull() ?: 0.0
            if (d >= 0) {
                val result = BigDecimal(sqrt(d))
                currentInput = formatBigDecimal(result)
            } else {
                currentInput = "Error"
            }
        } catch(e: Exception) { currentInput = "Error" }
        isWaitingForNextNumber = true
        lastActionWasOperator = false
        logState("squareRoot")
        return getState()
    }

    fun percent(): EngineState {
        try {
            val result = BigDecimal(currentInput).divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)
            currentInput = formatBigDecimal(result)
        } catch(e: Exception) { currentInput = "Error" }
        isWaitingForNextNumber = true
        lastActionWasOperator = false
        logState("percent")
        return getState()
    }

    fun taxPlus(rate: Double): EngineState {
        try {
            val currentVal = BigDecimal(currentInput)
            val rateBd = BigDecimal(rate.toString()).divide(BigDecimal("100"))
            val multiplier = BigDecimal.ONE.add(rateBd)
            val result = currentVal.multiply(multiplier).setScale(0, RoundingMode.DOWN)
            currentInput = formatBigDecimal(result)
        } catch(e: Exception) { currentInput = "Error" }
        isWaitingForNextNumber = true
        lastActionWasOperator = false
        logState("taxPlus")
        return getState()
    }

    fun taxMinus(rate: Double): EngineState {
        try {
            val currentVal = BigDecimal(currentInput)
            val rateBd = BigDecimal(rate.toString()).divide(BigDecimal("100"))
            val divisor = BigDecimal.ONE.add(rateBd)
            val result = currentVal.divide(divisor, 10, RoundingMode.HALF_UP).setScale(0, RoundingMode.DOWN)
            currentInput = formatBigDecimal(result)
        } catch(e: Exception) { currentInput = "Error" }
        isWaitingForNextNumber = true
        lastActionWasOperator = false
        logState("taxMinus")
        return getState()
    }
}
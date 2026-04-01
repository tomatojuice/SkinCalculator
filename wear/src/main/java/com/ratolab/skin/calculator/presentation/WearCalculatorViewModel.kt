package com.ratolab.skin.calculator.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WearCalculatorUiState(
    val displayText: String = "0",
    val expressionText: String = ""
)

class WearCalculatorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WearCalculatorUiState())
    val uiState: StateFlow<WearCalculatorUiState> = _uiState.asStateFlow()

    private val engine = CalculatorEngine()

    fun onInputNumber(number: String) { updateStateFromResult(engine.inputNumber(number)) }
    fun onOperatorClick(operator: Int) { updateStateFromResult(engine.operatorClick(operator)) }
    fun onEqualClick() { updateStateFromResult(engine.equalClick()) }
    fun onClearC() { updateStateFromResult(engine.clearC()) }
    fun onClearAC() { updateStateFromResult(engine.clearAC()) }
    fun onBackspace() { updateStateFromResult(engine.backspace()) }
    fun onToggleSign() { updateStateFromResult(engine.toggleSign()) }
    fun onPercent() { updateStateFromResult(engine.percent()) }

    private fun updateStateFromResult(result: EngineState) {
        _uiState.update { state ->
            state.copy(
                displayText = result.displayText,
                expressionText = result.expressionText // ★追加
            )
        }
    }
}
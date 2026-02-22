package com.ratolab.skin.calculator

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.ratolab.skin.calculator.ui.theme.AppThemeType

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// ★ 変更：履歴のリスト（history）を追加！
data class CalculatorUiState(
    val displayText: String = "0",
    val currentTheme: AppThemeType = AppThemeType.INDIGO,
    val hasMemory: Boolean = false,
    val isRoundShape: Boolean = false,
    val taxRate: String = "10",
    val history: List<String> = emptyList(),
    val vibrationEnabled: Boolean = true,
    val languageCode: String = "ja"
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val engine = CalculatorEngine()
    private val dataStore = application.dataStore

    companion object {
        val THEME_KEY = stringPreferencesKey("theme_key")
        val SHAPE_KEY = booleanPreferencesKey("shape_key")
        val TAX_KEY = stringPreferencesKey("tax_key")
        val VIB_KEY = booleanPreferencesKey("vib_key")
        val LANG_KEY = stringPreferencesKey("lang_key")
    }

    init {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                val savedThemeName = preferences[THEME_KEY] ?: AppThemeType.INDIGO.name
                val savedShape = preferences[SHAPE_KEY] ?: false
                val savedTax = preferences[TAX_KEY] ?: "10"
                val savedVib = preferences[VIB_KEY] ?: true

                val theme = try {
                    AppThemeType.valueOf(savedThemeName)
                } catch (e: Exception) {
                    AppThemeType.INDIGO
                }

                _uiState.update {
                    it.copy(
                        currentTheme = theme,
                        isRoundShape = savedShape,
                        taxRate = savedTax,
                        vibrationEnabled = savedVib,
                        languageCode = preferences[LANG_KEY] ?: "ja"
                    )
                }
            }
        }
    }

    fun changeLanguage(code: String) {
        viewModelScope.launch { dataStore.edit { preferences -> preferences[LANG_KEY] = code } }
    }

    fun changeTheme(newTheme: AppThemeType) {
        viewModelScope.launch { dataStore.edit { preferences -> preferences[THEME_KEY] = newTheme.name } }
    }

    fun toggleShape(isRound: Boolean) {
        viewModelScope.launch { dataStore.edit { preferences -> preferences[SHAPE_KEY] = isRound } }
    }

    fun saveTaxRate(rate: String) {
        viewModelScope.launch { dataStore.edit { preferences -> preferences[TAX_KEY] = rate } }
    }

    // ★ 追加：履歴を全消去する処理
    fun clearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    // --- 計算機能 ---
    fun onInputNumber(number: String) { updateStateFromResult(engine.inputNumber(number)) }
    fun onOperatorClick(operator: Int) { updateStateFromResult(engine.operatorClick(operator)) }
    fun onEqualClick() { updateStateFromResult(engine.equalClick()) }
    fun onClearC() { updateStateFromResult(engine.clearC()) }
    fun onClearAC() { updateStateFromResult(engine.clearAC()) }
    fun onBackspace() { updateStateFromResult(engine.backspace()) }
    fun onMemoryPlus() { updateStateFromResult(engine.memoryPlus()) }
    fun onMemoryMinus() { updateStateFromResult(engine.memoryMinus()) }
    fun onMemoryClear() { updateStateFromResult(engine.memoryClear()) }
    fun onMemoryRecall() { updateStateFromResult(engine.memoryRecall()) }
    fun onToggleSign() { updateStateFromResult(engine.toggleSign()) }
    fun onSquareRoot() { updateStateFromResult(engine.squareRoot()) }
    fun onPercent() { updateStateFromResult(engine.percent()) }
    fun onTaxPlus() {
        val rate = _uiState.value.taxRate.toDoubleOrNull() ?: 10.0
        updateStateFromResult(engine.taxPlus(rate))
    }
    fun onTaxMinus() {
        val rate = _uiState.value.taxRate.toDoubleOrNull() ?: 10.0
        updateStateFromResult(engine.taxMinus(rate))
    }

    // ★バイブレーション切り替え用
    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[VIB_KEY] = enabled
            }
        }
    }

    // ★ 変更：エンジンから渡された履歴（newHistoryLine）があればリストに追加する
    private fun updateStateFromResult(result: EngineState) {
        _uiState.update { state ->
            val updatedHistory = if (result.newHistoryLine != null) {
                state.history + result.newHistoryLine
            } else {
                state.history
            }

            state.copy(
                displayText = result.displayText,
                hasMemory = result.hasMemory,
                history = updatedHistory
            )
        }
    }
}
package com.example.banhammercalculator.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.banhammercalculator.data.local.SharedPreferencesManager
import com.example.banhammercalculator.domain.usecase.EvaluateExpressionUseCase

/**
 * ViewModel para a tela da calculadora.
 * Gerencia o estado da UI, interage com os Use Cases e expõe dados para a View.
 */
class CalculatorViewModel(
    private val evaluateExpressionUseCase: EvaluateExpressionUseCase,
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    private val _display = MutableLiveData<String>().apply { value = "0" }
    val display: LiveData<String> = _display

    private val _isBanned = MutableLiveData<Boolean>()
    val isBanned: LiveData<Boolean> = _isBanned

    private var currentExpression: StringBuilder = StringBuilder()
    private var lastResult: String? = null
    private var lastInputWasOperator: Boolean = false
    private var hasDecimalPoint: Boolean = false

    init {
        checkBanStatus()
    }

    /**
     * Verifica o status de banimento do usuário ao inicializar o ViewModel.
     */
    private fun checkBanStatus() {
        _isBanned.value = sharedPreferencesManager.getIsBanned()
        if (_isBanned.value == true) {
            _display.value = "BANIDO!"
        }
    }

    /**
     * Adiciona um dígito à expressão atual.
     * @param digit O dígito a ser adicionado (0-9).
     */
    fun onDigitClick(digit: String) {
        if (_isBanned.value == true) return

        if (lastResult != null) {
            currentExpression.clear()
            lastResult = null
            hasDecimalPoint = false
        }

        if (currentExpression.toString() == "0" && digit != ".") {
            currentExpression.clear()
        }

        currentExpression.append(digit)
        _display.value = currentExpression.toString()
        lastInputWasOperator = false
    }

    /**
     * Adiciona um operador à expressão atual.
     * Impede múltiplos operadores consecutivos e operadores no início da expressão.
     * @param operator O operador a ser adicionado (+, -, x, /).
     */
    fun onOperatorClick(operator: String) {
        if (_isBanned.value == true) return

        if (currentExpression.isEmpty() && lastResult == null) {
            // Não permite operador no início se não h
package com.example.banhammercalculator.domain.usecase

import com.example.banhammercalculator.data.local.SharedPreferencesManager
import com.example.banhammercalculator.domain.model.CalculatorOperation
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Use Case responsável por avaliar expressões matemáticas e aplicar a lógica de banimento.
 * Ele processa a expressão, realiza o cálculo e verifica se a operação é "muito burra".
 */
class EvaluateExpressionUseCase(
    private val sharedPreferencesManager: SharedPreferencesManager
) {

    private val banHammerOperations = setOf(
        "1+1", "1x1", "1*1", "2-1", "2/1", "0+0", "0x0", "0*0", "0/1", "1-0", "1+0", "0+1", "1*0", "0*1", "1/1", "0/0" // Adicione mais operações "burras" aqui
    )

    /**
     * Avalia uma expressão matemática e retorna o resultado.
     * Se a expressão for considerada "muito burra", o usuário é banido.
     * @param expression A expressão matemática a ser avaliada (ex: "2+2").
     * @return O resultado da expressão como String, ou uma mensagem de erro.
     */
    fun execute(expression: String): String {
        if (sharedPreferencesManager.getIsBanned()) {
            return "BANIDO!" // Nunca deveria ser chamado se o banimento for verificado antes
        }

        // Normaliza a expressão para a verificação de banimento
        val normalizedExpression = expression.replace(" ", "").lowercase()

        if (banHammerOperations.contains(normalizedExpression)) {
            sharedPreferencesManager.setIsBanned(true)
            return "BANIDO!"
        }

        return try {
            val result = evaluate(expression)
            if (result.isInfinite() || result.isNaN()) {
                "Erro"
            } else {
                // Formata o resultado para evitar casas decimais desnecessárias
                val bigDecimalResult = BigDecimal(result)
                if (bigDecimalResult.stripTrailingZeros().scale() <= 0) {
                    bigDecimalResult.toBigInteger().toString()
                } else {
                    bigDecimalResult.setScale(8, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
                }
            }
        } catch (e: Exception) {
            "Erro"
        }
    }

    /**
     * Função privada para realizar a avaliação da expressão.
     * Suporta operações básicas de adição, subtração, multiplicação e divisão.
     * @param expression A expressão a ser avaliada.
     * @return O resultado da expressão como Double.
     * @throws IllegalArgumentException se a expressão for inválida.
     */
    private fun evaluate(expression: String): Double {
        // Regex para separar números e operadores
        val tokens = expression.split(Regex("(?<=[-+x/])|(?=[-+x/])")).filter { it.isNotBlank() }

        if (tokens.isEmpty()) {
            throw IllegalArgumentException("Expressão vazia ou inválida")
        }

        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<CalculatorOperation>()

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            when {
                token.matches(Regex("-?\\d+(\\.\\d+)?")) -> { // Número (pode ser negativo)
                    numbers.add(token.toDouble())
                }
                CalculatorOperation.fromSymbol(token) != null -> { // Operador
                    operators.add(CalculatorOperation.fromSymbol(token)!!)
                }
                else -> throw IllegalArgumentException("Token inválido: $token")
            }
            i++
        }

        if (numbers.size - 1 != operators.size) {
            throw IllegalArgumentException("Expressão mal formatada: número de operandos e operadores não corresponde")
        }

        // Realiza as operações de multiplicação e divisão primeiro
        var j = 0
        while (j < operators.size) {
            val op = operators[j]
            if (op == CalculatorOperation.MULTIPLY || op == CalculatorOperation.DIVIDE) {
                val num1 = numbers[j]
                val num2 = numbers[j + 1]
                val result = when (op) {
                    CalculatorOperation.MULTIPLY -> num1 * num2
                    CalculatorOperation.DIVIDE -> {
                        if (num2 == 0.0) throw ArithmeticException("Divisão por zero")
                        num1 / num2
                    }
                    else -> 0.0 // Não deve acontecer
                }
                numbers[j] = result
                numbers.removeAt(j + 1)
                operators.removeAt(j)
                j-- // Reajusta o índice devido à remoção
            }
            j++
        }

        // Realiza as operações de adição e subtração
        var result = numbers.firstOrNull() ?: 0.0
        for (k in 0 until operators.size) {
            val op = operators[k]
            val num = numbers[k + 1]
            result = when (op) {
                CalculatorOperation.ADD -> result + num
                CalculatorOperation.SUBTRACT -> result - num
                else -> result // Multiplicação e divisão já foram tratadas
            }
        }

        return result
    }
}
package com.example.banhammercalculator.domain.model

/**
 * Enumeração que representa as operações matemáticas suportadas pela calculadora.
 */
enum class CalculatorOperation(val symbol: String) {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("x"),
    DIVIDE("/");

    companion object {
        /**
         * Retorna a operação correspondente a um símbolo, ou null se não for encontrada.
         */
        fun fromSymbol(symbol: String): CalculatorOperation? {
            return values().find { it.symbol == symbol }
        }
    }
}
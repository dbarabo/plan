package ru.barabo.gui.swing.cross

import ru.barabo.db.service.StoreFilterService
import java.math.BigDecimal
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

class FormulaCalc<T: Any>(private val store: StoreFilterService<T>,
                          private val propFormula: KProperty1<T, String?>,
                          private val propCode: KProperty1<T, Comparable<*>?>,
                          private val columnsValue: List<KMutableProperty1<T, BigDecimal?>>) {

    fun calc(propColumn: KMutableProperty1<T, BigDecimal?>? = null) {
        for(row in store.elemRoot()) {

            propColumn?.let {
                getSetFormulaValue(row, it)
            } ?: run {
                for (column in columnsValue) {
                    getSetFormulaValue(row, column)
                }
            }
        }
    }

    private fun getSetFormulaValue(row: T, column: KMutableProperty1<T, BigDecimal?>): BigDecimal? {
        val value = column.get(row)

        val formula = propFormula.get(row) ?: return value

        if(formula.isEmpty()) return value

        val formulaValue = getValueOper(formula, column)
        column.set(row, formulaValue)

        return formulaValue
    }

    private fun getValueOper(formula: String, column: KMutableProperty1<T, BigDecimal?>): BigDecimal {
        var start = 0
        var result = 0.0

        var priorOper: Oper? = null

        do {
            val posStart = formula.indexOf('[', start)
            val posEnd = formula.indexOf(']', start)

            if(posEnd < 0) return BigDecimal(result)

            val code = formula.substring(posStart+1 until posEnd).trim()

            val value = valueByCode(code, column)

            val (nextOper, nextPos) = formula.getNextOper(posEnd + 1)

            start = nextPos

            result = priorOper?.let { it.oper(result, value).toDouble() } ?: value.toDouble()

            priorOper = nextOper

        } while (formula.length > start)

        return BigDecimal(result)
    }

    private fun String.getNextOper(startIndex: Int): Pair<Oper, Int> {
        var start = startIndex

        while(start < length && this[start] !in Oper.SYMBOLS) start++

        return if(start >= length) Pair(Oper.NONE, start) else Pair(Oper.bySymbol(this[start]), start)
    }

    private fun valueByCode(code: String, column: KMutableProperty1<T, BigDecimal?>): Number {
        for(row in store.elemRoot()) {
            if(propCode.get(row)?.toString() != code) continue

            return getSetFormulaValue(row, column) ?: 0
        }
        return 0
    }
}

enum class Oper(val symbol: Char, val oper: (Number?, Number?)-> Number) {
    ADD('+',  { a: Number?, b: Number? -> nvl(a).toDouble() + nvl(b).toDouble()} ),
    MINUS('-', { a: Number?, b: Number? -> nvl(a).toDouble() - nvl(b).toDouble()} ),
    NONE('_', { _, _ -> 0 } );

    companion object {
        val SYMBOLS = values().map { it.symbol }

        fun bySymbol(symbol: Char): Oper = values().first {it.symbol == symbol}
    }
}

private fun nvl(value: Number?): Number = value ?: 0
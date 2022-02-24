package ru.barabo.plan.data.entity

import ru.barabo.db.annotation.ColumnName
import ru.barabo.db.annotation.SelectQuery
import java.math.BigDecimal
import java.text.DecimalFormat
import kotlin.reflect.KMutableProperty1

@SelectQuery("{ ? = call OD.PTKB_PLAN.getPlanData(?, ?) }")
data class PlanData(

    @ColumnName("ID")
    var id: Long? = null,

    @ColumnName("NAME")
    var name: String = "",

    @ColumnName("FORMULA")
    var formula: String? = null,

    @ColumnName("XLS_MAP")
    var xlsMap: String? = null,

    @ColumnName("VALUE_M11")
    var value11: BigDecimal? = null,

    @ColumnName("VALUE_M10")
    var value10: BigDecimal? = null,

    @ColumnName("VALUE_M9")
    var value9: BigDecimal? = null,

    @ColumnName("VALUE_M8")
    var value8: BigDecimal? = null,

    @ColumnName("VALUE_M7")
    var value7: BigDecimal? = null,

    @ColumnName("VALUE_M6")
    var value6: BigDecimal? = null,

    @ColumnName("VALUE_M5")
    var value5: BigDecimal? = null,

    @ColumnName("VALUE_M4")
    var value4: BigDecimal? = null,

    @ColumnName("VALUE_M3")
    var value3: BigDecimal? = null,

    @ColumnName("VALUE_M2")
    var value2: BigDecimal? = null,

    @ColumnName("VALUE_M1")
    var value1: BigDecimal? = null,

    @ColumnName("VALUE_M0")
    var value0: BigDecimal? = null
) {
    var fmtValue0: String = ""
        get() = value0.formatNumber()

    var fmtValue1: String = ""
        get() = value1.formatNumber()

    var fmtValue2: String = ""
        get() = value2.formatNumber()

    var fmtValue3: String = ""
        get() = value3.formatNumber()

    var fmtValue4: String = ""
        get() = value4.formatNumber()

    var fmtValue5: String = ""
        get() = value5.formatNumber()

    var fmtValue6: String = ""
        get() = value6.formatNumber()

    var fmtValue7: String = ""
        get() = value7.formatNumber()

    var fmtValue8: String = ""
        get() = value8.formatNumber()

    var fmtValue9: String = ""
        get() = value9.formatNumber()

    var fmtValue10: String = ""
        get() = value10.formatNumber()

    var fmtValue11: String = ""
        get() = value11.formatNumber()
}

private val NUMBER_FORMATTER = DecimalFormat("#,###.##")

private fun BigDecimal?.formatNumber(): String = this?.let { NUMBER_FORMATTER.format(it) } ?: ""

val planDataValueList: List<KMutableProperty1<PlanData, BigDecimal?>> = listOf(
    PlanData::value0,
    PlanData::value1,
    PlanData::value2,
    PlanData::value3,
    PlanData::value4,
    PlanData::value5,
    PlanData::value6,
    PlanData::value7,
    PlanData::value8,
    PlanData::value9,
    PlanData::value10,
    PlanData::value11
)

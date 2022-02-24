package ru.barabo.plan.data.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.gui.swing.cross.CrossData
import ru.barabo.gui.swing.cross.FormulaCalc
import ru.barabo.gui.swing.cross.RowType
import ru.barabo.plan.data.entity.PlanData
import ru.barabo.plan.data.entity.planDataValueList
import ru.barabo.plan.import.SHEET_LABELS
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KMutableProperty1

private val FORMATTER_YEAR = DateTimeFormatter.ofPattern("yyyy")

var officeKey: String = SHEET_LABELS.keys.first()
    set(value) {
        officeCode = SHEET_LABELS[value] ?: officeCode

        PlanDataService.initData()
        field = value
    }

private var officeCode: String = SHEET_LABELS.values.first()

var yearDate: Timestamp = Timestamp.valueOf(dateDiffByNow(0) )
    private set

var year: String
    get() = FORMATTER_YEAR.format( yearDate.toLocalDateTime() )
    set(value) {
        yearDate = Timestamp.valueOf(LocalDate.parse("${value.trim()}-01-01").atStartOfDay())

        PlanDataService.initData()
    }

object PlanDataService : StoreFilterService<PlanData>(AfinaOrm, PlanData::class.java),
    ParamsSelect, CrossData<PlanData> {

    private lateinit var formulaCalc: FormulaCalc<PlanData>

    override fun selectParams(): Array<Any?> = arrayOf(officeCode, yearDate.addYear())

    override fun getRowCount(): Int = dataListCount()

    override fun getRowType(rowIndex: Int): RowType = when {
        dataList[rowIndex].xlsMap != null -> RowType.SIMPLE
        dataList[rowIndex].formula != null -> RowType.SUM
        else -> RowType.HEADER
    }

    override fun initData() {
        super.initData()

        if(!(::formulaCalc.isInitialized)) {
            formulaCalc = FormulaCalc(this, PlanData::formula, PlanData::id, planDataValueList)
        }
        formulaCalc.calc()
    }

    override fun setValue(value: Any?, rowIndex: Int, propColumn: KMutableProperty1<PlanData, Any?>) {
        TODO("Not yet implemented")
    }
}


var yearsNow: List<String> = listOf(

    FORMATTER_YEAR.format( dateDiffByNow(-4) ),

    FORMATTER_YEAR.format( dateDiffByNow(-3) ),

    FORMATTER_YEAR.format( dateDiffByNow(-2) ),

    FORMATTER_YEAR.format( dateDiffByNow(-1) ),

    FORMATTER_YEAR.format( dateDiffByNow(0) ),

    FORMATTER_YEAR.format( dateDiffByNow(1) )
)

private fun Timestamp.addYear(): Timestamp = Timestamp.valueOf(this.toLocalDateTime().plusYears(1) )

private fun dateDiffByNow(diffYear: Long): LocalDateTime =
    LocalDate.now().withDayOfYear(1).plusYears(diffYear).atStartOfDay()



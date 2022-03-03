package ru.barabo.plan.data.gui

import ru.barabo.gui.swing.cross.CrossColumn
import ru.barabo.gui.swing.cross.CrossColumns
import ru.barabo.gui.swing.cross.CrossTable
import ru.barabo.gui.swing.cross.CrossTableModel
import ru.barabo.plan.data.entity.PlanData
import ru.barabo.plan.data.service.PlanDataService
import ru.barabo.plan.data.service.yearDate
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.sql.Timestamp
import java.time.format.DateTimeFormatter

val yearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("на MM.yyyy")

private val columnsPlanData = arrayOf(
    CrossColumn({ "Наименование" }, PlanData::name, 50),

    CrossColumn({ formatDateAdd(yearDate, 1) } , PlanData::fmtValue11 ),

    CrossColumn({ formatDateAdd(yearDate, 2) } , PlanData::fmtValue10 ),

    CrossColumn({ formatDateAdd(yearDate, 3) } , PlanData::fmtValue9 ),

    CrossColumn({ formatDateAdd(yearDate, 4) } , PlanData::fmtValue8 ),

    CrossColumn({ formatDateAdd(yearDate, 5) } , PlanData::fmtValue7 ),

    CrossColumn({ formatDateAdd(yearDate, 6) } , PlanData::fmtValue6 ),

    CrossColumn({ formatDateAdd(yearDate, 7) } , PlanData::fmtValue5 ),

    CrossColumn({ formatDateAdd(yearDate, 8) } , PlanData::fmtValue4 ),

    CrossColumn({ formatDateAdd(yearDate, 9) }, PlanData::fmtValue3 ),

    CrossColumn({ formatDateAdd(yearDate, 10) }, PlanData::fmtValue2 ),

    CrossColumn({ formatDateAdd(yearDate, 11) }, PlanData::fmtValue1 ),

    CrossColumn({ formatDateAdd(yearDate, 12) }, PlanData::fmtValue0 )
)

private val crossPlanDataColumns = CrossColumns(1, true, columnsPlanData)

object TablePlanData : CrossTable<PlanData>( crossPlanDataColumns, PlanDataService)

fun formatDateAdd(yearDate: Timestamp, addMonth: Long = 0L): String =
    yearFormatter.format(yearDate.toLocalDateTime().plusMonths(addMonth) )


fun CrossTable<*>.copyTable() {

    val modelThis = this.model as CrossTableModel<*>

    val tableData = StringBuilder()

    tableData.append(modelThis.columnNames()).append("\n")

    for(rowIndex in 0 until modelThis.rowCount) {

        tableData.append(modelThis.rowToString(rowIndex)).append("\n")
    }

    val selection = StringSelection(tableData.toString())

    Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
}

private fun CrossTableModel<*>.columnNames(): String =
    (0 until columnCount).joinToString("\t") { getColumnName(it) }

private fun CrossTableModel<*>.rowToString(rowIndex: Int): String =
    (0 until columnCount).joinToString("\t") { getValueAt(rowIndex, it).toString() }

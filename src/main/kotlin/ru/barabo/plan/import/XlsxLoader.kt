package ru.barabo.plan.import

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import java.io.File
import java.sql.Date

private val logger = LoggerFactory.getLogger("xlsxFileLoad")

internal fun xlsxFileLoad(file: File, sections: Map<Section, List<SectionInfo>> ): Pair<String, List<ValueDB>> {

    val pathTemplate = file.inputStream()

    return pathTemplate.use { stream ->

        val workbook: XSSFWorkbook = WorkbookFactory.create(stream) as XSSFWorkbook

        workbook.use { book ->

            book.checkSheetNames()

            book.fillAllSheetData(sections)
        }
    }
}

private fun XSSFWorkbook.fillAllSheetData(sections: Map<Section, List<SectionInfo>>): Pair<String, List<ValueDB>> {

    val result = ArrayList<ValueDB>()

    var absentValues = ""

    for (sheetName in SHEET_NAMES.keys) {

        val sheet = getSheet(sheetName) ?: continue

        sections.clearAllInfo()

        val (nameSectionRowIndex, nameSectionColumnIndex) = sheet.parseSections(sections)

        val dateColumns = sheet.findDateColumns(nameSectionRowIndex, nameSectionColumnIndex + 1)
        if(dateColumns.isEmpty()) throw Exception("Не найдены столбцы дат на листе $sheetName $nameSectionRowIndex $nameSectionColumnIndex")

        sheet.parseSubSections(nameSectionColumnIndex, sections)

        val (absentValuesSheet, dataSheet) = sheet.getSectionsData(sheetName, sections, dateColumns)

        absentValues += absentValuesSheet

        result.addAll(dataSheet)
    }

    return Pair(absentValues, result)
}

private fun XSSFSheet.getSectionsData(sheetName: String, sections: Map<Section, List<SectionInfo>>,
                                      dateColumns: Map<Int, Date>): Pair<String, List<ValueDB>> {

    val office = SHEET_NAMES[sheetName]!!

    val absentValues = checkAbsentSections(sections, sheetName)

    val result = ArrayList<ValueDB>()

    for(dateColumn in dateColumns.entries) {

        result.addAll( calcValuesByDate(office, sections, dateColumn) )
    }

    return Pair(absentValues, result)
}

private fun checkAbsentSections(sections: Map<Section, List<SectionInfo>>, sheetName: String): String {

    val notFoundSections = sections.values.flatten()
        .flatMap { it.info.entries }.filter { it.value == null }.map { it.key }
        .takeIf { it.isNotEmpty() } ?: return ""

    val sectionsJoin = notFoundSections.joinToString("\n")

    return "\nНа листе '$sheetName' не найдены секции:\n$sectionsJoin"
}

private fun XSSFSheet.calcValuesByDate(office: String, sections: Map<Section, List<SectionInfo>>,
                                       dateColumn: Map.Entry<Int, Date>): List<ValueDB> {

    val result = ArrayList<ValueDB>()

    for(section in sections.values) {

        for (item in section) {

            calcValue(item.info.values, dateColumn.key)?.let {

                result += ValueDB(item.idFnBankPlanType, dateColumn.value, office, it)
            }
        }
    }

    return result
}

private fun XSSFSheet.calcValue(info: MutableCollection<Int?>, dateColumnIndex: Int): Double? {

    if(info.firstOrNull { it != null } == null) return null

    return info.filterNotNull().sumOf { getNumberValueOrZero(it, dateColumnIndex) }
}

private fun Map<Section, List<SectionInfo>>.clearAllInfo() {

    for(section in this) {
        section.key.rows.clear()

        for(sectionInfo in section.value) {
            sectionInfo.info.keys.forEach { sectionInfo.info[it] = null }
        }
    }
}

private fun XSSFSheet.findDateColumns(rowIndex: Int, startColumnIndex: Int): Map<Int, Date> {
    val row = getRow(rowIndex)

    val result = LinkedHashMap<Int, Date>()

    for(colIndex in (startColumnIndex until row.lastCellNum) ) {

        val colCell = row.getCell(colIndex)

        val dateValue = colCell?.getDateOrNull() ?: continue

        if(row.getCell(colIndex - 1)?.getStringOrNull() != null) {
            result[colIndex-1] = dateValue
        }
    }

    return result
}

private fun XSSFSheet.parseSections(sections: Map<Section, List<SectionInfo>>): Pair<Int, Int> {

    val (nameSectionRowIndex, nameSectionColumnIndex) = findHeaderSection()

    for(keySection in sections.keys) {

        keySection.rows.addAll(
            findMainSection(keySection.name, nameSectionColumnIndex, nameSectionRowIndex + 1) )
    }

    return Pair(nameSectionRowIndex, nameSectionColumnIndex)
}

private fun XSSFSheet.parseSubSections(nameSectionColumnIndex: Int, sections: Map<Section, List<SectionInfo>>) {

    for (mainSection in sections.entries) {

        if(mainSection.key.rows.isEmpty()) continue

        parseSubSections(nameSectionColumnIndex, mainSection.key.rows, mainSection.value)
    }
}

private fun XSSFSheet.parseSubSections(nameColumnIndex: Int, sectionsRows: List<Int>, subSections: List<SectionInfo>) {

    for(row in sectionsRows) {

        for(subSection in subSections) {
            findSubSections(row + 1, nameColumnIndex, subSection.info)
        }
    }
}

private fun XSSFSheet.findSubSections(rowStart: Int, nameColumnIndex: Int, subSection: MutableMap<String, Int?>) {

    var index = 0

    for (sectionItem in subSection.keys) {
        if(subSection[sectionItem] != null) continue

        subSection[sectionItem] = findSubSection(rowStart, nameColumnIndex, sectionItem, index)
        index++
    }
}

private fun XSSFSheet.findSubSection(rowStartIndex: Int, columnIndex: Int, sectionItem: String, index: Int): Int? {

    var count = 0
    for(rowIndex in (rowStartIndex..lastRowNum)) {

        if(isMainSectionType(rowIndex, columnIndex)) {
            if(index == 0 || count > 5) {
                return null
            } else {
                count++
            }
        }
        if(isEqualStringCell(sectionItem, rowIndex, columnIndex)) return rowIndex
    }

    return null
}

private fun XSSFSheet.findMainSection(sectionName: String, columnIndex: Int, rowStartIndex: Int): List<Int> {

    val findList = ArrayList<Int>()

    for(rowIndex in (rowStartIndex..lastRowNum)) {

        if( isEqualStringCell(sectionName, rowIndex, columnIndex)  &&
            isMainSectionType(rowIndex, columnIndex)  ) {

            findList += rowIndex
        }
    }
    return findList
}

private fun XSSFSheet.isMainSectionType(rowIndex: Int, columnIndex: Int): Boolean =
        isEmptyNextColumn(rowIndex, columnIndex) &&
        isEmptyAllPriorColumn(rowIndex, columnIndex)  &&
        isBoldCell(rowIndex, columnIndex)

private fun XSSFSheet.isBoldCell(rowIndex: Int, columnIndex: Int): Boolean =
    getRow(rowIndex).getCell(columnIndex).cellStyle.font.bold


private fun XSSFSheet.isEmptyAllPriorColumn(rowIndex: Int, columnIndex: Int): Boolean =
    (0 until columnIndex).firstOrNull {
        getRow(rowIndex).getCell(it)?.let { cell ->  !cell.isBlankOrEmpty() } ?: false
    } == null


private fun XSSFSheet.isEmptyNextColumn(rowIndex: Int, columnIndex: Int): Boolean =
    getRow(rowIndex).getCell(columnIndex + 1)?.isBlankOrEmpty() ?: true


private fun XSSFSheet.findHeaderSection(): Pair<Int, Int> {

    val rows = rowIterator()

    while (rows.hasNext()) {

        val row: Row = rows.next()

        val columnIndexHeaderSection = row.findHeaderSectionColumnIndex()

        if(columnIndexHeaderSection >= 0) {
            return Pair(row.rowNum, columnIndexHeaderSection)
        }
    }

    throw Exception("На листе $sheetName не найдена колонка $HEADER_SECTION_NAME")
}

private fun Row.findHeaderSectionColumnIndex(): Int {

    val columns = this.iterator()

    while (columns.hasNext()) {
        val colCell = columns.next()

        if(colCell.getStringOrNull()?.trim()?.equals(HEADER_SECTION_NAME, true) == true) {
            return colCell.columnIndex
        }
    }
    return -1
}

private fun XSSFSheet.isEqualStringCell(compareString: String, rowIndex: Int, columnIndex: Int): Boolean =
    compareString.isEqualStringCell(getRow(rowIndex).getCell(columnIndex) )

private fun String.isEqualStringCell(cellCompare: Cell?): Boolean =
    (cellCompare?.getStringOrNull()?.trim()?.equals(this, true) == true)

private fun XSSFSheet.getNumberValueOrZero(rowIndex: Int, columnIndex: Int): Double {

    return try {
        getRow(rowIndex).getCell(columnIndex)?.numericCellValue ?: 0.0
    } catch (e: java.lang.Exception) {
        0.0
    }
}

private fun XSSFWorkbook.checkSheetNames() {

    val absentSheet = SHEET_NAMES.keys.firstOrNull { (it != OPTIONAL_SHEET) &&(getSheet(it) == null) } ?: return

    throw java.lang.Exception("В книге не найден обязательный лист '$absentSheet' ")
}

private fun Cell.isBlankOrEmpty(): Boolean = when(cellType) {
    CellType.BLANK -> true
    CellType.STRING -> stringCellValue?.isBlank() ?: true
    else -> false
}

private fun Cell.getStringOrNull(): String? = if(cellType == CellType.STRING) stringCellValue else null

private fun Cell.getDateOrNull(): Date? =
    if(cellType == CellType.NUMERIC && DateUtil.isCellDateFormatted(this) ) Date(this.dateCellValue.time) else null


private const val HEADER_SECTION_NAME = "Наименование статьи"

private const val OPTIONAL_SHEET = "RUS"

private val SHEET_NAMES =
    mapOf("GOL" to "VDK", "SPS" to "SPS", "SLV" to "SLV", "NHD" to "NAH", OPTIONAL_SHEET to OPTIONAL_SHEET)
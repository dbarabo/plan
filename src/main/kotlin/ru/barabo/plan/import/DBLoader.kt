package ru.barabo.plan.import

import org.slf4j.LoggerFactory
import ru.barabo.afina.AfinaQuery
import java.io.File
import java.sql.Date
import java.time.Instant
import java.time.ZoneId
import java.util.regex.Pattern

internal data class SectionInfo(val idFnBankPlanType: Long, val info: MutableMap<String, Int?>, val prefix: Char? = null)

private fun createSectionOnfo(idFnBankPlanType: Long, info: MutableMap<String, Int?>): SectionInfo {
    if(info.entries.size != 1) return SectionInfo(idFnBankPlanType, info)

    val first = info.keys.first()

    if(first.isEmpty() || first[0] != '-') return SectionInfo(idFnBankPlanType, info)

    return SectionInfo(idFnBankPlanType, mutableMapOf<String, Int?>(first.substring(1) to null), first[0])
}

internal data class Section(val name: String, val rows: MutableList<Int> = ArrayList())

private val logger = LoggerFactory.getLogger("XlsxDBLoader")

fun processFile(file: File): String {

    val (absent, dbValues) = xlsxFileLoad(file, loadDBInfo())

    val sessionSetting = AfinaQuery.uniqueSession()

    try {
        saveValues(dbValues)

        AfinaQuery.commitFree(sessionSetting)

    } catch (e: java.lang.Exception) {

        AfinaQuery.rollbackFree(sessionSetting)

        logger.error("processFile", e)

        throw Exception(e)
    }

    return absent
}

private fun saveValues(values: List<ValueDB>) {

    val minMaxTime = getMainYear(values)

    for(value in values) {

        val params = arrayOf<Any?>(value.id, value.date, value.office, value.value)

        if(value.date.time >= minMaxTime.first && value.date.time <= minMaxTime.second) {

            AfinaQuery.execute(UPSERT_REGISTER, params)
        }
    }
}

private fun getMainYear(values: List<ValueDB>): Pair<Long, Long> {

    val mainOffice = SHEET_NAMES.entries.iterator().next().value

    val minDate = values.filter { it.office == mainOffice }
        .minByOrNull { it.date.time }?.date?.time ?: throw java.lang.Exception("minDate not found")

    val maxDate = values.filter { it.office == mainOffice }
        .maxByOrNull { it.date.time }?.date?.time ?: throw java.lang.Exception("maxDate not found")

    return Pair(minDate, maxDate)
}

internal fun loadDBInfo(): Map<Section, List<SectionInfo>> {

    val result = HashMap<Section, List<SectionInfo>>()

    val data = AfinaQuery.select(SELECT_MAP)

    var priorSectionName = ""

    var priorSection: Section? = null

    lateinit var priorSectionInfo: ArrayList<SectionInfo>

    for(row in data) {

        val id = (row[0] as Number).toLong()

        val section = (row[1] as String).substringBefore("@@").trim()

        val subSections = (row[1] as String).substringAfter("@@")
            .splitByRegexp("\\[(.*?)\\]")
            .associateWith { null as Int? }.toMutableMap()

        if(!priorSectionName.equals(section, true) ) {

            priorSection?.let { result[it] = priorSectionInfo }

            priorSectionName = section
            priorSection = Section(section)

            priorSectionInfo = ArrayList()
            priorSectionInfo += createSectionOnfo(id, subSections)

        } else {
            priorSectionInfo += createSectionOnfo(id, subSections)
        }
    }

    priorSection?.let { result[it] = priorSectionInfo }

    return result
}

private const val SELECT_MAP = """
select p.id, trim(substr(p.XLS_MAP, instr(p.XLS_MAP, '@@')+2))
from od.PTKB_FIN_BANK_PLAN_TYPE p
where trim(substr(p.XLS_MAP, instr(p.XLS_MAP, '@@')+2)) is not null
order by 2
"""

private const val UPSERT_REGISTER = "{ call od.PTKB_PLAN.upsertFinBankPlanReg(?, ?, ?, ?) }"

fun String.splitByRegexp(regExp: String): List<String> {

    val matcher = Pattern.compile(regExp).matcher(this)

    val list = ArrayList<String>()

    while(matcher.find()) {
        list += matcher.group(1).trim()
    }

    return list
}

internal data class ValueDB(val id: Long, val date: Date, val office: String, val value: Double)
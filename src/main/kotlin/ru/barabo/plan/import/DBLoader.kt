package ru.barabo.plan.import

import org.slf4j.LoggerFactory
import ru.barabo.afina.AfinaQuery
import java.io.File
import java.sql.Date
import java.util.regex.Pattern

internal data class SectionInfo(val idFnBankPlanType: Long, val info: MutableMap<String, Int?>)

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

    for(value in values) {

        val params = arrayOf<Any?>(value.id, value.date, value.office, value.value)

        AfinaQuery.execute(UPSERT_REGISTER, params)
    }
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
            priorSectionInfo += SectionInfo(id, subSections)

        } else {
            priorSectionInfo += SectionInfo(id, subSections)
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
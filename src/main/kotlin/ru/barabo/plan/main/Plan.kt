package ru.barabo.plan.main

import ru.barabo.afina.AfinaQuery
import ru.barabo.afina.VersionChecker
import ru.barabo.afina.gui.ModalConnect
import ru.barabo.gui.swing.ResourcesManager
import ru.barabo.gui.swing.processShowError
import ru.barabo.plan.construct.bank.data.DBStoreFinBankPlanType
import ru.barabo.plan.construct.bank.data.FinBankPlanTypeRow
import ru.barabo.plan.construct.bank.gui.component.ConfigBankPanel
import ru.barabo.plan.finplan.bank.data.DelegateDataFinBankPlan
import ru.barabo.plan.finplan.bank.gui.TableFinBankPlanData
import ru.barabo.plan.finplan.bank.gui.TableFinBankPlanTypes
import ru.barabo.total.db.DBStore
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import kotlin.system.exitProcess


fun main() {
    Plan()
}

class Plan : JFrame() {

    init {
        if(!ModalConnect.initConnect(this)) {
            exitProcess(0)
        }

        var isOk = false

        processShowError {
            AfinaQuery.execute(query = CHECK_WORKPLACE, params = null)

            isOk = true
        }

        if(!isOk) {
            exitProcess(0)
        }

        val store = DBStoreFinBankPlanType()

        buildGui(store)
    }

    private fun buildGui(store: DBStore<FinBankPlanTypeRow>) {

        layout = BorderLayout()

        title = title()
        iconImage = ResourcesManager.getIcon("plan")?.image

        val mainBook = ConfigBankPanel(store)

        add( mainBook, BorderLayout.CENTER)

        defaultCloseOperation = EXIT_ON_CLOSE
        isVisible = true

        pack()
        extendedState = MAXIMIZED_BOTH

        VersionChecker.runCheckVersion("PLAN.JAR", 8)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                VersionChecker.exitCheckVersion()
            }
        })
    }

    private fun buildMainBookPlan(): Component {

        val dataFinBankPlan = DelegateDataFinBankPlan()

        val tableType = TableFinBankPlanTypes( dataFinBankPlan  )

        val tableData = TableFinBankPlanData( dataFinBankPlan )
        val rightPan = JScrollPane(tableData)

        rightPan.setCorner(JScrollPane.UPPER_LEFT_CORNER, tableType.tableHeader)
        rightPan.setRowHeaderView(tableType)
        val fixedSize: Dimension = tableType.preferredScrollableViewportSize
        fixedSize.width = Toolkit.getDefaultToolkit().screenSize.width / 4
        fixedSize.height = 100000

        tableType.preferredSize = fixedSize
        tableType.maximumSize = fixedSize

        // меню

        // меню
        val menuBar = JMenuBar()

        val topToolBar = JToolBar()
        topToolBar.add(menuBar)

        val finPanel = JPanel(BorderLayout(), true)

        finPanel.add(topToolBar, BorderLayout.PAGE_START)
        finPanel.add(rightPan, BorderLayout.CENTER)

        return finPanel
        // return JTabbedPane()
    }


    private fun title(): String {
        val (userName, departmentName, workPlace, _, userId, _) = AfinaQuery.getUserDepartment()

        val user = userName ?: userId

        val db = if (AfinaQuery.isTestBaseConnect()) "TEST" else "AFINA"

        val header = "Конструктор плана всего банка"

        return "$header [$db] [$user] [$departmentName] [$workPlace]"
    }
}

private const val CHECK_WORKPLACE = "{ call od.XLS_REPORT_ALL.checkWorkplace }"
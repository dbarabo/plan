package ru.barabo.plan.data.gui

import ru.barabo.gui.swing.processShowError
import ru.barabo.gui.swing.showMessage
import ru.barabo.plan.data.service.PlanDataService
import ru.barabo.plan.import.processFile
import java.awt.BorderLayout
import java.awt.Container
import java.io.File
import javax.swing.JPanel
import javax.swing.JScrollPane

class PanelPlanData(private val mainContainer: Container) : JPanel() {

    init {
        layout = BorderLayout()

        val toolBar = ToolBarPlanData(this, mainContainer)

        add(toolBar, BorderLayout.NORTH)

        add(JScrollPane(TablePlanData), BorderLayout.CENTER)
    }

    fun showPanel() {
        PlanDataService.initData()

        val parentContainer = mainContainer.parent

        parentContainer.remove(mainContainer)
        parentContainer.add(this)

        parentContainer.invalidate()
        parentContainer.repaint()
        parentContainer.revalidate()

        this.invalidate()
        this.repaint()
    }

    fun processXlsxFile(file: File) {

        processShowError {
            val absent = processFile(file)

            if(absent.isNotEmpty()) {

                showMessage(absent)
            } else {

                showMessage(infoLoaded(file) )
            }
        }
    }

    fun infoLoaded(file: File) = "Файл ${file.name} загружен в Афину\nДанные можно увидеть выбрав кнопку 'Плановые данные...'"
}
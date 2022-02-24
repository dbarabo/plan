package ru.barabo.plan.data.gui

import org.jdesktop.swingx.JXHyperlink
import ru.barabo.gui.swing.comboBox
import ru.barabo.gui.swing.maxSpaceXConstraint
import ru.barabo.gui.swing.processShowError
import ru.barabo.plan.data.service.officeKey
import ru.barabo.plan.data.service.year
import ru.barabo.plan.data.service.yearsNow
import ru.barabo.plan.import.SHEET_LABELS
import java.awt.Container
import java.awt.GridBagLayout
import javax.swing.JToolBar

class ToolBarPlanData(private val myPanelContainer: Container, private val mainContainer: Container) : JToolBar() {

    init {

        layout = GridBagLayout()

        add(JXHyperlink().apply {
            text = " ← Назад..."

            addActionListener {

                processShowError {
                    val parentContainer = myPanelContainer.parent

                    parentContainer.remove(myPanelContainer)
                    parentContainer.add(mainContainer)

                    parentContainer.invalidate()
                    parentContainer.repaint()
                }
            }
        })

        comboBox("Год", 0, yearsNow, 1).apply {

            selectedItem = year

            addActionListener {
                year = selectedItem as String
            }

            this.maximumRowCount = this.itemCount
        }

        comboBox("Офис", 0, SHEET_LABELS.keys.toList(), 2).apply {

            selectedItem = officeKey

            addActionListener {
                officeKey = selectedItem as String
            }

            this.maximumRowCount = this.itemCount
        }

        maxSpaceXConstraint(8)
    }
}
package ru.barabo.plan.construct.bank.gui.component;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;

import ru.barabo.plan.construct.bank.data.FinBankPlanTypeRow;
import ru.barabo.plan.construct.bank.gui.table.TablePlanExecData;
import ru.barabo.total.db.DBStore;

/**
 * главная панель - на книгу добавляется
 * @author debara
 *
 */
public class ConfigBankPanel extends JPanel {
	
	final static transient private Logger logger = 
			Logger.getLogger(ConfigBankPanel.class.getName());

	public ConfigBankPanel(DBStore<FinBankPlanTypeRow> store) {
		
		setLayout(new BorderLayout());
		
		
		TablePlanExecData tableFocus = new TablePlanExecData(store);
		
		add(new TopToolBar<FinBankPlanTypeRow>(store, tableFocus), BorderLayout.NORTH);

		PanelEvents<FinBankPlanTypeRow> leftPanel = 
				new PanelEvents<FinBankPlanTypeRow>(store);
		
		JScrollPane rightPanel = new JScrollPane(tableFocus);
		
		JSplitPane splitBar = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,  rightPanel);
		
		add(splitBar, BorderLayout.CENTER);
	}
}

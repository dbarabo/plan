package ru.barabo.plan.finplan.bank.gui;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import ru.barabo.plan.finplan.bank.data.IDataFinBankPlan;


public class TableFinBankPlanData extends JTable {
	final static transient private Logger logger = Logger.getLogger(TableFinBankPlanData.class.getName());
	
	private TableCellRenderer renderer;
	
	public TableFinBankPlanData(IDataFinBankPlan data) {
		super();
		
        setModel( new TableFinBankPlanDataModel(data) );
        
        renderer = new FinPlanDataCellRenderer(data, 1, false);
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return renderer;
	}
}

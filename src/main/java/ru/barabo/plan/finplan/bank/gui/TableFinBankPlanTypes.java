package ru.barabo.plan.finplan.bank.gui;

import ru.barabo.plan.finplan.bank.data.IDataFinBankPlan;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class TableFinBankPlanTypes extends JTable {
	private TableCellRenderer renderer;
	
	public TableFinBankPlanTypes(IDataFinBankPlan data) {
		super();
        setModel( new TableFinBankPlanTypesModel(data) );
        renderer = new FinPlanTypesRenderer();
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return renderer;
	}
}

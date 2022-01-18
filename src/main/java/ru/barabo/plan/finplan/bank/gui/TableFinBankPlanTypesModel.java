package ru.barabo.plan.finplan.bank.gui;

import javax.swing.table.AbstractTableModel;

import ru.barabo.plan.finplan.bank.data.IDataFinBankPlan;

public class TableFinBankPlanTypesModel extends AbstractTableModel {
	private final IDataFinBankPlan data;
	
	public TableFinBankPlanTypesModel(IDataFinBankPlan data) {
		this.data = data;
	}
	
	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {
		return data.getFinBankPlanTypeCount();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.getFinBankPlanTypeName(rowIndex);
	}
	
	@Override
    public String getColumnName( int column ) {
	  	return data.getFinBankPlanReportYear();
	}
}

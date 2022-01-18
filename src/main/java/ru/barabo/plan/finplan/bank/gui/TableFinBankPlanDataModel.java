package ru.barabo.plan.finplan.bank.gui;

import ru.barabo.plan.finplan.bank.data.IDataFinBankPlan;

import javax.swing.table.AbstractTableModel;


public class TableFinBankPlanDataModel extends AbstractTableModel {
	private final IDataFinBankPlan data;
	
	public TableFinBankPlanDataModel(IDataFinBankPlan data) {
		this.data = data;
	}
	
	@Override
	public int getColumnCount() {
		return data.getFinBankPlanMonthCount();
	}

	@Override
	public int getRowCount() {
		return data.getFinBankPlanTypeCount();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.getFinBankPlanDataValue(rowIndex, columnIndex);
	}
	
	@Override
    public String getColumnName( int column ) {
	  	return data.getFinBankPlanMonthName(column);
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex < 12) && (
				(data.getFinBankPlanTypePart(rowIndex) == 0) ||
				(data.getFinBankPlanTypePart(rowIndex) == 3) ||
				(data.getFinBankPlanTypePart(rowIndex) == 8) ||
				(data.getFinBankPlanTypePart(rowIndex) == 7) 
		);
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	
		if("".equals(aValue)) aValue = null;
		
		Double val;
		try {
			val = (aValue != null) ? Double.valueOf((String)aValue) : null;
		} catch(java.lang.NumberFormatException e) {
			this.fireTableCellUpdated(rowIndex, columnIndex);
			return;
		}
				
		boolean res = data.updateFinBankPlanDataValue(val, rowIndex, columnIndex);
		if(!res) {
			this.fireTableCellUpdated(rowIndex, columnIndex);
		} else {
			this.fireTableDataChanged();
		}
	}
}

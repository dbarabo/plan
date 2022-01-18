package ru.barabo.plan.construct.bank.gui.table;

import java.util.List;

import javax.swing.DropMode;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

import ru.barabo.plan.construct.bank.data.FinBankPlanTypeRow;
import ru.barabo.total.db.DBStore;
import ru.barabo.total.db.ListenerStore;
import ru.barabo.total.db.StateRefresh;

public class TablePlanExecData extends JTable
			implements ListenerStore<FinBankPlanTypeRow> {
	
	final static transient private Logger logger = Logger.getLogger(TablePlanExecData.class.getName());

	private DBStore<FinBankPlanTypeRow> store;

	private RendererTablePlanExecData renderer;
	
	public TablePlanExecData(DBStore<FinBankPlanTypeRow> store) {
		super();
		this.store = store;
		
		//store.getData();
		
		setModel( new TableModelPlanExecData<FinBankPlanTypeRow>(store) );
		
		renderer = new RendererTablePlanExecData(store);
		//checkBoxEditor = new CheckBoxEditor();
		
		getSelectionModel().addListSelectionListener(
				(ListSelectionEvent e)-> {
					if (e.getValueIsAdjusting ()) return;
					
			        ListSelectionModel selModel = (ListSelectionModel)e.getSource();
			          // Номер текущей строки таблицы
			        if (!selModel.isSelectionEmpty ()) {
			        	int index = selModel.getMinSelectionIndex ();
			        	
			        	logger.debug(" e.getFirstIndex()=" +  index);
			        	
			        	List<FinBankPlanTypeRow> data = store.getData();
			        	
			        	if(index < 0 || data == null || 
								data.size() <= index) {
							return;
						}

						logger.debug(" data=" +  data);
						logger.debug(" data.size()=" +  data.size());
						
						store.setRow(data.get(index));
			         }
				});
		
		
		store.addListenerStore(this);
		
		setDragAndDrop();
		
	
		setColumnmSizes();
	}
	
	
	private void setDragAndDrop() {
		setDragEnabled(true);
    	setDropMode(DropMode.USE_SELECTION);
    	setFillsViewportHeight(true);
    	TransferHandler dnd = new TransferHandlerPlanExecData(this) {
    		// here be code to handle drops, and one would
    		// presume drag exporting, too
    	};
    	setTransferHandler(dnd);
	}

	private void setColumnmSizes() {
		
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//getTableHeader().setResizingAllowed(false);
		//setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS/*AUTO_RESIZE_SUBSEQUENT_COLUMNS*/); 
		
		for (int index = 0; index < getColumnModel().getColumnCount(); index++) {
			
			int width = store.getFields().get(index).getWidth();
			
			getColumnModel().getColumn(index).setPreferredWidth(width);
			getColumnModel().getColumn(index).setMaxWidth(width);
		}
	}

	@Override
	public void setCursor(FinBankPlanTypeRow row) {
		
		if(store.getData() == null || store.getData().size() == 0) {
			((AbstractTableModel)this.getModel()).fireTableDataChanged();
		}
		
		int index = store.getData().indexOf(store.getRow());
		
		if(index < 0) {
			index = 0;
		}
		
		this.setRowSelectionInterval(index, index);
		this.scrollRectToVisible(this.getCellRect(index, 0, true));
	}

	@Override
	public void refreshData(List<FinBankPlanTypeRow> allData, StateRefresh stateRefresh) {
		((AbstractTableModel)this.getModel()).fireTableStructureChanged();
		setColumnmSizes();
	}

	/**
	 * перенос порядка ячеек
	 * @param rowFrom
	 * @param rowTo
	 */
	public void moveOrderTo(int rowFrom, int rowTo) {
		store.moveRow(rowFrom, rowTo);
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return renderer;
	}
	
	/*
	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		return 	checkBoxEditor;	
	}*/
}

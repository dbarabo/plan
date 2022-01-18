package ru.barabo.plan.construct.bank.gui.table;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

public class TransferHandlerPlanExecData extends TransferHandler {
	
	final static transient private Logger logger = Logger.getLogger(TransferHandlerPlanExecData.class.getName());
	
	private TablePlanExecData table;
	
	private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class,
			DataFlavor.javaJVMLocalObjectMimeType, "Integer Row Index");


	public TransferHandlerPlanExecData(TablePlanExecData table) {
		this.table = table;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		assert (c == table);
		return new DataHandler(new Integer(table.getSelectedRow()), localObjectFlavor.getMimeType());
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport info) {
		boolean b = info.getComponent() == table && info.isDrop();
		
		table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
		
		return b;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY_OR_MOVE;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		
		JTable target = (JTable) info.getComponent();
		
		JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
		
		int index = dl.getRow();
		int max = table.getModel().getRowCount();
		if (index < 0 || index > max)
		   index = max;
		
		target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		
		try {
		   Integer rowFrom = (Integer) info.getTransferable().getTransferData(localObjectFlavor);
		   
		   logger.debug("rowFrom=" + rowFrom);
		   
		   logger.debug("rowTo=" + index);
		   
		   if (rowFrom != -1 && rowFrom != index) {
			   table.moveOrderTo(rowFrom, index);
		   }
		} catch (Exception e) {
		   e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void exportDone(JComponent c, Transferable t, int act) {
		if (act == TransferHandler.MOVE) {
		   table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

}

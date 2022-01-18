package ru.barabo.plan.construct.bank.gui.table;

import ru.barabo.total.db.DBStore;
import ru.barabo.total.db.impl.AbstractRowFields;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

public class RendererTablePlanExecData <E> extends JLabel
	implements TableCellRenderer {
	
	private DBStore<E> store;
	
	public RendererTablePlanExecData(DBStore<E> store) {
		this.store = store;
	}
	

	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if(isSelected) {
			setBackground(table.getSelectionBackground());
			setFont(table.getFont());
			this.setOpaque( true );
			this.setText(value == null ? "" : value.toString());
			setHorizontalAlignment(SwingConstants.LEFT);
			return this;
		}
		
		List<E> data = store.getData();
		
		if(data == null || data.size() <= row) {
			return null;
		}
		
		AbstractRowFields row_ = (AbstractRowFields)data.get(row);
		
		if(row_ == null) {
			return null;
		}
		
		HashMap attributes = new HashMap ();
				
		switch(row_.getTypePl()) {
		case 1: // гл нередакт
		case 6: // гл редакт
			setFont(table.getFont().deriveFont(table.getFont().getStyle() | Font.BOLD));
			setBackground(table.getSelectionBackground());
			setHorizontalAlignment(SwingConstants.LEFT);
			this.setOpaque( true );
			break;
			
		case 2: // подуровень нередакт
		case 8: // подуровень редакт
			this.setFont(table.getFont().deriveFont(table.getFont().getStyle() | Font.BOLD));
			attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			this.setFont(this.getFont().deriveFont(attributes));
			setBackground(Color.LIGHT_GRAY);
			this.setOpaque( true );
			setHorizontalAlignment(SwingConstants.CENTER);
			break;
			
		case 5: // нередактир-жирное - как подуровень
		case 7: // редактир-жирное - как подуровень
			this.setFont(table.getFont().deriveFont(table.getFont().getStyle() | Font.BOLD));
			setBackground(table.getBackground());
			setHorizontalAlignment(SwingConstants.CENTER);
			this.setOpaque( false );
			break;
			
		default: // 0 -просто редакт
			     // 9 -просто нередакт 
			setFont(table.getFont().deriveFont(table.getFont().getStyle()));
			setBackground(table.getBackground());
			setHorizontalAlignment(SwingConstants.RIGHT);
			this.setOpaque( false );
			break;
		}
		
		this.setText(value == null ? "" : value.toString());
		
		return this;
	}
	

}

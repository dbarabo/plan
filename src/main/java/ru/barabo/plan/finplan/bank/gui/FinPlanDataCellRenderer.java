package ru.barabo.plan.finplan.bank.gui;

import ru.barabo.plan.finplan.bank.data.IDataFinBankPlan;
import ru.barabo.plan.finplan.department.data.IDataFinPlan;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

public class FinPlanDataCellRenderer extends JLabel implements TableCellRenderer {
	  private final int lengthFrac;
	  private final Object data;
	  private final boolean isFinPlan;
		
	  public FinPlanDataCellRenderer(Object data, int lengthFrac, boolean isFinPlan) {
		this.lengthFrac = lengthFrac;
		this.data = data;
		this.isFinPlan = isFinPlan;
		
	    setOpaque(true);
	  }

	  public Component getTableCellRendererComponent(JTable table, Object value,
	                 boolean isSelected, boolean hasFocus, int row, int column) {
		  if (hasFocus) {
			  setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
			  if (table.isCellEditable(row, column)) {
				  setForeground(UIManager.getColor("Table.focusCellForeground") );
				  setBackground(UIManager.getColor("Table.focusCellBackground") );
			  }
		  } else {
			  setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		  }
		  
		  if (isSelected) {
			  this.setBackground(table.getSelectionBackground());
			  this.setForeground(table.getSelectionForeground());
		  }
		  else {
			  this.setBackground(table.getBackground());
			  this.setForeground(table.getForeground());
		  }
		  
		  setHorizontalAlignment(JLabel.RIGHT);
		  int type = (isFinPlan) ? ((IDataFinPlan)data).getFinPlanTypePart(row)
				  : ((IDataFinBankPlan)data).getFinBankPlanTypePart(row);
		  
			switch(type) {
			case 1: // гл нередакт
				setFont(table.getFont().deriveFont(table.getFont().getStyle() | Font.BOLD));
				setBackground(table.getSelectionBackground());
				this.setOpaque( true );
				break;
			case 6: // гл редакт
				setFont(table.getFont().deriveFont(table.getFont().getStyle()));
				setBackground(table.getSelectionBackground());
				this.setOpaque( true );
				break;
				
			case 2: // подуровень нередакт
				this.setFont(table.getFont().deriveFont(table.getFont().getStyle() | Font.BOLD));
				setBackground(Color.LIGHT_GRAY);
				this.setOpaque( true );
				break;	
			case 8: // подуровень редакт
				this.setFont(table.getFont().deriveFont(table.getFont().getStyle()));
				setBackground(Color.LIGHT_GRAY);
				this.setOpaque( true );
				break;

			case 4: // сумма неглавн,
				setFont(table.getFont().deriveFont(table.getFont().getStyle() | Font.BOLD | Font.ITALIC));
				setBackground(table.getBackground());
				this.setOpaque( false );
				break;
				
			case 5: // нередактир-жирное - как подуровень
				this.setFont(table.getFont().deriveFont(table.getFont().getStyle() | Font.BOLD));
				setBackground(table.getBackground());
				this.setOpaque( false );
				break;

			case 7: // редактир-жирное - как подуровень
				this.setFont(table.getFont().deriveFont(table.getFont().getStyle()));
				setBackground(table.getBackground());
				this.setOpaque( false );
				break;
				
			default: // 0 -просто редакт
				     // 9 -просто нередакт 
				setFont(table.getFont().deriveFont(table.getFont().getStyle()));
				setBackground(table.getBackground());
				this.setOpaque( false );
				break;
			}
			
		  if(column >= 12) {  // жирним немясаца тож
			  setFont(table.getFont().deriveFont(table.getFont().getStyle() | Font.BOLD));  
		  }
		  
		  if(lengthFrac != 0) {
			  setValue(value == null ? value : String.format("%." + lengthFrac + "f", value));
		  } else {
			  setValue(value);
		  }
	     
		  return this;
	  }
	    
	  protected void setValue(Object value) {
		  setText((value == null) ? "" : value.toString());
	  }
}

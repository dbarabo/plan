package ru.barabo.plan.finplan.bank.gui;

import ru.barabo.plan.correct.profit.data.FinPlanTypeValue;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class FinPlanTypesRenderer extends JLabel implements TableCellRenderer {
	
	public FinPlanTypesRenderer() {
		
	}

	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		FinPlanTypeValue val = (value == null) ? new FinPlanTypeValue() : (FinPlanTypeValue)value;
		Map<TextAttribute, Integer> attributes = new HashMap<>();
		
		  /*
		   * 1-заголовок - сумм			2-подзаголовок-сумм		4-подзаголовок-редактир	
		   * 0-простое - выр-справа		3-заголовок-редактир	5-подзаголовок 3-го уровня суммы	
		   *
		   */
		
		switch(val.getType()) {
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
			
		case 3: // сумма гл,
			setFont(table.getFont().deriveFont(table.getFont().getStyle() | Font.ITALIC));
			setBackground(table.getBackground());
			setHorizontalAlignment(SwingConstants.RIGHT);
			this.setOpaque( false );
			break;
			
		case 4: // сумма неглавн,
			setFont(table.getFont().deriveFont(table.getFont().getStyle() | Font.ITALIC));
			attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON );
			this.setFont(this.getFont().deriveFont(attributes));
			
			setBackground(table.getBackground());
			setHorizontalAlignment(SwingConstants.LEFT);
			this.setOpaque( false );
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
		
		this.setText(val.getName() == null ? "" : val.getName());
		
		return this;
	}
	
}

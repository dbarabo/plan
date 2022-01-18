package ru.barabo.plan.construct.bank.gui.component;

import org.apache.log4j.Logger;
import ru.barabo.total.db.DBStore;
import ru.barabo.total.db.FieldItem;
import ru.barabo.total.db.ListenerStore;
import ru.barabo.total.db.StateRefresh;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * панель свойств слева 
 * Информация о строке - Вид отображения всех строк 
 * Далее - поля редактирования
 * 
 * @author debara
 *
 */
public class PanelEvents <E> extends JPanel implements ListenerStore<E> {
	
	final static transient private Logger logger = Logger.getLogger(PanelEvents.class.getName());
	private DBStore<E> store;
	
	private int minWidth;
	
	private Map<Component, FieldItem> maps;

	public PanelEvents(DBStore<E> store) {
		super(new GridBagLayout());
		
		this.store = store;
		
		store.addListenerStore(this);
		
		init();
	}
	
	public int getMinWidth() {
		return minWidth;
	}
	
	private void init() {
		
		int row = 0;
		
		minWidth = 0;
		
		int width = 0;
		
		maps = new HashMap<Component, FieldItem>();
		
		for (FieldItem item : store.getFields()) {
			width = addField(item, row);
			row++;
			
			if(width > minWidth) {
				minWidth = width;
			}
		}
		
		//addButtons(row);
		setDownEmptyLine(row);
	}
	
	private int addField(FieldItem item, int row) {
				
		final GridBagConstraints gridConstLabel = new GridBagConstraints(0, 
				row, 
				1, 1, 0.0, 0.0, 
				GridBagConstraints.PAGE_START, //EAST, 
				GridBagConstraints.EAST, //*HORIZONTAL*//*NONE*/, 
				new Insets(0, 0, 0, 0), 0, 0);
		
		JLabel lab = new JLabel(item.getLabel());
		
		add(lab, gridConstLabel);

		final GridBagConstraints gridConstComp = new GridBagConstraints(1, 
				row, 
				1, // field.getColumnLength() * 2 - 1 длина
				1,
				1.0, 1.0,//(field.getRowHeight() > 1 ? 1.0 : 0.0), 
				GridBagConstraints.PAGE_START, //CENTER,
				GridBagConstraints.HORIZONTAL,//(field.getRowHeight() == 1) ? GridBagConstraints.HORIZONTAL : GridBagConstraints.BOTH/,
				new Insets(1, 1, 1, 1), 0, 0);
			
		Component comp = createComponent(item);
		add(comp, gridConstComp);
		
		//logger.debug("field.getComponent().getClass().getName():" + field.getComponent().getClass().getName());
		/*for(int rowIndex = 1; rowIndex < field.getRowHeight(); rowIndex++) {
			add(new StoreLabel(uiClient, " "), new GridBagConstraints(field.getColumn() * 2, 
					maxRow[field.getColumn()] + rowIndex, 
					1, 1, 0.0, 0.0, GridBagConstraints.PAGE_START/*EAST*///, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		//}
		
		return lab.getPreferredSize().width + comp.getPreferredSize().width;
	}
	

	private Component createComponent(FieldItem item) {
		Component comp = null;
		if(item.getListField() != null) {
			comp = new JComboBox(item.getListField());
			
			((JComboBox)comp).addItemListener(
					getItemListenerComboBoxSelected() );
			
			((JComboBox)comp).setSelectedItem(item.getValueField());
		} else {
			comp = new JFormattedTextField(item.getValueField());
			((JFormattedTextField)comp).addFocusListener(new FocusListenerTextLost() );
			if(item.isReadOnly()) {
				((JFormattedTextField)comp).setEditable(false);
			}
		}
			
		maps.put(comp, item);
		
		return comp;
	}
	
	private ItemListener getItemListenerComboBoxSelected() {
		return (ItemEvent e)-> {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				/*JComboBox*/Component comp = (Component)e.getSource();
				
				String val = (String)e.getItem(); // comp.getSelectedItem();
				
				FieldItem field = maps.get(comp);
				
				if(field == null){
					return;
				}
 				
				//logger.debug("field=" + field);
				
				//logger.debug("ComboBoxSelected val=" + val);
				
				field.setValueField(val);
		    }
		};
	}
	
	
	/**
	 * Устанавливает пустой последний элемент, чтобы он все пространство забирал
	 */
	private void setDownEmptyLine(int row ) {
		final GridBagConstraints gridConstComp = new GridBagConstraints(
				0, 
				row, 
				1,
				1, // плюс заголовок
				0, 5.0, 
				GridBagConstraints.PAGE_END,
				GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0);
		
		add(new JLabel(""), gridConstComp);
	}
	
	class FocusListenerTextLost extends FocusAdapter {
		
		@Override
		public void focusLost(FocusEvent e) {
			JFormattedTextField comp = (JFormattedTextField)e.getSource();
			
			FieldItem field = maps.get(comp);
			
			String val = comp.getText();
			
			field.setValueField(val);
		}
	}

	@Override
	public void setCursor(E row) {
				
		updateCursor();
	}

	private void updateCursor() {
		int index = 0;
		
		for (Component key : maps.keySet() ) {
			
			FieldItem oldField = maps.get(key);
			
			FieldItem newField = store.getFields().get(oldField.getIndex());
			
			//logger.debug("newField.lab=" + newField.getLabel());
			//logger.debug("newField.val=" + newField.getValueField());
			
			maps.put(key, newField);
			
			if(key instanceof JComboBox) {
				((JComboBox)key).setSelectedItem(newField.getValueField());
			} else {
				((JFormattedTextField)key).setText(newField.getValueField());
				//logger.debug("newField.key_val=" + newField.getValueField());
			}
			index++;
		}
	}

	@Override
	public void refreshData(List<E> allData, StateRefresh stateRefresh) {
		updateCursor();
	}
}

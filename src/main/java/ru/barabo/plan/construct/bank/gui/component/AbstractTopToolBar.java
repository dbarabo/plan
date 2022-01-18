package ru.barabo.plan.construct.bank.gui.component;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import ru.barabo.gui.swing.ResourcesManager;
import ru.barabo.total.db.DBStore;

public abstract class AbstractTopToolBar <E> extends JToolBar {

	protected DBStore<E> store;
	
	protected List<ButtonGroup> groups;
	
	protected JComponent focusComp;

	
	abstract protected ButtonKarkas[] getButtonKarkases();
	
	public AbstractTopToolBar(DBStore<E> store, JComponent focusComp) {
		this.store = store;
		
		this.focusComp = focusComp;
		
		setLayout(new FlowLayout(FlowLayout.LEFT) );
		
		setFloatable(true);
		
	}
	
		
	
	protected void initButton() {
		for(ButtonKarkas karkas : getButtonKarkases()) {
			if(karkas.getName() == null) {
				this.addSeparator();
			} else {
				add(createButton(karkas) );
			}
		}
	}
	
	private void addGroup(AbstractButton button, int index) {
		 
		if(groups == null) {
			groups = new ArrayList<ButtonGroup>();
		}
		
		if(groups.size() <= index) {
			groups.add(new ButtonGroup());
		}
		
		groups.get(index).add(button);
	}
	
	protected AbstractButton createButton(ButtonKarkas karkas) {
		if(karkas.getName() == null) return null;
		
		ImageIcon icon = ResourcesManager.getIcon(karkas.getIco());
		
		AbstractButton button = null;
		
		if(karkas.getGroupIndex() != null) {
			button = new JToggleButton(icon);
			addGroup(button, karkas.getGroupIndex());
		} else {
			button = new JButton(icon);
		}
		// show caption on
		button.setText(karkas.getName());
		
		button.setToolTipText(karkas.getName());
		button.addActionListener(karkas.getListener() );
		karkas.setButton(button);
		
		return button;
	}

}


package ru.barabo.plan.construct.bank.gui.component;

import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;

import ru.barabo.gui.swing.ResourcesManager;

public class ButtonKarkas {

	private String ico;
	private String name;
	private ActionListener listener;
	private Integer groupIndex;
	
	private AbstractButton button;
	
	public ButtonKarkas(String ico, String name, 
			ActionListener listener, Integer groupIndex) {
		
		this.ico = ico;
		this.name = name;
		this.listener = listener;
		this.groupIndex = groupIndex;
	}
	
	public AbstractButton getButton() {
		return button;
	}

	public void setButton(AbstractButton button) {
		this.button = button;
	}
	
	public String getIco() {
		return ico;
	}

	public ImageIcon getImageIco() {
		return getIco() == null ? null : ResourcesManager.getIcon(getIco());
	}

	public String getName() {
		return name;
	}
	
	public Integer getGroupIndex() {
		return groupIndex;
	}

	public ActionListener getListener() {
		return listener;
	}
	
	
}

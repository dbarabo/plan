package ru.barabo.plan.construct.bank.gui.component;

import ru.barabo.db.SessionException;
import ru.barabo.plan.construct.bank.data.DBStoreFinBankPlanType;
import ru.barabo.total.db.DBStore;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JToggleButton;


public class TopToolBar <E> extends AbstractTopToolBar<E> {

	public TopToolBar(DBStore<E> store, JComponent focusComp) {
		super(store, focusComp);
		initButton();
	}


	private final ButtonKarkas[] buttonKarkases = {
			
		new ButtonKarkas("viewExec", "Вид-Выполнение плана",
				(ActionEvent e)-> {store.setViewType(0);
				focusComp.requestFocus();}, 0),
				
		new ButtonKarkas("viewPlan", "Вид-План",
				(ActionEvent e)-> {store.setViewType(1);
				focusComp.requestFocus();}, 0),
				
		new ButtonKarkas("viewAll", "Вид-Все",
				(ActionEvent e)-> {store.setViewType(2);
				focusComp.requestFocus();}, 0),
		
		new ButtonKarkas(null, null, null, null),
		
			
		new ButtonKarkas("add", "Добавить строку",
					(ActionEvent e) -> {
						store.addRow();
						if (e.getSource() instanceof JToggleButton) {
							((JToggleButton) e.getSource()).setSelected(false);
						}
						focusComp.requestFocus();
					}, null),
				
		new ButtonKarkas("delete", "Удалить строку",
					(ActionEvent e) -> {
						store.removeRow();

						if (e.getSource() instanceof JToggleButton) {
							((JToggleButton) e.getSource()).setSelected(false);
						}

						focusComp.requestFocus();
					}, null),
	
		new ButtonKarkas("save", "Сохранить запись",
			(ActionEvent e)-> {store.save();
			focusComp.requestFocus();}, null) ,
			
		new ButtonKarkas(null, null, null, null),
		
		new ButtonKarkas("add_group", "Добавить Офисы",
				(ActionEvent e)-> {addOfficces(e); }, null),
				
		new ButtonKarkas("importXLS", "->Загрузить из XLS-файла...", 
				(ActionEvent e)-> {importFromExcel(e); }, null),
				
		
		new ButtonKarkas("exportXLS", "Выгрузить в XLS-файл->", 
						(ActionEvent e)-> {exportToExcel(e); }, null),		
	};
	
	/**
	 * 
	 * @param e
	 */
	private void importFromExcel(ActionEvent e) {
		((DBStoreFinBankPlanType)store).importFromExcel();
		
		focusComp.requestFocus();
	}
	
	private void exportToExcel(ActionEvent e) {
		((DBStoreFinBankPlanType)store).exportToExcel();
		
		focusComp.requestFocus();
	}
	
	
	/**
	 * добавляем доп. офисы и меняем тип у текущего
	 * @param e
	 */
	private void addOfficces(ActionEvent e) {
		try {
			((DBStoreFinBankPlanType)store).addOffices();
		} catch (SessionException ex) {
			ex.printStackTrace();
		}

		focusComp.requestFocus();
	}


	@Override
	protected ButtonKarkas[] getButtonKarkases() {
		return buttonKarkases;
	}
		
	

}

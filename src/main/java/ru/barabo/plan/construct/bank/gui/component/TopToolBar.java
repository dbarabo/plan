package ru.barabo.plan.construct.bank.gui.component;

import ru.barabo.db.SessionException;
import ru.barabo.plan.construct.bank.data.DBStoreFinBankPlanType;
import ru.barabo.plan.data.gui.PanelPlanData;
import ru.barabo.total.db.DBStore;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;


public class TopToolBar <E> extends AbstractTopToolBar<E> {

	private final PanelPlanData panelPlanData;

	public TopToolBar(DBStore<E> store, JComponent focusComp, PanelPlanData panelPlanData) {
		super(store, focusComp);

		this.panelPlanData = panelPlanData;

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

		new ButtonKarkas("importXLS", "->Загрузить План из файла...",
				this::importPlanXlsx, null),

		new ButtonKarkas("plan32", "Плановые данные...",
				this::showPlanData, null)
			/*
				
		new ButtonKarkas("importXLS", "->Загрузить из XLS-файла...", 
				(ActionEvent e)-> {importFromExcel(e); }, null),
				
		
		new ButtonKarkas("exportXLS", "Выгрузить в XLS-файл->", 
						(ActionEvent e)-> {exportToExcel(e); }, null),

		 */
	};

	private void importPlanXlsx(ActionEvent e) {


		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileFilter() {

			public String getDescription() {
				return "xlsx-файл с планом (*.xlsx)";
			}

			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					String filename = f.getName().toLowerCase();
					return filename.endsWith(".xlsx");
				}
			}
		});

		if(fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

		panelPlanData.processXlsxFile(fileChooser.getSelectedFile());
	}

	private void showPlanData(ActionEvent e) {

		panelPlanData.showPanel();
	}
	
	private void importFromExcel(ActionEvent e) {
		((DBStoreFinBankPlanType)store).importFromExcel();
		
		focusComp.requestFocus();
	}
	
	private void exportToExcel(ActionEvent e) {
		((DBStoreFinBankPlanType)store).exportToExcel();
		
		focusComp.requestFocus();
	}
	
	
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

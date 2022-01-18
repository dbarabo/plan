package ru.barabo.plan.construct.bank.xls;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.log4j.Logger;
import ru.barabo.db.SessionException;
import ru.barabo.plan.construct.bank.data.DBStoreFinBankPlanType;
import ru.barabo.plan.construct.bank.data.FinBankPlanTypeRow;
import ru.barabo.total.db.impl.AbstractDBStore;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class LoadAccounts {
	
	// порядок расположение офисов в xls-файле
	public final static int[] xlsOffices = { 0, 1, 4, 5, 6, 2, 3 }; // {0, 1, 3,
																	// 4, 5, 2};
	
	transient private static Logger logger = Logger.getLogger(LoadAccounts.class.getName());
	
	transient private static String HEADER_NOT_FOUND = "Заголовок не найден!";
	
	transient private static String STRUCT_ERROR = "Для строки из базы <%s> не найдена строка из файла <%s>";
	
	transient private static String NO_UPDATE_ACCOUNTS = "невозможно обновить данные по строке %s счета = %s";

	
	public String doImportExcelAccounts(DBStoreFinBankPlanType data) {
		
		File excelFile = openFile();
		
		if(excelFile == null) return null;
			
		return openMainFile(excelFile, data);
	}
	
	private File openFile() {

		final JFileChooser fc = new JFileChooser();
			
		int returnVal = fc.showOpenDialog(null);
			
		if (returnVal != JFileChooser.APPROVE_OPTION) return null;
			
	    return  fc.getSelectedFile();
	}
	
	private String openMainFile(File file, DBStoreFinBankPlanType data) {
		Workbook workbook = null;
		Sheet sheet = null;
		
		try {
			workbook = Workbook.getWorkbook(file);
			if(workbook == null) return null;
			sheet = workbook.getSheet(0);
		} catch (BiffException e) {
			e.printStackTrace();
			logger.error("openMainFile=" + e.getMessage());
			return e.getMessage();
					
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("openMainFile=" + e.getMessage());
			return e.getMessage();
		}
		
		return openMainFile(sheet, data);
	}
	
	private String openMainFile(Sheet sheet, DBStoreFinBankPlanType data) {
		
		StringColumn result = checkStructure(data.getData(), sheet);
		if(result.getName() != null) {
			return result.getName();
		}
		
		return fillStructure(sheet, data, result.getColumn());
	}
	
	private String fillStructure(Sheet sheet, DBStoreFinBankPlanType data, int rowHeader) {
		
		String res = null;
		
		List<FinBankPlanTypeRow> srcData = data.getData();
		
		for (int index = 0; index < srcData.size(); index++) {
			
			String srcName = srcData.get(index).getName().trim();
			
			if(AbstractDBStore.isOffice(srcName) ||
					"".equals(srcName) ) {
				
				continue;
			}
			
			StringColumn xlsName = getNextNameXls(sheet, rowHeader);
			
			rowHeader = xlsName.getColumn();
			
			if(rowHeader < 0) {
				return String.format(STRUCT_ERROR, srcName, xlsName.getName());
			}
			
			res = updateAccountsRow(index, rowHeader, sheet, data);
			if(res != null) {
				return res;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Обновляет данные
	 * @param srcIndex
	 * @param xlsRow
	 * @param sheet
	 * @param data
	 * @return
	 */
	private String  updateAccountsRow(int srcIndex, int xlsRow, Sheet sheet, 
			DBStoreFinBankPlanType data) {
		
		String res = null;
		
		Cell cell = sheet.getCell(1, xlsRow);
		final String value = cell.getContents();
		
		if(value == null || "".equals(value.trim())) return null;
		
		List<FinBankPlanTypeRow> srcData = data.getData();
		
		// не по доп офисам
		if(!AbstractDBStore.isOffice(srcData.get(srcIndex + 1).getName())) {
			res = updateMainOnly(srcData.get(srcIndex), data, value);
		} else {
			res = updateAllOffices(srcIndex + 1, data, sheet, xlsRow);
		}
		
		return res;
	}
	
	private String updateMainOnly(FinBankPlanTypeRow row, DBStoreFinBankPlanType data, String newAccounts) {

		data.updateAccount(row, newAccounts);
		return null;
	}
	
	private String updateAllOffices(int firstOffice, DBStoreFinBankPlanType data, Sheet sheet, int xlsRow) {
		
	   List<FinBankPlanTypeRow> srcData = data.getData();
	   
	   int colOffice = 0;
		
       for (int index = firstOffice; AbstractDBStore.isOffice(srcData.get(index).getName()); index++) {
			
    	   Cell cell = sheet.getCell(xlsOffices[colOffice] + 1, xlsRow);
    	   
    	   colOffice++;
   		   
    	   String value = cell.getContents();
   		   
   		   value = (value == null) ? "" : value.trim();
   		
   		    data.updateAccount(srcData.get(index), value); 
		}
       
      return null; 
	}
	
	/**
	 * проверка структуры в excel файле 
	 * @return
	 */
	private StringColumn checkStructure(List<FinBankPlanTypeRow> srcData, 
			 Sheet sheet) {
		
		int rowHeader = getHeader(sheet);
		
		final int resHeader = rowHeader;
		
		if(rowHeader < 0) {
			return new StringColumn(HEADER_NOT_FOUND, -1);
		}
		
		for (int index = 0; index < srcData.size(); index++) {
			
			String srcName = srcData.get(index).getName().trim().toLowerCase();
			
			if(srcName == null || "".equals(srcName) || 
					AbstractDBStore.isOffice(srcName) ) {
				continue;
			}
			
			StringColumn xlsName = getNextNameXls(sheet, rowHeader);
			
			rowHeader = xlsName.getColumn();
			
			if(xlsName.getName().toLowerCase().replaceAll(" ", "").indexOf(
					srcName.replaceAll(" ", "")) < 0 &&
					srcName.replaceAll(" ", "").indexOf(
							xlsName.getName().toLowerCase().replaceAll(" ", "") ) < 0 	) {
				
				return new StringColumn(String.format(STRUCT_ERROR, srcName, xlsName.getName()), -1);
			}
			
		}
		
		return new StringColumn(null, resHeader);
	}
	
	public static int getHeader(Sheet sheet) {
		
		for (int row = 0; row < sheet.getRows(); row++) {
			
			Cell cell = sheet.getCell(0, row);
			String value = cell.getContents();
			
			logger.info("XLS row=" + value);
			
			if(value == null || "".equals(value.trim())) continue;
			
			if("Наименование статей".equalsIgnoreCase(value.trim())) {
				cell = sheet.getCell(1, row + 1);
				value = cell.getContents();
				if("Головной офис".equalsIgnoreCase(value.trim())) {
					return row + 1;
				}
			}
			
		}
		
		return -1;
	}
	
	
	/**
	 * поиск в excel файле следующего после colHeader непустого значения 
	 * @param sheet
	 * @return
	 */
	private StringColumn getNextNameXls(Sheet sheet, int rowHeader) {
		
		for (int row = rowHeader + 1; row < sheet.getRows(); row++) {
			
			Cell cell = sheet.getCell(0, row);
			final String value = cell.getContents();
			
			logger.info("XLS row=" + value);
			
			if(value == null || "".equals(value.trim())) continue;
			
			return new StringColumn(value.trim(), row);
		}
		
		return new StringColumn(sheet.getCell(0, rowHeader).getContents(), -1);
	}
	
}

class StringColumn {
	private String name;
	private int column;
	
	public String getName() {
		return name;
	}

	public int getColumn() {
		return column;
	}

	public StringColumn(String name, int column) {
		
		this.name = name;
		this.column = column;
	}
	
}

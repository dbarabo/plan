package ru.barabo.plan.construct.bank.xls;

import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.*;
import jxl.write.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.barabo.gui.swing.ResourcesManager;
import ru.barabo.gui.swing.UTF8Control;
import ru.barabo.plan.construct.bank.data.DBStoreFinBankPlanType;
import ru.barabo.plan.construct.bank.data.FinBankPlanTypeRow;
import ru.barabo.total.db.impl.AbstractDBStore;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class ExportAccounts {

	transient static private final Logger logger = LoggerFactory.getLogger(ExportAccounts.class);
		
	//new File(ResourcesManager.class.getResource("/xls/accountBank.xls").toString());

	transient static private final String NAME_OUT = "СЧЕТА_ПЛАНОВ_БАНКА.xls";
	
	// buffer size used for reading and writing
    private static final int BUFFER_SIZE = 8192;
	
	public static long copy(InputStream source, OutputStream sink)
	        throws IOException
	    {
	        long nread = 0L;
	        byte[] buf = new byte[BUFFER_SIZE];
	        int n;
	        while ((n = source.read(buf)) > 0) {
	            sink.write(buf, 0, n);
	            nread += n;
	        }
	        return nread;
	    }
	
	public void export(DBStoreFinBankPlanType data) {
		
		//URL url = ResourcesManager.class.getResource("/xls/");
		
		//logger.info("url=" + url);
		
		String newFileName = getNewFilePath();
		File newFile = new File( newFileName );
		
		WritableSheet sheet = null;
		
		Sheet sheet0 = null;
		
		WritableWorkbook copy = null;
		Workbook workbook0 = null;
		
		
		URL inputUrl = ResourcesManager.getXlsPath("exportAccount");
		
		ResourceBundle xlsBundle = ResourceBundle.getBundle(
        		"properties.xls_names", new UTF8Control() );
    	
    	String path = xlsBundle.getString("exportAccount");
		
		File tempalte = null;
		
		try {
			
		   InputStream input = inputUrl.openStream();
		   
		   tempalte = new File(getHomeFilePath()  + path);
		   
	       try {
    	     		        	
	            FileOutputStream output = new FileOutputStream(tempalte);
	            try {
	                copy(input, output);
	            } finally {
	            	output.close();
	            }
	        } finally {
	        	input.close();
	        }
		   
		} catch (java.io.IOException e) {
			
		} 
		
		        

		
		//File tempalte = ResourcesManager.getXlsPath("exportAccount");
		//logger.error("tempalte=" + tempalte);
		
		try {
			workbook0 = Workbook.getWorkbook(tempalte);
			logger.error("workbook0=" + workbook0);
			
			if(workbook0 == null) return;
			copy = Workbook.createWorkbook(newFile, workbook0); //создание копии файла 4read.xls
			
			if(copy == null) {
				logger.error("ExportExtract export copy = null ");
			}
			
			sheet = (WritableSheet)copy.getSheet(0);
		} catch (BiffException e) {
			
			e.printStackTrace();
			
			logger.error("export BiffException " + e.getLocalizedMessage());
			
			JOptionPane.showMessageDialog(null, 
					e.getLocalizedMessage(),
			        null, JOptionPane.ERROR_MESSAGE );
			
		} catch (IOException e) {
			e.printStackTrace();
			
			logger.error("export IOException " + e.getLocalizedMessage());
			
			JOptionPane.showMessageDialog(null, 
					e.getLocalizedMessage(),
			        null, JOptionPane.ERROR_MESSAGE );
		}
		
		if(copy == null) return;
		
		filldata(sheet, data);
		
		workbook0.close();
		try {
			copy.write();
			copy.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("export workbook0.close IOException " + e.getLocalizedMessage());
			
		} catch (WriteException e) {
			e.printStackTrace();
			logger.error("export workbook0.close WriteException " + e.getLocalizedMessage());
		}
		
		tempalte.delete();
	
		execExcel(new File(getNewFilePath() ));
	}
	
	
	private void execExcel(File excelFile) {
		
		final int result = JOptionPane.showOptionDialog(null,
				"Excel-Файл \r\n" + excelFile.getAbsolutePath() + "\r\n сохранен в папку . Открыть файл?",
				"открыть файл", 
				JOptionPane.YES_NO_OPTION,
				JOptionPane.INFORMATION_MESSAGE,
		        null, new Object[]{"Да", "Нет"}, "Да");
		
		if (result == JOptionPane.YES_OPTION) {
			
			try {
				Desktop.getDesktop().open(excelFile);
				
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("execExcel " + e.getLocalizedMessage());
			}
		};
		
	}
	
	public static String getHomeFilePath() {
		final javax.swing.JFileChooser fr = new javax.swing.JFileChooser(); 
		final javax.swing.filechooser.FileSystemView fw = fr.getFileSystemView(); 
		String path = fw.getDefaultDirectory().getAbsolutePath() + File.separator;
		
		return path;
	
	}
	
	/**
	 * @return полное имя файла с путем
	 */
	private String getNewFilePath() {
			
		return getHomeFilePath() + NAME_OUT;
	}
	
	private void filldata(WritableSheet sheet, DBStoreFinBankPlanType data) {
		
		List<FinBankPlanTypeRow> srcData = data.getData();
		
		int row = LoadAccounts.getHeader(sheet) + 1;
		
		for (int index = 0; index < srcData.size(); index++) {
			
			String headerName = srcData.get(index).getName().trim();
			
			int column = 0;
			
			if(AbstractDBStore.isOffice(headerName) ) {
				
				column = LoadAccounts.xlsOffices[AbstractDBStore.getOfficeIndex(headerName)] + 1;	
				
				headerName = replaceAccounts(srcData.get(index).getAccounts());
				
				String percent = srcData.get(index).getAccountsPercent();
				if(percent != null && !"".equals(percent)) {
					if(headerName == null || "".equals(headerName)) {
						headerName = percent;
					} else {
						headerName += "\n" + percent;
					}
				}
				
				drawOffice(sheet, row, column, headerName);
				
			} else {
				
				row++;
				
				drawHeader(sheet, row, column, headerName);
			}
			
		}
	}
	
	private String replaceAccounts(String accounts) {
		if(accounts == null || accounts.trim().equals("")) {
			return "";
		}
		
		String res = accounts.replace("'", "");
		
		return res.replace(",", "\n");
	}
	
	private void drawOffice(WritableSheet sheet, int row, int column, String text) {
		
		WritableFont arial12ptBoldBold =
				new WritableFont(WritableFont.ARIAL, 9, WritableFont.NO_BOLD);
		
		WritableCellFormat arial12BoldBoldFormat = new WritableCellFormat(arial12ptBoldBold);
		
		
		try {
			arial12BoldBoldFormat.setAlignment(Alignment.LEFT);//выравнивание
			arial12BoldBoldFormat.setWrap(true); //перенос по словам если не помещается
			arial12BoldBoldFormat.setBackground(Colour.WHITE); //установить цвет
			arial12BoldBoldFormat.setBorder(Border.ALL, BorderLineStyle.MEDIUM); //рисуем рамку
			
			Label lab = new Label(column, row, text, arial12BoldBoldFormat);
			sheet.addCell(lab);
			
		} catch (WriteException e) {
			e.printStackTrace();
			logger.error("drawText " + e.getMessage() );
		} 

	}
	
	private void drawHeader(WritableSheet sheet, int row, int column, String text) {
		
		WritableFont arial12ptBoldBold =
				new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
		
		WritableCellFormat arial12BoldBoldFormat = new WritableCellFormat(arial12ptBoldBold);
		
		
		try {
			arial12BoldBoldFormat.setAlignment(Alignment.LEFT);//выравнивание
			arial12BoldBoldFormat.setWrap(true); //перенос по словам если не помещается
			arial12BoldBoldFormat.setBackground(Colour.WHITE); //установить цвет
			arial12BoldBoldFormat.setBorder(Border.ALL, BorderLineStyle.MEDIUM); //рисуем рамку
			
			Label lab = new Label(column, row, text, arial12BoldBoldFormat);
			sheet.addCell(lab);
			
			for(int index = 0; index < LoadAccounts.xlsOffices.length; index++) {
				lab = new Label(column + 1 + index, row, "", arial12BoldBoldFormat);
				sheet.addCell(lab);
			}
			
			
		} catch (WriteException e) {
			e.printStackTrace();
			logger.error("drawText " + e.getMessage() );
		} 

	}
}

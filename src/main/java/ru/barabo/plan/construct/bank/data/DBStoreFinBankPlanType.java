package ru.barabo.plan.construct.bank.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import ru.barabo.afina.AfinaQuery;
import ru.barabo.db.SessionException;
import ru.barabo.plan.construct.bank.xls.ExportAccounts;
import ru.barabo.plan.construct.bank.xls.LoadAccounts;
import ru.barabo.total.db.FieldItem;
import ru.barabo.total.db.impl.AbstractDBStore;

public class DBStoreFinBankPlanType extends AbstractDBStore<FinBankPlanTypeRow> {
	
	transient static private final Logger logger = Logger.getLogger(DBStoreFinBankPlanType.class.getName());
	
	final static String SEL_DATA_REAL = "select ID, NAME, TYPE_PL, ORD, SYMBOLS, ORD_REAL, "
			+ "HIDE, ACCOUNT, ACCOUNT_PERC, PERCENT, DESC_SYMBOL, FORMULA "
			+ "from od.PTKB_FIN_BANK_PLAN_TYPE "
			+ "where hide not in (2, 3, 4) order by ORD_REAL";
	
	final static String SEL_DATA_PLAN = "select ID, NAME, TYPE_PL, ORD, SYMBOLS, ORD_REAL, "
			+ "HIDE, ACCOUNT, ACCOUNT_PERC, PERCENT, DESC_SYMBOL, FORMULA from od.PTKB_FIN_BANK_PLAN_TYPE "
			+ "where hide not in (1, 3, 4) order by ORD, ORD_REAL";
	
	final static String SEL_DATA_ALL = "select ID, NAME, TYPE_PL, ORD, SYMBOLS, ORD_REAL, "
			+ "HIDE, ACCOUNT, ACCOUNT_PERC, PERCENT, DESC_SYMBOL, FORMULA from od.PTKB_FIN_BANK_PLAN_TYPE "
			+ "order by ORD_REAL, ORD";
	
	
	final static String UPD_ACCOUNTS = "update od.PTKB_FIN_BANK_PLAN_TYPE "
									+ " set ACCOUNT = od.ptkb_plan_toAccount(?), "
									+ " ACCOUNT_PERC = od.ptkb_plan_getAccountPerc(?), "
									+ " PERCENT = od.ptkb_plan_PercentfromAccount(?) "
									+ " where id = ?";
	
	final static String DEL_DATA = "delete from od.PTKB_FIN_BANK_PLAN_TYPE where id = ?";
	
	final static String UPD_DATA_FORMAT = "update od.PTKB_FIN_BANK_PLAN_TYPE set %s where id = ?";
	
	final static String UPD_DATA_ORD = "update od.PTKB_FIN_BANK_PLAN_TYPE set ord = ord + ? where ord >= ?";
	
	final static String UPD_DATA_ORDREAL = "update od.PTKB_FIN_BANK_PLAN_TYPE set ORD_REAL = ORD_REAL + ? where ORD_REAL >= ?";
	
	
	final static String INS_DATA = "insert into od.PTKB_FIN_BANK_PLAN_TYPE "
			+ "(ID, NAME, TYPE_PL, ORD, SYMBOLS, ORD_REAL, "
			+ "HIDE, ACCOUNT, ACCOUNT_PERC, PERCENT, DESC_SYMBOL, FORMULA) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	
	final static String INS_DATA_REFRESH_ALL = "insert into od.PTKB_FIN_BANK_PLAN_TYPE "
			+ "(ID, NAME, TYPE_PL, ORD, SYMBOLS, ORD_REAL, "
			+ "HIDE, ACCOUNT, ACCOUNT_PERC, PERCENT, DESC_SYMBOL, FORMULA) "
			+ "VALUES (od.PTKB_PLAN_TYPEBANK.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	
	final static String UPD_TYPE_PL = "update od.PTKB_FIN_BANK_PLAN_TYPE set TYPE_PL = ? where id = ?";
	
	final static String SEL_NEXT_SEQ = "select od.PTKB_PLAN_TYPEBANK.nextval from dual";
	
	
	final static String UPD_ORD_1 = "update od.PTKB_FIN_BANK_PLAN_TYPE set ORD = ORD + ? where ORD >= ?";
	final static String UPD_ORD_2 = "update od.PTKB_FIN_BANK_PLAN_TYPE set ORD = ? + ORD - ? - ? where ORD >= ? and ORD < ?";
	 
	
	final static String UPD_ORDREAL_1 = "update od.PTKB_FIN_BANK_PLAN_TYPE set ORD_REAL = ORD_REAL + ? where ORD_REAL >= ?";
	final static String UPD_ORDREAL_2 = "update od.PTKB_FIN_BANK_PLAN_TYPE set ORD_REAL = ? + ORD_REAL - ? - ? where ORD_REAL >= ? and ORD_REAL < ?";

	final static String UPD_ORD_1_MORE = "update od.PTKB_FIN_BANK_PLAN_TYPE set ORD = ORD - ? where ORD <= ?";
	//ord = ord - count where ord <= TO
	final static String UPD_ORD_2_MORE = "update od.PTKB_FIN_BANK_PLAN_TYPE set ORD = ? + ORD - ? + ? where ORD >= ? and ORD < ?";
	//ord = TO + ord - From + count where ord >= From-COUNT and ord < From
	
	final static String UPD_ORDREAL_1_MORE = "update od.PTKB_FIN_BANK_PLAN_TYPE set ORD_REAL = ORD_REAL - ? where ORD_REAL <= ?";
	//ord = ord - count where ord <= TO
	final static String UPD_ORDREAL_2_MORE = "update od.PTKB_FIN_BANK_PLAN_TYPE set ORD_REAL = ? + ORD_REAL - ? + ? where ORD_REAL >= ? and ORD_REAL < ?";
	//ord = TO + ord - From + count where ord >= From-COUNT and ord < From
	
	
	//private Data owner;
	
	private TypeSelect typeSelect;
	
	
	
	public int getTypeSelect() {
		return typeSelect.ordinal();
	}

	public DBStoreFinBankPlanType() {
		//this.owner = owner;
		typeSelect  = TypeSelect.values()[0];
	}
	
	@Override
	public void setViewType(int index) {
		typeSelect = TypeSelect.values()[index];
		
		setMustUpdate();
		getData();
	}

	@Override
	public void moveRow(int rowFrom, int rowTo) {
		logger.debug("rowFrom=" + rowFrom);
		logger.debug("rowTo=" + rowTo);
		FinBankPlanTypeRow from = getData().get(rowFrom);
		
		FinBankPlanTypeRow to = getData().get(rowTo);

		if(isOffice(to) ) {
			return;
		}
		
		int countFrom = headerOfficeCount(from);

		try {
			if(updateOrder(from, to, countFrom) ){
				setViewType( getTypeSelect() );
			}
		} catch (SessionException e) {
			e.printStackTrace();
		}
	}
	
	private boolean updateOrder(FinBankPlanTypeRow from, FinBankPlanTypeRow to, int count) throws SessionException {
		if(typeSelect == TypeSelect.All) {
			return false;
		}
		
		if(typeSelect == TypeSelect.Plan) {
			if(to.getOrd().intValue() < from.getOrd().intValue()) {

				AfinaQuery.INSTANCE.execute(UPD_ORD_1,
						new Object[] {count, to.getOrd()});

				AfinaQuery.INSTANCE.execute(UPD_ORD_2,
						new Object[] {to.getOrd(), from.getOrd(),  count,
						  from.getOrd().intValue() + count, from.getOrd().intValue() + 2*count}  );
			} else {
				int countTo = headerOfficeCount(to) - 1;

				AfinaQuery.INSTANCE.execute(UPD_ORD_1_MORE,
						new Object[] {count, to.getOrd().intValue() + countTo}  );

				AfinaQuery.INSTANCE.execute(UPD_ORD_2_MORE,
						new Object[] {to.getOrd().intValue() + countTo, from.getOrd(),  count,
						  from.getOrd().intValue() - count, from.getOrd().intValue()}  );
			}
		} else {
			
			if(to.getOrdReal().intValue() < from.getOrdReal().intValue()) {

				AfinaQuery.INSTANCE.execute(UPD_ORDREAL_1,
					new Object[] {count, to.getOrdReal()}  );

				AfinaQuery.INSTANCE.execute(UPD_ORDREAL_2,
					new Object[] {to.getOrdReal(), from.getOrdReal(), count, 
						from.getOrdReal().intValue() + count, from.getOrdReal().intValue() + 2*count}  );
			} else {
				int countTo = headerOfficeCount(to) - 1;

				AfinaQuery.INSTANCE.execute(UPD_ORDREAL_1_MORE,
						new Object[] {count, to.getOrdReal().intValue() + countTo}  );

				AfinaQuery.INSTANCE.execute(UPD_ORDREAL_2_MORE,
						new Object[] {to.getOrdReal().intValue() + countTo, from.getOrdReal(),  count,
						  from.getOrdReal().intValue() - count, from.getOrdReal().intValue()}  );
			}

		}
		
		return true;
	}
	
	/**
	 * @param row
	 * @return
	 */
	private int headerOfficeCount(FinBankPlanTypeRow row) {
		 if(row.getTypePl() == 0) return 1;
		 
		 List<FinBankPlanTypeRow> data = getData();
		 final int indexFirst = data.indexOf(row);
		 int index = indexFirst + 1;
		 for(; index < data.size(); index++) {
			 if(!isOffice(data.get(index) ) ) {
				  break;
			 }
		 }
		 
		 return index - indexFirst;
	}
	
	@Override
	protected List<FinBankPlanTypeRow> initData() {
		
		List<Object[]> datas = AfinaQuery.INSTANCE.select(typeSelect.getSelect(), null );
		
		List<FinBankPlanTypeRow> data = new ArrayList<FinBankPlanTypeRow>();
		
		for (Object[] row : datas) {
			data.add(FinBankPlanTypeRow.create(row) );
		}
		
		return data;
	}
	
	/**
	 * ��������� �������� hide, ord ��� ordReal, typePL
	 * @param newRow
	 * @param cursor
	 */
	private void setDefaultRowValue(FinBankPlanTypeRow newRow, FinBankPlanTypeRow cursor) {
		
		newRow.setTypePl(0);
		
		newRow.setHide(0);
		
		int ordReal = 0;
		int ord = 0;
		
		if(cursor != null) {
			ordReal = cursor.getOrdReal() == null ? 0 : cursor.getOrdReal().intValue();
			ord = cursor.getOrd() == null ? 0 : cursor.getOrd().intValue();
		}
		
		switch (typeSelect) {
		case Real:
			newRow.setOrdReal(ordReal + 1);
			break;
			
		case Plan:
			newRow.setOrd(ord + 1);
			break;
			
		case All:
			newRow.setOrdReal(ordReal + 1);
			newRow.setOrd(ord + 1);
			break;
			
		default:
			break;
		}
	}
	
	private void updateTypePl(Number id, int typePl) throws SessionException {
		AfinaQuery.INSTANCE.execute(UPD_TYPE_PL,
				new Object[] {typePl, id}  );
	}
	
	private void updateTypePl(FinBankPlanTypeRow cursor) throws SessionException {
		switch (typeSelect) {
		case Real:
			if(cursor.getTypePl() != 5) {
				updateTypePl(cursor.getId(), 5);
			}
			
			updateOrdReal(cursor.getOrdReal() == null 
					      ? 1 : cursor.getOrdReal().intValue() + 1,
						OFFICES.length);
			break;
			
		case Plan:
			updateOrd(cursor.getOrd() == null 
		      ? 1 : cursor.getOrd().intValue() + 1,
			OFFICES.length);
			
			// ��� BREAK!!!
			
		case All:
			if(cursor.getTypePl() != 6) {
				updateTypePl(cursor.getId(), 6);
			}
			break;
			
		default:
			break;
		}
	}
	
	public void addOffices() throws SessionException {
		
		FinBankPlanTypeRow cursor = getRow();
		if(cursor == null || cursor.getId() == null) {
			return;
		}
		
		updateTypePl(cursor);
	  
	  
	 
		Number ord = cursor.getOrd();
		Number ordReal = cursor.getOrdReal();
		
		int hide = 0;
		
		/*
		if(ord == null) {
			hide = 1; // ������ ����
		} else if(ordReal == null) {
			hide = 2; // ������ ����
		}
		*/
		boolean isCommit = false;
		
		for (String office : OFFICES) {
			if(ord != null) {
				ord = ord.intValue() + 1;
			}
			
			if(ordReal != null) {
				ordReal = ordReal.intValue() + 1;
			}
			
			isCommit = office.equals(OFFICES[OFFICES.length - 1] );

			AfinaQuery.INSTANCE.execute(INS_DATA_REFRESH_ALL,
					new Object[] {office, 0, ord, null, ordReal, hide, null, null, null, null, null} );
		}
		
		setViewType( getTypeSelect() );
	}

	@Override
	protected FinBankPlanTypeRow createEmptyRow() {
		
		FinBankPlanTypeRow row = new FinBankPlanTypeRow();
		
		FinBankPlanTypeRow cursor = getRow();
		
		//logger.debug("cursor=" + cursor);
		//logger.debug("typeSelect=" + typeSelect);
				
		setDefaultRowValue(row, cursor);
		
		return row;
	}

	@Override
	protected FinBankPlanTypeRow cloneRow(FinBankPlanTypeRow row) {
		FinBankPlanTypeRow newRow = new FinBankPlanTypeRow();
		
		row.cloneTo(newRow);
		
		return newRow;
	}
	
	private void updateOrdReal(Number ordReal, int step) throws SessionException {
		AfinaQuery.INSTANCE.execute(UPD_DATA_ORDREAL,
				new Object[] {step, ordReal}  );
	}
	
	private void updateOrd(Number ord, int step) throws SessionException {
		AfinaQuery.INSTANCE.execute(UPD_DATA_ORD,
				new Object[] {step, ord} );
	}

	@Override
	protected void insertRow(FinBankPlanTypeRow row) {
		
		List<Object[]> datas = AfinaQuery.INSTANCE.select(SEL_NEXT_SEQ, null);
		row.setId((Number)datas.get(0)[0]);
	try {
		switch (typeSelect) {
		case Real:
			updateOrdReal(row.getOrdReal(), 1);
			break;
			
		case Plan:
			updateOrd(row.getOrd(), 1 );
			break;
			
		case All:
			updateOrdReal(row.getOrdReal(), 1);
			updateOrd(row.getOrd(), 1 );
			break;
			
		default:
			break;
		}

		AfinaQuery.INSTANCE.execute(INS_DATA, row.getFields()  );
	} catch (SessionException e) {
		e.printStackTrace();
	}

	}
	
	@Override
	protected void updateRow(FinBankPlanTypeRow oldData,
			FinBankPlanTypeRow newData) {

		Vector<Object> updateData = new Vector<Object>();
		
		String updated = newData.getUpdateData(oldData, updateData);
		
		if(updated == null) return;

		try {
			AfinaQuery.INSTANCE.execute(String.format(UPD_DATA_FORMAT, updated), updateData.toArray()  );
		} catch (SessionException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void remove(FinBankPlanTypeRow row) {
		try {
			AfinaQuery.INSTANCE.execute(DEL_DATA, new Object[]{row.getId()} );
		} catch (SessionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<FieldItem> getFields() {
		FinBankPlanTypeRow cursor = getRow();
		
		if(cursor == null) {
			cursor = new FinBankPlanTypeRow();//createEmptyRow();
		}
		
		return cursor.fieldItems();
	}
	
	public void updateAccount(FinBankPlanTypeRow newData, String newAccount) {

		try {
			AfinaQuery.INSTANCE.execute(UPD_ACCOUNTS, new Object[]{newAccount, newAccount, newAccount, newData.getId()} );
		} catch (SessionException e) {
			e.printStackTrace();
		}
	}
	
	public void importFromExcel() {
		LoadAccounts loadAccounts = new LoadAccounts();
		
		String error = loadAccounts.doImportExcelAccounts(this);
		
		if(error != null) {
			JOptionPane.showMessageDialog(null,
					error, null, JOptionPane.ERROR_MESSAGE, null); 
		}
	}
	
	public void exportToExcel() {
		ExportAccounts exportAccounts = new ExportAccounts();
		
		exportAccounts.export(this);
	}

}

enum TypeSelect {
	Real(DBStoreFinBankPlanType.SEL_DATA_REAL),
	Plan(DBStoreFinBankPlanType.SEL_DATA_PLAN),
	All(DBStoreFinBankPlanType.SEL_DATA_ALL);
	
	String select;
	
	TypeSelect(String sel) {
		select = sel;
	}
	
	public String getSelect() {
		return select;
	}
}

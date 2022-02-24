package ru.barabo.plan.construct.bank.data;


import org.apache.log4j.Logger;
import ru.barabo.total.db.FieldItem;
import ru.barabo.total.db.Type;
import ru.barabo.total.db.impl.AbstractRowFields;
import ru.barabo.total.db.impl.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * @author debara
 *
 * entity таблицы PTKB_FIN_BANK_PLAN_TYPE
 */
public class FinBankPlanTypeRow extends AbstractRowFields {
	
	final static transient private Logger logger = Logger.getLogger(FinBankPlanTypeRow.class.getName());
	
	@Override
	protected List<FieldItem> createFields() {
		List<FieldItem> fields = new ArrayList<FieldItem>();
		
		fields.add(new Field("#id", true, Type.LONG, null, "ID", 50, 0, true)); // id
		fields.add(new Field("Наименование", true, Type.STRING, null, "NAME", 400, 1, false)); // name
		fields.add(new Field("Тип строки", true, Type.LONG,
				
				new String[]{"1-Заголовок неред",     
				"1-Заголовок редак", 
				"2-ПодЗаголовок неред",    
				"2-ПодЗаголовок редак",   
				"3-ПодЗаголовок неред", 
				"3-ПодЗаголовок ред",
				"4-обычный редак", 
				"4-обычный неред"}, "TYPE_PL", 100, 2, false,
				
				new Integer[]{1, 6, 2, 8, 5, 7, 0, 9} ) ); //typePl
		
		fields.add(new Field("План сорт", true, Type.LONG, null, "ORD", 50, 3, false)); // ord
		fields.add(new Field("Символы", true, Type.STRING, null, "SYMBOLS", 100, 4, false)); // symbols
		fields.add(new Field("Реал сорт", true, Type.LONG, null, "ORD_REAL", 40, 5, true)); // ordReal
		fields.add(new Field("План/Реал", true, Type.LONG,
				new String[]{"Все", "План", "Реал", "Нигде"}, "HIDE", 40, 6, false,
				new Integer[]{0, 2, 1, 4} )); //hide
		
		fields.add(new Field("Счета", true, Type.STRING, null, "ACCOUNT", 150, 7, false)); // account
		fields.add(new Field("Счета от %%", true, Type.STRING, null, "ACCOUNT_PERC", 100, 8, false)); // accountPerc
		fields.add(new Field("Часть %%", true, Type.DECIMAL, null, "PERCENT", 40, 9, false)); // percent
		fields.add(new Field("Вид символ.", true, Type.STRING, null, "DESC_SYMBOL", 80, 10, false)); // descSymbol
		fields.add(new Field("Формула", true, Type.STRING, null, "FORMULA", 150, 11, false)); // formula
		
		return fields;
	}
	
	static public FinBankPlanTypeRow create(Object[] row) {
		FinBankPlanTypeRow finBankRow = new FinBankPlanTypeRow();
		
		for(int index = 0; index < finBankRow.fieldItems().size(); index++) {
			finBankRow.fieldItems().get(index).setValueFieldObject(row[index] );
		}

		return finBankRow;
	}
	

	protected Number getOrdReal() {
		return (Number)fieldItems().get(5).getVal();
	}
	
	protected void setOrdReal(Number ordReal) {
		fieldItems().get(5).setValueFieldObject(ordReal);
	}
	
	protected Number getHide() {
		Number val = (Number)fieldItems().get(6).getVal();
		
		return val == null ? -1 : val.intValue();
	}
	
	protected void setHide(int hide) {
		fieldItems().get(6).setValueFieldObject(hide);
	}
}


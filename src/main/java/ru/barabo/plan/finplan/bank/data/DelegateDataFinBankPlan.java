package ru.barabo.plan.finplan.bank.data;

import org.slf4j.Logger;
import ru.barabo.afina.AfinaQuery;
import ru.barabo.plan.construct.bank.data.DBStoreFinBankPlanType;
import ru.barabo.plan.finplan.department.data.AbstractDataFinPlan;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DelegateDataFinBankPlan extends AbstractDataFinPlan implements IDataFinBankPlan {

	static private final String SEL_TYPES = "select id, name, TYPE_PL, formula from od.PTKB_FIN_BANK_PLAN_TYPE where hide not in (1, 3, 4) order by ord";
	static private final String SEL_REGS = "select MONTH_FIN, TYPE_FIN, VALUE from od.PTKB_FIN_BANK_PLAN_REG FR" +
			" where trunc(MONTH_FIN, 'YYYY') = trunc(?, 'YYYY') order by MONTH_FIN, TYPE_FIN";
	
	static private final String INS_REGS = "insert into od.PTKB_FIN_BANK_PLAN_REG (MONTH_FIN, TYPE_FIN, VALUE) values (?, ?, ?)";
	static private final String UPD_REGS = "update od.PTKB_FIN_BANK_PLAN_REG set VALUE = ? where MONTH_FIN = ? and  TYPE_FIN = ?";
	
	static private final List<String> stekErrorList = new ArrayList<>();

	public DelegateDataFinBankPlan() {

		super();
	}
	

	/**
	 * определяет нужно ли вычислять формулу в ячейке или нет
	 */
	private boolean isCalcFormul(int row, int month) {
		if(registers[month][row] != null) {
			return false;
		}
		
		String formulParam = (String)types.get(row)[3];
		if(formulParam == null) {
			return false;
		}
		
		formulParam = formulParam.trim();
		
		if("".equals(formulParam)) {
			return false;
		}
		
		Number type = (Number)types.get(row)[2];

		return !(type == null ||
		   type.intValue() == 6 ||
		   type.intValue() == 8 ||
		   type.intValue() == 7 ||
		   type.intValue() == 0);
	}
	
	/**
	 * Вычисление формулы
	 */
	private Number calcFormula(int month, String formula) {

		if (formula == null || "".equals(formula)) {
			return null;
		}

		if (stekErrorList.contains(formula)) {
			return null;
		}

		String select = "select " + formula.replaceAll("\\[\\d(\\d*)]", " ?") + " from dual";
		// logger.debug("RegExp Select=" + select);
		
		Pattern p = Pattern.compile("([^\\[]+)(?=\\][^\\]]*)" ); 
		
		Matcher matcher = p.matcher(formula);  
		
		List<Number> paramId = new ArrayList<Number>();
        	       
		while (matcher.find()) {

			int id = Integer.parseInt(matcher.group().trim());
			
			int row = getOrderTypeById(id);
			
			if(row < 0 ){
				if(month == 0) {
				JOptionPane.showMessageDialog(null, 
				        "В формуле " + formula + 
				        "\nесть несуществующая строка с id=" + id,
				        null, JOptionPane.ERROR_MESSAGE );
				}
				return null;
			}
			
			if( isCalcFormul(row, month) ) {
				registers[month][row] = calcFormula(month, (String)types.get(row)[3]);
			}

			paramId.add(nvl(registers[month][row]));
		}
		
		List<Object[]> values = AfinaQuery.INSTANCE.select(select, paramId.toArray() );

		if (values.size() == 0 || values.get(0).length == 0) {

			stekErrorList.add(formula);

			JOptionPane.showMessageDialog(null,
					"В формуле " + formula +
							"\nДопущена ошибка, невозможно вычислить",
					null, JOptionPane.ERROR_MESSAGE);

			return null;
		}

		return (Number)values.get(0)[0];
	}
	
	
	private ValueStep calcDopOfficces(int month, int firstDopOfficce) {
		
		double result = 0;
		int step = 0;
		
		for(int row = firstDopOfficce; row < types.size(); row++) {
			if(DBStoreFinBankPlanType.isOffice((String)types.get(row)[1])) {
				result += nvl(registers[month][row]);
				step++;
			} else {
				break;
			}
		}
		
		return new ValueStep(step, result);
	}
	
	/**
	 * вычиление сумм для заголовка доп. офиссов
	 */
	private void computeDopOffice(int month) {
		
		for(int row = 0; row < types.size(); row++) {
			String formul = (String)types.get(row)[3];
						
			if(formul != null && (!"".equals(formul.trim()) ) )  {
				continue;
			}
			
			Number type = (Number)types.get(row)[2];
			
			// если можно редактировать
			if(type == null ||
					type.intValue() == 6 ||
					type.intValue() == 8 || 
					type.intValue() == 7 ||
					type.intValue() == 0) {
				
				continue;
			}
			
			if(formul != null && (!"".equals(formul.trim() ) ) )  {
				continue; //registers[month][row] = null;
			}
			
			// следующий должен быть доп. офисом
			if(row + 1 == types.size()) {
				continue; 
			}
			if(!DBStoreFinBankPlanType.isOffice((String)types.get(row + 1)[1])) {
				continue; 
			}
			
			final ValueStep stv = calcDopOfficces(month, row+1);
			
			registers[month][row] = stv.getValue();
			row += stv.getStep();
		}
	}
	
	/**
	 * вычисляет поля месяца - для вычисляемых полей  
	 */
	protected void computeMonth(int month) {
		
		computeDopOffice(month);
		
		for(int row = 0; row < types.size(); row++) {
			String formul = (String)types.get(row)[3];
			
			Number type = (Number)types.get(row)[2];

			if(type != null && (type.intValue() == 6 ||
					type.intValue() == 8 || 
					type.intValue() == 7 ||
					type.intValue() == 0)) {
				
				continue;
			}
			
			if(formul != null && (!"".equals(formul.trim() ) ) )  {
				registers[month][row] = null;
			}
		}
		
		for(int row = 0; row < types.size(); row++) {
			String formul = (String)types.get(row)[3] ;
			
			Number type = (Number)types.get(row)[2];

			if(type != null && (type.intValue() == 6 ||
					type.intValue() == 8 || 
					type.intValue() == 7 ||
					type.intValue() == 0)) {
				
				continue;
			}
			
			if(formul != null && (!"".equals(formul.trim() ) ) )  {

				registers[month][row] = calcFormula(month, formul.trim());
			}
		}
	}

	@Override
	protected String getInsertRegs() {
		return INS_REGS;
	}

	@Override
	protected String getSelectRegs() {
		return SEL_REGS;
	}

	@Override
	protected String getSelectTypes() {
		return SEL_TYPES;
	}

	@Override
	protected String getUpdateRegs() {
		return UPD_REGS;
	}
	
	
	public void actionBeforeClose() {
		for (int month = 0; month <= 12; month++) {
			super.actionBeforeClose(209, month, month == 11);
		}
	}
	
}

class ValueStep {
	private final int step;

	private final double value;
	

	public ValueStep(int step, double value) {
		this.step = step;
		this.value = value;
	}
	
	public int getStep() {
		return step;
	}
	
	public double getValue() {
		return value;
	}

}

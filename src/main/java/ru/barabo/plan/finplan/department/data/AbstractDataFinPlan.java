package ru.barabo.plan.finplan.department.data;

import org.apache.log4j.Logger;
import ru.barabo.afina.AfinaQuery;
import ru.barabo.db.SessionException;
import ru.barabo.plan.correct.profit.data.FinPlanTypeValue;
import ru.barabo.plan.finplan.bank.data.IDataFinBankPlan;
import ru.barabo.total.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.*;


public abstract class AbstractDataFinPlan implements IDataFinBankPlan /*IDataFinPlan*/ {

	transient static private final Logger logger = Logger.getLogger(AbstractDataFinPlan.class.getName());

	protected List<Object[]> types;
	protected Number[][] registers;
	
	public AbstractDataFinPlan() {

		fillData();
	}
	
	abstract protected String getSelectTypes();
	abstract protected String getSelectRegs();
	abstract protected String getInsertRegs();
	abstract protected String getUpdateRegs();
	
	/**
	 * вычисляет поля месяца
	 */
	abstract protected void computeMonth(int month);
	
	
	protected void fillData() {
		types = AfinaQuery.INSTANCE.select(getSelectTypes(), null);

		if(types.size() == 0) return;
		
		registers = new Number[getFinBankPlanMonthCount()][types.size()];
		
		Vector<Object[]> tmpReg = new Vector<Object[]>();

		AfinaQuery.INSTANCE.select(getSelectRegs(), new Object[]{new java.sql.Date( reportDate().getTimeInMillis())} );

		fillRegisters(registers, tmpReg);
				
		computeField();
	}
	
	/**
	 * вычисляет все поля
	 */
	protected void computeField() {
	
		for (int month = 0; month < (getFinBankPlanMonthCount() - 1); month++) {
			computeMonth(month);
		}
		for (int row = 0; row < types.size(); row++) {
			computeTotalRow(row);
		}
	}
	
	protected static double nvl(Object value) {
		return (value == null) ? 0 : ((Number)value).doubleValue();
	}
	
	protected void computeTotalRow(int row) {
		// if(this instanceof DelegateDataRestBalancePlan) return;

		double value = 0;
		for (int month = 0; month < (getFinBankPlanMonthCount() - 1); month++) {
			value += nvl(registers[month][row]);
		}
		registers[(getFinBankPlanMonthCount() - 1)][row] = value;
	}
	

	protected String getNameMoreMonth(int index) {
		switch(index) {
			case 12: 
				return "Итого " + getFinBankPlanReportYear();
			default:
				return null;
		}
	}
	
	/**
	 * @param idType
	 * @return номер в массиве types по Idtype
	 */
	protected int getOrderTypeById(int idType) {
		//logger.debug("idType :" + idType);
		for(int index = 0; index < types.size(); index++) {
			if((types.get(index)[0] != null) && (((Number)types.get(index)[0]).intValue() == idType) ) {
				return index;
			}
		}
		return -1; // не нашли 
	}
	
	protected int getMonthIndex(java.util.Date date) {
		return DateUtils.getMonthIndex(date);
	}
	
	private void fillRegisters(Object[][] registers, Vector<Object[]> tmpReg) {
		int monthId = -1;
		for (Object[] row : tmpReg) {
			monthId = getMonthIndex((java.util.Date)row[0]);
			if(row[1] != null) {
				final int typeOrder = getOrderTypeById( ((Number)row[1]).intValue() );
				if(typeOrder != -1) {
					registers[monthId][typeOrder] = row[2];
				}
			}
		}
	}
	
	static public double sumValue(Number[] reg, int start, int end) {
		double value = 0;
		
		for(int index = start;  index <= end; index++) {
			value += nvl(reg[index]);
		}
		
		return value;
	}
	
	protected double sumValueByIdType(int[] idTypeValues, Number[] reg) {
		double value = 0;
		
		for(int idType : idTypeValues) {
			int order = getOrderTypeById(idType);
			if(order >= 0) {
				value +=  nvl(reg[order]);
			}
		}
		
		return value;
	}
	
	protected Number getRegisterMonth(Number[] register, int index) {
		if(index == -1 || register.length >= index ) {
			return null;
		}
		
		return register[index];
	}
	
	protected void setRegisterMonth(Number[] register, int index, Number value) {
		if(index == -1 || register.length >= index ) {
			return;
		}
		
		register[index] = value;
	}
	
	protected void applySum(int month, int idTypeApply, int[] idTypeValues) {
		
		setRegisterMonth(registers[month], getOrderTypeById(idTypeApply), 
				sumValueByIdType(idTypeValues, registers[month]) );
	}
	
	
	protected void applyMulti(int month, int idTypeApply, int idTypeSource, double constanta) {
		
		setRegisterMonth(registers[month], getOrderTypeById(idTypeApply), 
				constanta * nvl(getRegisterMonth(registers[month], 
						                         getOrderTypeById(idTypeSource))) );
	}
	
	protected void applyMinus(int month, int idTypeApply, int idTypeMinus, int idTypeMinus2) {
		setRegisterMonth(registers[month], getOrderTypeById(idTypeApply), 
				nvl(getRegisterMonth(registers[month], 
						             getOrderTypeById(idTypeMinus)) ) -
			    nvl(getRegisterMonth(registers[month], 
								     getOrderTypeById(idTypeMinus2)) )	);
	}
	
	protected boolean insertRegister(Number value, int typeOrder, int monthOrder, boolean isCommit) {
		logger.debug((value == null ? "value:null" : value.doubleValue()));

		try {
			AfinaQuery.INSTANCE.execute(getInsertRegs(),
					new Object[]{getMonthDate(monthOrder),
					types.get(typeOrder)[0], value} );

			registers[monthOrder][typeOrder] = value;

			return true;
		} catch (SessionException e) {
			e.printStackTrace();

			return false;
		}
	}
	
	protected boolean updateRegister(Number value, int typeOrder, int monthOrder, boolean isCommit) {
		logger.debug((value == null ? "value:null" : value.doubleValue()));

		try {
			AfinaQuery.INSTANCE.execute(getUpdateRegs(),
					new Object[]{value, getMonthDate(monthOrder),
					types.get(typeOrder)[0]} );

			registers[monthOrder][typeOrder] = value;

			return true;
		} catch (SessionException e) {
			e.printStackTrace();

			return false;
		}
	}
	
	/**
	 * Рассчитываем общий стобец
	 */
	protected void computeTotalColumn() {
		for (int row = 0; row < types.size(); row++) {
			if(types.get(row)[2] == null || ((Number)types.get(row)[2]).intValue() == 1 ||
					((Number)types.get(row)[2]).intValue() == 2) continue; // неча обновлять
			
			double value = 0;
			for (int month = 0; month < (getFinBankPlanMonthCount() - 1); month++) {
				value += nvl(registers[month][row]);
			}
			registers[(getFinBankPlanMonthCount() - 1)][row] = value;
		}
	}
	
	
	@Override
	public Object getFinBankPlanDataValue(int typeOrder, int monthOrder) {
		if (registers == null || monthOrder >= registers.length || 
			typeOrder >= registers[monthOrder].length || registers[monthOrder][typeOrder] == null)
			return  null;
		final Number val = (Number)registers[monthOrder][typeOrder];

		return val == null ? null : val.doubleValue();
	}

	@Override
	public int getFinBankPlanMonthCount() {
		return 13; // пока...
	}

	@Override
	public String getFinBankPlanMonthName(int index) {
		return index < 12 ? getMonthByFormat(index) : getNameMoreMonth(index);
	}

	private String getMonthByFormat(int monthIndex) {
		Calendar calendarTmp = new GregorianCalendar(reportDate().get(Calendar.YEAR), 0, 1);
		calendarTmp.add(Calendar.MONTH, monthIndex);
		SimpleDateFormat weekFormatter = new SimpleDateFormat("MMMMM");
		return weekFormatter.format(calendarTmp.getTime());
	}

	@Override
	public int getFinBankPlanTypeCount() {
		return (types == null) ? 0 : types.size();
	}

	@Override
	public FinPlanTypeValue getFinBankPlanTypeName(int index) {
		if(types == null || index >= types.size() ) return null;
		
		return new FinPlanTypeValue(types.get(index)[1], types.get(index)[2]);
	}

	@Override
	public int getFinBankPlanTypePart(int index) {
		return (types == null || index >= types.size() ) ? -1 : ((Number)types.get(index)[2]).intValue();
	}

	@Override
	public String getFinBankPlanReportYear() {

	// return owner.getReportYear();
		return ""; // TODO: 18.01.2022
	}

	// TODO
	private Calendar reportDate() {
		Calendar calendarTmp = new GregorianCalendar();
		calendarTmp.setTime(new Date() );

		return new GregorianCalendar(calendarTmp.get(Calendar.YEAR), 0, 1);
	}

	public java.sql.Date getMonthDate(int monthOrder) {
		Calendar calendarTmp = null;
		if(monthOrder > Calendar.DECEMBER) {
			calendarTmp = new GregorianCalendar(reportDate().get(Calendar.YEAR), Calendar.DECEMBER, 31);
		} else {
			calendarTmp = new GregorianCalendar(reportDate().get(Calendar.YEAR), monthOrder, 1);
		}
		return new java.sql.Date( calendarTmp.getTimeInMillis() );
	}

	@Override
	public boolean updateFinBankPlanDataValue(Double value, int typeOrder, int dayOrder) {

		if(Objects.equals(value, registers[dayOrder][typeOrder])) return true;
				
		boolean result = false;
		
		result = updateRegister(
				value, typeOrder, dayOrder, true);
		if(!result) { // не нашли че обновлять
			result = insertRegister(value, typeOrder, dayOrder, true);
		}
		if(result) {
			computeMonth(dayOrder);
			computeTotalRow(typeOrder);
			computeTotalColumn();
		}
		return result;
	}
	
	protected void actionBeforeClose(int typeOrder, int monthOrder, boolean isCommited) {

		if (registers[monthOrder].length <= typeOrder)
			return;

		Number value = registers[monthOrder][typeOrder];
		boolean result = false;
		
		result = updateRegister(
				value, typeOrder, monthOrder, isCommited);
		if(!result) { // не нашли че обновлять
			result = insertRegister(value, typeOrder, monthOrder, isCommited);
		}
	}

	protected Object[] getTypesByIndex(int index) {
		return (types == null || index >= types.size() ) ? null : types.get(index);
	}

}

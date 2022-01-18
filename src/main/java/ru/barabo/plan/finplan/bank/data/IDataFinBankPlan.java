package ru.barabo.plan.finplan.bank.data;


import ru.barabo.plan.correct.profit.data.FinPlanTypeValue;

/**
 * @author debara
 *
 * Интерфейсы для "Финансого плана за год по всему банку"
 */
public interface IDataFinBankPlan {
	int getFinBankPlanTypeCount();
	
	FinPlanTypeValue getFinBankPlanTypeName(int index);
	
	/**
	 * @param index
	 * @return тип раздела - 0 обычный, 1 главный, 2 гл. уровня 2, 3 - сумма, 
	 * 4 - сумма уровня 2., 5-обычный жирный 
	 */
	int getFinBankPlanTypePart( int index);
	
	int getFinBankPlanMonthCount();
	
	String getFinBankPlanMonthName(int index);
	
	Object getFinBankPlanDataValue(int typeOrder, int monthOrder);
	
	boolean updateFinBankPlanDataValue(Double value, int typeOrder, int dayOrder);
	
	/**
	 * @return возвр. год в кот. размещен план
	 */
	String getFinBankPlanReportYear();
	
	/**
	 * событие перед закрытием приложения
	 */
	void actionBeforeClose();
}

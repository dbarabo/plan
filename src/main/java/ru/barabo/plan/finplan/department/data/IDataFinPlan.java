package ru.barabo.plan.finplan.department.data;

import ru.barabo.plan.correct.profit.data.FinPlanTypeValue;

/**
 * @author debara
 *
 * Интерфейсы для "Финансого плана за год по отделам"
 */
public interface IDataFinPlan {

	int getFinPlanTypeCount();
	
	FinPlanTypeValue getFinPlanTypeName(int index);
	
	/**
	 * @param index
	 * @return тип раздела - 0 обычный, 1 главный, 2 гл. уровня 2, 3 - сумма, 4 - сумма уровня 2. 
	 */
	int getFinPlanTypePart( int index);
	
	/**
	 * @return возвр. год в кот. размещен план
	 */
	String getReportYear();
	
	int getFinPlanMonthCount();
	
	String getFinPlanMonthName(int index);
	
	Object getFinPlanDataValue(int typeOrder, int monthOrder);
	
	boolean updateFinPlanDataValue(Number value, int typeOrder, int dayOrder);
}

package ru.barabo.plan.correct.profit.data;

public class FinPlanTypeValue {
	
	private String name; // название раздела
	private int    type; // тип раздела
	
	public FinPlanTypeValue(Object name, Object type) {
		this.name = (name == null) ? null : name.toString();
		this.type = (type == null || !(type instanceof Number)) ? -1 : ((Number)type).intValue();
	}
	
	public FinPlanTypeValue() {
		this.name = null;
		this.type = -1;
	}

	public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}
}

package ru.barabo.total.db.impl;

import org.apache.log4j.Logger;
import ru.barabo.total.db.FieldItem;

import java.util.List;
import java.util.Vector;

public abstract class AbstractRowFields {

	final static transient private Logger logger = Logger.getLogger(AbstractRowFields.class.getName());
	
	private List<FieldItem> fields;
	
	abstract protected List<FieldItem> createFields();

	static public <T extends AbstractRowFields> T create(Class<T> clazz) {

		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("create", e);

			throw new NumberFormatException(e.getMessage());
		}
	}

	static public <T extends AbstractRowFields> T create(Object[] row, Class<T> rowClazz) {
		T rowField = create(rowClazz);

        //logger.error("rowField=" + rowField.fieldItems());

		for (int index = 0; index < Math.min(rowField.fieldItems().size(), row.length); index++) {
			rowField.fieldItems().get(index).setValueFieldObject(row[index]);
		}

		return rowField;
	}

	public AbstractRowFields() {
		fields = createFields();
	}
	
	public List<FieldItem> fieldItems() {
		return fields;
	}

	public FieldItem getFieldByLabel(String label) {

		for (FieldItem field : fieldItems()) {
			if (label.equals(field.getLabel())) {
				return field;
			}
		}

		return null;
	}

	public String getRowString() {
		StringBuilder result = new StringBuilder();
		
		for(FieldItem field : fields) {
			if(field.isExistsGrid()) {
				result.append(field.getValueField() == null ? "" : field.getValueField()).append("\t");
			}
		}
		
		return result.toString();
	}

    public Object[] getFields() {
		Object[] row = new Object[fields.size()];
		
		for(int index = 0; index < fields.size(); index++) {
			row[index] = fields.get(index).getVal();
		}
		return row;
	}
	
	private long getFieldDBCount() {
		
		return fields.stream().filter(field -> field.getColumn() != null).count();
		
	}
	
	public Object[] getFieldsDB() {
		
		Object[] row = new Object[(int)getFieldDBCount()];
		
		int index = 0;
		for(FieldItem field : fields) {
			if(field.getColumn() != null) {
				row[index] = field.getVal();
				index++;
			}
		}
		return row;
	}

    public Number getId() {
		return (Number)fields.get(0).getVal();
	}
	
	public void setId(Number id) {
		fields.get(0).setValueFieldObject(id);
	}
	
	public String getName() {

		return (String)fields.get(1).getVal();
	}

	public void setName(String name) {
		fields.get(1).setValueFieldObject(name);
	}

	public Number getOrd() {
		return (Number)fields.get(3).getVal();
	}

	public void setOrd(Number ord) {
		fields.get(3).setValueFieldObject(ord);
	}

	public int getTypePl() {
		Number val = (Number)fields.get(2).getVal();

		return val == null ? -1 : val.intValue();
	}

	public void setTypePl(Number typePl) {
		fields.get(2).setValueFieldObject(typePl);
	}

	public void cloneTo(AbstractRowFields destRow) {

		for(int index = 0; index < fields.size(); index++) {
			destRow.fields.get(index).setValueFieldObject(fields.get(index).getVal() );
		}
	}

	public String getUpdateData(AbstractRowFields oldData,
								Vector<Object> values) {

		String result = null;

		for(int index = 1; index < fields.size(); index++) {

			if(!isEqual(fields.get(index).getVal(),
					oldData.fields.get(index).getVal()) ) {

				values.add(fields.get(index).getVal());

				if(result != null) {
					result += ", ";
					result += fields.get(index).getColumn() + " = ?";

				}  else {
					result = fields.get(index).getColumn() + " = ?";
				}
			}
		}

		values.add(fields.get(0).getVal());
		return result;
	}

	private boolean isEqual(Object val1, Object val2) {
		if(val1 == val2) return true;

		if(val1 == null || val2 == null) return false;

		return val1.equals(val2);
	}

	public String getAccounts() {
		return (String)fields.get(7).getVal();
	}

	public String getAccountsPercent() {

		String res = (String)fields.get(8).getVal();
		if(res == null || "".equals(res)) return null;

		Number perc = (Number)fields.get(9).getVal();
		if(perc == null) return null;

		int val = (int) (perc.doubleValue()*100);

		String percTak = "/" + val + "\n";

		res = res.replace(",", percTak);

		res = res.replace("'", "");

		res += percTak;

		return res;
	}
}

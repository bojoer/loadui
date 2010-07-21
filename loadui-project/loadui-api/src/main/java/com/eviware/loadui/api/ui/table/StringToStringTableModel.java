/*
 * Copyright 2010 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.api.ui.table;

import java.util.ArrayList;

public class StringToStringTableModel extends KeyValueTableModel {

	private static final long serialVersionUID = -6107162368567835864L;

	private ArrayList<StringProperty> data = new ArrayList<StringProperty>();

	public StringToStringTableModel() {
		header = new String[] { "Property", "Value" };
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return data.get(rowIndex).getName();
		case 1:
			return data.get(rowIndex).getValue();
		default:
			return null;
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		StringProperty p = data.get(rowIndex);
		p.setValue((String)aValue);
		hashCode();
		fireTableDataChanged();
		observer.startNotification();
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		for (StringProperty v : data) {
			hashCode += v.getValue() == null ? 0 : v.getValue().hashCode();
		}
		return hashCode;
	}

	public void update(StringToStringTableModel model) {
		for (int i = 0; i < model.getRowCount(); i++) {
			StringProperty p = data.get(i);
			p.setValue((String)model.getValueAt(i, 1));
		}
		hashCode();
		fireTableDataChanged();
		observer.startNotification();
	}

	public void addRow(StringProperty p) {
		data.add(p);
		hashCode();
		fireTableDataChanged();
	}

	public void addRow(String name, String value) {
		data.add(new StringProperty(name, value));
		hashCode();
		fireTableDataChanged();
	}

	public ArrayList<StringProperty> getData() {
		return data;
	}
	
	public static class StringProperty {
		private String name;
		private String value;

		public StringProperty(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String v) {
			this.value = v;
		}

		@Override
		public String toString() {
			return name + "=" + value;
		}
	}

}

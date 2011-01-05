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

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;

import com.eviware.loadui.api.property.Property;

public class SettingsTableModel extends KeyValueTableModel {

	private static final long serialVersionUID = 3644028575932424988L;
	
	private ArrayList<PropertyProxy> data = new ArrayList<PropertyProxy>();

	public SettingsTableModelObserver observer = new SettingsTableModelObserver(this);

	public SettingsTableModel() {
		header = new String[] { "Property Name", "Property Value" };
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
		PropertyProxy p = data.get(rowIndex);
		if (p.getType().getSimpleName().equals(File.class.getSimpleName())) {
			p.setValue(new File((String) aValue));
		}
		else if (p.getType().getSimpleName().equals(Double.class.getSimpleName())) {
			p.setValue(Double.valueOf((String) aValue));
		}
		else if (p.getType().getSimpleName().equals(Boolean.class.getSimpleName())) {
			p.setValue(Boolean.valueOf((String) aValue));
		}
		else if (p.getType().getSimpleName().equals(Long.class.getSimpleName())) {
			p.setValue(Long.valueOf((String) aValue));
		}
		else {
			p.setValue(aValue);
		}
		hashCode();
		fireTableDataChanged();
		observer.startNotification();
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		for (PropertyProxy v : data) {
			hashCode += v.getValue() == null ? 0 : v.getValue().hashCode();
		}
		return hashCode;
	}

	public void addRow(PropertyProxy p) {
		data.add(p);
		hashCode();
		fireTableDataChanged();
	}

	public static class PropertyProxy {
		private String name;
		private Object value;
		private Class pClass;

		public PropertyProxy(Property p) {
			this.name = p.getKey();
			this.value = p.getValue();
			this.pClass = p.getType();
		}

		public PropertyProxy(String name, Object value, Class type) {
			this.name = name;
			this.value = value;
			this.pClass = type;
		}

		public Class getType() {
			return pClass;
		}

		public String getName() {
			return name;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object v) {
			this.value = v;
		}

		@Override
		public String toString() {
			return name == null ? "name null" : name + " " + value == null ? "null" : "" + value;
		}
	}

	public static class SettingsTableModelObserver extends Observable {

		public SettingsTableModel model;

		public SettingsTableModelObserver(SettingsTableModel model) {
			this.model = model;
		}

		public void startNotification() {
			setChanged();
			notifyObservers();
		}
	}
}
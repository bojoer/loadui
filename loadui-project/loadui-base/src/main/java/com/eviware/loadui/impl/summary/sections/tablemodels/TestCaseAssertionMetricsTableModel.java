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
package com.eviware.loadui.impl.summary.sections.tablemodels;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ComponentItem;

public class TestCaseAssertionMetricsTableModel extends AbstractTableModel {

	String[] columnNames = {"name", "cnt", "passed", "failed", "failure ratio"};
	ArrayList<AssertionMetricsModel> data = new ArrayList<AssertionMetricsModel>();
	
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		switch(col) {
		case 0: 
			return data.get(row).getName();
		case 1:
			return data.get(row).getCnt();
		case 2:
			return data.get(row).getPassed();
		case 3:
			return data.get(row).getFailed();
		case 4:
			return data.get(row).getFailRatio();
		default:
				return null;
		}
	}

	public class AssertionMetricsModel {
		String name;
		String cnt;
		String passed;
		String failed;
		String failRatio;
		
		public AssertionMetricsModel(ComponentItem component) {
			name = component.getLabel();
			cnt = String.valueOf(component.getCounter(CanvasItem.ASSERTION_COUNTER).get());
			passed = String.valueOf(component.getCounter(CanvasItem.ASSERTION_COUNTER).get() - component.getCounter(CanvasItem.FAILURE_COUNTER).get());
			failed = String.valueOf(component.getCounter(CanvasItem.FAILURE_COUNTER).get());
			int failedCount = Integer.parseInt(failed);
			int count = Integer.parseInt(cnt);
			int perc = 0;
			if (count > 0)
				perc = (failedCount * 100/count);
			failRatio = failed + " / " + cnt + " (" + perc + "%)";
		}

		public String getName() {
			return name;
		}

		public String getCnt() {
			return cnt;
		}

		public String getPassed() {
			return passed;
		}

		public String getFailed() {
			return failed;
		}

		public String getFailRatio() {
			return failRatio;
		}
		
		@Override
		public String toString() {
			return name + "-"+cnt+"-"+passed+"-"+failed+"-"+failRatio;
		}
	}

	public void add(AssertionMetricsModel row) {
		data.add(row);
		fireTableDataChanged();
	}
}

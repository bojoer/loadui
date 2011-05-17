/*
 * Copyright 2011 eviware software ab
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
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class TestCaseSamplerStatisticsTable extends AbstractTableModel {

	String[] columnNames = { "name", "cnt", "min", "max", "avg", "std-dev",
			"min/avg", "max/avg", "err", "ratio" };
	ArrayList<TestCaseSamplerStatisticsModel> data = new ArrayList<TestCaseSamplerStatisticsModel>();

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
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0)
			return data.get(rowIndex).getName();
		else if (columnIndex == 9 ) {
			
		} else {
			String v = data.get(rowIndex).getStat(columnNames[columnIndex]);
			if ( v.equals("-1") )
				return "N/A";
		}
		return data.get(rowIndex).getStat(columnNames[columnIndex]);
	}

	public void add(TestCaseSamplerStatisticsModel row) {
		data.add(row);
	}

	public class TestCaseSamplerStatisticsModel {

		String name;
		HashMap<String, String> stats;

		public TestCaseSamplerStatisticsModel(String label,
				Map<String, String> stats) {
			this.name = label;
			this.stats = new HashMap<String, String>(stats);
		}

		public String getStat(String statName) {
			return stats.get(statName);
		}

		public String getName() {
			return this.name;
		}
	}

}

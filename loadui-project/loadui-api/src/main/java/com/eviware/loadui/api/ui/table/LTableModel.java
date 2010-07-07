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
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class LTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 5947811194397913150L;
	
	private static Logger log = LoggerFactory.getLogger( "com.eviware.loadui.api.ui.table.LTableModel" );

	private ArrayList<String> header = new ArrayList<String>();
	private ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

	private int maxRow;
	private boolean follow;
	
	public LTableModel(int maxRow, boolean follow) {
		this.maxRow = maxRow;
		this.follow = follow;
	}

	@Override
	public int getColumnCount() {
		if( header.size() == 0 ) return 1;
		return header.size();
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		try
		{
			return data.get(rowIndex).get(columnIndex);
		}
		catch( Throwable t )
		{
			t.printStackTrace();
			return null;
		}
	}

	@Override
	public String getColumnName(int column) {
		if( header.size() == 0 ) return " ";
		return header.get(column);
	}

	public void addColumn(String columnName) {
		if (!header.contains(columnName)) {
			header.add(columnName);
			// add a column for each row
			for (ArrayList<String> row : data) {
				row.add("");
			}
			fireTableStructureChanged();
		}
	}

	public boolean addRow(ArrayList<String> row) {
		while ( maxRow > 0 && !(data.size() < maxRow) )
		{
			data.remove(0);
			fireTableRowsDeleted( 0, 0 );
		}	
		boolean result = data.add(row);
		fireTableRowsInserted( data.size()-1, data.size()-1  );
		return result;
	}

	public boolean addRow(Map<String, String> row) {
		if (!row.isEmpty()) {
			// first check for columns
			for (String key : row.keySet()) {
				if (!header.contains(key)) {
					addColumn(key);
				}
			}

			ArrayList<String> newRow = new ArrayList<String>();
			for (int cnt = 0; cnt < header.size(); cnt++)
				newRow.add("");
			for (String key : row.keySet()) {
				if (header.contains(key)) {
					newRow.set(header.indexOf(key), row.get(key));
				}
			}
			addRow(newRow);
			return true;
		}
		return false;
	}

	public int getMaxRow() {
		return maxRow;
	}

	public void setMaxRow(Integer maxRow) {
		this.maxRow = maxRow;
		if ( maxRow <= data.size() ) {
			int numberOfTopRowsToDelete = data.size() - maxRow + 1;
			for( int cnt = 0; cnt < numberOfTopRowsToDelete; cnt++ ) 
			{				
				data.remove(0);
				fireTableRowsDeleted( 0, 0 );
			}
		}
		
	}
	
	public ArrayList getLastRow() {
		return data.get(data.size() -1);
	}
	
	public void reset() {
		header.clear();
		data.clear();
		fireTableStructureChanged();
	}
	
	public void clear() {
		data.clear();
		fireTableDataChanged();
	}

	public boolean isFollow() {
		return follow;
	}

	public void setFollow(boolean follow) {
		this.follow = follow;
		fireTableDataChanged();
	}
	
	public LTableModel getLastRows(long numRows) {
		if (numRows >= data.size())
			return this;
		LTableModel result = new LTableModel((int)numRows, false);
		for (String col: header)
			result.addColumn(col);
		for ( int cnt = data.size() - (int)numRows; cnt <= data.size() - 1; cnt++ ) {
			result.addRow(data.get(cnt));
		}
		return result;
	}
	
}

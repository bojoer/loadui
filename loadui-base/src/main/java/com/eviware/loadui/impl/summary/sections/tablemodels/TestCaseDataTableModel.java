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

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.impl.model.SceneItemImpl;
import com.eviware.loadui.util.summary.CalendarUtils;

public class TestCaseDataTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = -99646701272738332L;

	String columnNames[] = { "Test Case", "exec time", "requests", "assertions", "failed assertions" };
	ArrayList<TestCaseDataModel> data = new ArrayList<TestCaseDataModel>();

	public void add( TestCaseDataModel row )
	{
		data.add( row );
		fireTableDataChanged();
	}

	@Override
	public String getColumnName( int column )
	{
		return columnNames[column];
	}

	@Override
	public int getColumnCount()
	{
		return columnNames.length;
	}

	@Override
	public int getRowCount()
	{
		return data.size();
	}

	@Override
	public Object getValueAt( int row, int col )
	{
		switch( col )
		{
		case 0 :
			return data.get( row ).getName();
		case 1 :
			return data.get( row ).getExecTime();
		case 2 :
			return data.get( row ).getNumberOfSamples();
		case 3 :
			return data.get( row ).getNumberOfAssertions();
		case 4 :
			return data.get( row ).getNumberOfFailedAssertions();
		default :
			return null;
		}
	}

	public static class TestCaseDataModel
	{
		String name;
		String execTime;
		String numberOfSamples;
		String numberOfAssertions;
		String numberOfFailedAssertions;
		SimpleDateFormat dateFormat;

		public TestCaseDataModel( SceneItemImpl tc )
		{
			this.name = tc.getLabel();

			this.execTime = CalendarUtils.getFormattedPeriod( tc.getStartTime(), tc.getEndTime() );

			this.numberOfSamples = String.valueOf( tc.getCounter( CanvasItem.SAMPLE_COUNTER ).get() );

			this.numberOfAssertions = String.valueOf( tc.getCounter( CanvasItem.ASSERTION_COUNTER ).get() );

			this.numberOfFailedAssertions = String.valueOf( tc.getCounter( CanvasItem.ASSERTION_FAILURE_COUNTER ).get() );
		}

		public String getName()
		{
			return name;
		}

		public void setName( String name )
		{
			this.name = name;
		}

		public String getExecTime()
		{
			return execTime;
		}

		public void setExecTime( String execTime )
		{
			this.execTime = execTime;
		}

		public String getNumberOfSamples()
		{
			return numberOfSamples;
		}

		public void setNumberOfSamples( String numberOfSamples )
		{
			this.numberOfSamples = numberOfSamples;
		}

		public String getNumberOfAssertions()
		{
			return numberOfAssertions;
		}

		public void setNumberOfAssertions( String numberOfAssertions )
		{
			this.numberOfAssertions = numberOfAssertions;
		}

		public String getNumberOfFailedAssertions()
		{
			return numberOfFailedAssertions;
		}

		public void setNumberOfFailedAssertions( String numberOfFailedAssertions )
		{
			this.numberOfFailedAssertions = numberOfFailedAssertions;
		}

	}
}

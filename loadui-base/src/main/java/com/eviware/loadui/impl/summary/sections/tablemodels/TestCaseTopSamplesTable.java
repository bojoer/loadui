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
import java.util.Collections;
import java.util.Comparator;
import javax.swing.table.AbstractTableModel;

import com.eviware.loadui.api.summary.SampleStats;
import com.eviware.loadui.util.summary.CalendarUtils;

public class TestCaseTopSamplesTable extends AbstractTableModel
{

	String[] columnNames = { "name", "ms", "time", "size" };
	public ArrayList<TestCaseSampleModel> data = new ArrayList<TestCaseSampleModel>();
	TestCaseSampleModelComparator bottomUpComparator = new TestCaseSampleModelComparator();
	Comparator<TestCaseSampleModel> topDownComparator = Collections.reverseOrder( new TestCaseSampleModelComparator() );

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
		if( data.size() > 5 )
			return 5;
		else
			return data.size();
	}

	@Override
	public String getValueAt( int rowIndex, int columnIndex )
	{
		switch( columnIndex )
		{
		case 0 :
			return data.get( rowIndex ).getName();
		case 1 :
			return String.valueOf( data.get( rowIndex ).getStats().getTimeTaken() );
		case 2 :
			long time = data.get( rowIndex ).getStats().getTime();
			return CalendarUtils.format( time );
		case 3 :
			return String.valueOf( data.get( rowIndex ).getStats().getSize() );
		default :
			return null;
		}
	}

	public static class TestCaseSampleModel
	{
		String name;
		SampleStats stats;

		public TestCaseSampleModel( String label, SampleStats stats )
		{
			name = label;
			this.stats = stats;
		}

		public String getName()
		{
			return name;
		}

		public SampleStats getStats()
		{
			return stats;
		}
	}

	public void add( String label, SampleStats stat, boolean topDown )
	{
		synchronized( data )
		{
			data.add( new TestCaseSampleModel( label, stat ) );
			Collections.sort( data, topDown ? topDownComparator : bottomUpComparator );
			if( data.size() > 5 )
				data.remove( 5 );
		}
	}

	public void finalizeOrdering( boolean topDown )
	{
		if( topDown )
			Collections.sort( data, topDownComparator );
		else
			Collections.sort( data, bottomUpComparator );
	}
	
	private static class TestCaseSampleModelComparator implements Comparator<TestCaseSampleModel>
	{
		@Override
		public int compare( TestCaseSampleModel o1, TestCaseSampleModel o2 )
		{
			long i1 = o1.getStats().getTimeTaken();
			long i2 = o2.getStats().getTimeTaken();
			return ( int )( i2 - i1 );
		}
	}
}

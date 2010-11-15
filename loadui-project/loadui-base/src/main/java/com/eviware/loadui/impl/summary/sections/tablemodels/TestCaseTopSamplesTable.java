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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import javax.swing.table.AbstractTableModel;

import com.eviware.loadui.api.summary.SampleStats;

public class TestCaseTopSamplesTable extends AbstractTableModel
{

	String[] columnNames = { "name", "ms", "time", "size" };
	ArrayList<TestCaseSampleModel> data = new ArrayList<TestCaseSampleModel>();

	// SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

	@Override
	public String getColumnName( int column )
	{
		return columnNames[column];
	}

	public TestCaseTopSamplesTable()
	{
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
			SimpleDateFormat df;
			long time = data.get( rowIndex ).getStats().getTime();
			if( time < 3600000 )
				df = new SimpleDateFormat( "mm:ss" );
			else
				df = new SimpleDateFormat( "HH:mm:ss" );
			return df.format( new Date( time ) );
		case 3 :
			return String.valueOf( data.get( rowIndex ).getStats().getSize() );
		default :
			return null;
		}
	}

	public class TestCaseSampleModel
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

	public void addTop( String label, SampleStats stat )
	{
		synchronized( data )
		{
			data.add( new TestCaseSampleModel( label, stat ) );
			// System.out.println("add " + stat);
			Collections.sort( data, new TestCaseSampleModelComparator() );
			if( data.size() > 11 )
				data.remove( 11 );
		}
	}

	public void addBottom( String label, SampleStats stat )
	{
		synchronized( data )
		{
			// System.out.println("badd " + stat);
			data.add( new TestCaseSampleModel( label, stat ) );
			Collections.sort( data, new TestCaseSampleModelComparator2() );
			if( data.size() > 11 )
				data.remove( 11 );
		}
	}

	private class TestCaseSampleModelComparator implements Comparator<TestCaseSampleModel>
	{

		@Override
		public int compare( TestCaseSampleModel o1, TestCaseSampleModel o2 )
		{
			long i1 = o1.getStats().getTimeTaken();
			long i2 = o2.getStats().getTimeTaken();
			return ( int )( i2 - i1 );
		}

	}

	private class TestCaseSampleModelComparator2 implements Comparator<TestCaseSampleModel>
	{

		@Override
		public int compare( TestCaseSampleModel o1, TestCaseSampleModel o2 )
		{
			long i1 = o1.getStats().getTimeTaken();
			long i2 = o2.getStats().getTimeTaken();
			return ( int )( i1 - i2 );
		}

	}
}

/*
 * Copyright 2011 SmartBear Software
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
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LTableModel extends AbstractTableModel
{

	private static final long serialVersionUID = 5947811194397913150L;

	private static Logger log = LoggerFactory.getLogger( "com.eviware.loadui.api.ui.table.LTableModel" );

	private final List<String> header = new ArrayList<>();
	private final List<List<String>> data = new ArrayList<>();

	private int maxRow;
	private boolean follow;
	private boolean enabledInDistMode;

	public LTableModel( int maxRow, boolean follow )
	{
		this.maxRow = maxRow;
		this.follow = follow;
		this.enabledInDistMode = false;
	}

	public LTableModel( int maxRow, boolean follow, boolean enabledInDistMode )
	{
		this.maxRow = maxRow;
		this.follow = follow;
		this.enabledInDistMode = enabledInDistMode;
	}

	public List<String> getHeader()
	{
		return header;
	}

	@Override
	public int getColumnCount()
	{
		if( header.size() == 0 )
			return 1;
		return header.size();
	}

	@Override
	public int getRowCount()
	{
		return data.size();
	}

	@Override
	public Object getValueAt( int rowIndex, int columnIndex )
	{
		try
		{
			if( data.size() > rowIndex && data.get( rowIndex ).size() > columnIndex )
				return data.get( rowIndex ).get( columnIndex );
			else
				return "";
		}
		catch( Exception e )
		{
			// e.printStackTrace();
			log.error( e.getMessage() );
			return null;
		}
	}

	@Override
	public String getColumnName( int column )
	{
		if( header.size() == 0 )
			return " ";
		return header.get( column );
	}

	public void addColumn( String columnName )
	{
		if( !header.contains( columnName ) )
		{
			header.add( columnName );
			// add a column for each row
			for( List<String> row : data )
			{
				row.add( "" );
			}
			fireTableStructureChanged();
		}
	}

	public boolean addRow( List<String> row )
	{
		if( maxRow > 0 )
		{
			while( maxRow > 0 && !( data.size() < maxRow ) )
			{
				data.remove( 0 );
				fireTableRowsDeleted( 0, 0 );
			}
			boolean result = data.add( row );
			fireTableRowsInserted( data.size() - 1, data.size() - 1 );
			return result;
		}
		else
		{
			return false;
		}
	}

	public boolean addRow( Map<String, Object> row )
	{
		if( !row.isEmpty() )
		{
			// first check for columns
			for( String key : row.keySet() )
			{
				if( !header.contains( key ) )
				{
					addColumn( key );
				}
			}

			List<String> newRow = new ArrayList<>();
			for( int cnt = 0; cnt < header.size(); cnt++ )
				newRow.add( "" );
			for( String key : row.keySet() )
			{
				if( header.contains( key ) )
				{
					newRow.set( header.indexOf( key ), String.valueOf( row.get( key ) ) );
				}
			}
			addRow( newRow );
			return true;
		}
		return false;
	}

	public int getMaxRow()
	{
		return maxRow;
	}

	public void setMaxRow( Integer maxRows )
	{
		if( maxRows == null )
			return;
		this.maxRow = maxRows;
		while( data.size() > maxRows )
		{
			data.remove( 0 );
			fireTableRowsDeleted( 0, 0 );
		}
	}

	public List<String> getLastRow()
	{
		return data.get( data.size() - 1 );
	}

	public List<String> getRowAt( int rowIndex )
	{
		if( data.size() > rowIndex )
			return data.get( rowIndex );
		else
			return null;
	}

	public void reset()
	{
		header.clear();
		data.clear();
		fireTableStructureChanged();
	}

	public void clear()
	{
		data.clear();
		fireTableDataChanged();
	}

	public boolean isFollow()
	{
		return follow;
	}

	public void setFollow( boolean follow )
	{
		this.follow = follow;
		fireTableDataChanged();
	}

	public boolean isEnabledInDistMode()
	{
		return enabledInDistMode;
	}

	public void setEnabledInDistMode( boolean enabledInDistMode )
	{
		this.enabledInDistMode = enabledInDistMode;
		fireTableDataChanged();
	}

	public LTableModel getLastRows( long numRows )
	{
		if( numRows >= data.size() )
			return this;
		LTableModel result = new LTableModel( ( int )numRows, false );
		for( String col : header )
			result.addColumn( col );
		for( int cnt = data.size() - ( int )numRows; cnt <= data.size() - 1; cnt++ )
		{
			result.addRow( data.get( cnt ) );
		}
		return result;
	}

}

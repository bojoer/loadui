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

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.eviware.loadui.api.property.Property;

public class StringToStringTableModel extends KeyValueTableModel
{
	private static String unescape( String string )
	{
		return string == null ? "" : string.replaceAll( "&#124;", "|" ).replaceAll( "&#061;", "=" );
	}

	private static String escape( String string )
	{
		return string == null ? "" : string.replaceAll( "\\|", "&#124;" ).replaceAll( "=", "&#061;" );
	}

	private static final long serialVersionUID = -6107162368567835864L;

	private final List<StringProperty> data = new ArrayList<>();

	private Property<String> property;

	public StringToStringTableModel()
	{
		initialize();
	}

	public StringToStringTableModel( Property<String> property )
	{
		this.property = property;
		initialize();
	}

	private void initialize()
	{
		restore();

		header = new String[] { "Property", "Value" };
		addTableModelListener( new TableModelListener()
		{
			@Override
			public void tableChanged( TableModelEvent arg0 )
			{
				store();
			}
		} );
	}

	@Override
	public int getRowCount()
	{
		return data.size();
	}

	@Override
	public Object getValueAt( int rowIndex, int columnIndex )
	{
		switch( columnIndex )
		{
		case 0 :
			return data.get( rowIndex ).getName();
		case 1 :
			return data.get( rowIndex ).getValue();
		default :
			return null;
		}
	}

	@Override
	public void setValueAt( Object aValue, int rowIndex, int columnIndex )
	{
		StringProperty p = data.get( rowIndex );
		p.setValue( ( String )aValue );
		//		hashCode();
		fireTableDataChanged();
	}

	public void update( StringToStringTableModel model )
	{
		for( int i = 0; i < model.getRowCount(); i++ )
		{
			StringProperty p = data.get( i );
			p.setValue( ( String )model.getValueAt( i, 1 ) );
		}
		//		hashCode();
		fireTableDataChanged();
	}

	public void clear()
	{
		data.clear();
		fireTableDataChanged();
	}

	public void addRow( StringProperty p )
	{
		data.add( p );
		fireTableRowsInserted( data.size() - 1, data.size() - 1 );
	}

	public void addRow( String name, String value )
	{
		data.add( new StringProperty( name, value ) );
		fireTableRowsInserted( data.size() - 1, data.size() - 1 );
	}

	public void removeRow( String name )
	{
		for( int i = 0; i < data.size(); i++ )
		{
			if( data.get( i ).getName().equals( name ) )
			{
				data.remove( i );
				fireTableDataChanged();
				return;
			}
		}
	}

	public List<StringProperty> getData()
	{
		return data;
	}

	private void store()
	{
		if( property != null )
		{
			StringBuilder resultBuilder = new StringBuilder();
			for( StringProperty sp : data )
			{
				resultBuilder.append( escape( sp.getName() ) + "=" + escape( sp.getValue() ) + "|" );
			}
			String result = resultBuilder.toString();
			if( result.endsWith( "|" ) )
			{
				result = result.substring( 0, result.length() - 1 );
			}
			property.setValue( result );
		}
	}

	private void restore()
	{
		data.clear();
		if( property == null || property.getValue() == null )
		{
			return;
		}
		for( String pair : property.getValue().split( "\\|" ) )
		{
			String[] p = pair.split( "=" );
			if( p.length == 2 )
			{
				data.add( new StringProperty( unescape( p[0] ), unescape( p[1] ) ) );
			}
			else if( p.length == 1 )
			{
				data.add( new StringProperty( unescape( p[0] ), "" ) );
			}
		}
	}

	public static class StringProperty
	{
		private final String name;
		private String value;

		public StringProperty( String name, String value )
		{
			this.name = name;
			this.value = value;
		}

		public String getName()
		{
			return name;
		}

		public String getValue()
		{
			return value;
		}

		public void setValue( String v )
		{
			this.value = v;
		}

		@Override
		public String toString()
		{
			return name + "=" + value;
		}
	}

}

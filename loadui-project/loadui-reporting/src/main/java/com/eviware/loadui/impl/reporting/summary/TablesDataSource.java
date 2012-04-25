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
package com.eviware.loadui.impl.reporting.summary;

import java.util.Map;

import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class TablesDataSource extends JRAbstractBeanDataSource
{
	private final Map<String, TableModel> map;
	private int cnt = -1;
	private final String[] keys;
	private final TableModel[] tables;

	public TablesDataSource( Map<String, TableModel> map )
	{
		super( true );
		this.map = map;
		this.keys = map.keySet().toArray( new String[0] );
		this.tables = map.values().toArray( new TableModel[0] );

	}

	@Override
	public void moveFirst() throws JRException
	{
		cnt = 0;
	}

	@Override
	public Object getFieldValue( JRField field ) throws JRException
	{
		if( field.getName().equals( "title" ) )
			return keys[cnt];
		if( field.getName().equals( "table" ) )
			return new LTableDataSource( tables[cnt] );
		if( field.getName().equals( "column_count" ) )
			return Integer.valueOf( tables[cnt].getColumnCount() );
		if( field.getName().equals( "print_tables" ) )
		{
			// logger.debug("pt: " + (tables[cnt].getRowCount() > 0));
			return tables[cnt].getRowCount() > 0;
		}
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		return ++cnt < map.size();
	}

}

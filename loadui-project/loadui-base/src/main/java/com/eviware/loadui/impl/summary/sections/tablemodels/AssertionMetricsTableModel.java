/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.summary.sections.tablemodels;

import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.google.common.collect.Lists;

public class AssertionMetricsTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = -6015108629273771461L;
	private static final String[] columnNames = { "name", "constraint", "failures", "" };
	private final List<? extends AssertionItem<?>> assertions;

	public AssertionMetricsTableModel( Iterable<? extends AssertionItem<?>> assertions )
	{
		this.assertions = Collections.unmodifiableList( Lists.newArrayList( assertions ) );
	}

	@Override
	public String getColumnName( int column )
	{
		System.out.println( "    getColumnName(" + column + ") = " + columnNames[column] );
		return columnNames[column];
	}

	@Override
	public int getColumnCount()
	{
		System.out.println( "    getColumnCount = " + columnNames.length );
		return columnNames.length;
	}

	@Override
	public int getRowCount()
	{
		return assertions.size();
	}

	@Override
	public Object getValueAt( int row, int col )
	{
		switch( col )
		{
		case 0 :
			return assertions.get( row ).getLabel();
		case 1 :
			return assertions.get( row ).getConstraint().toString();
		case 2 :
			return Long.toString( assertions.get( row ).getFailureCount() );
		default :
			return ""; //throw new RuntimeException( "Table column out of bounds." );
		}
	}
}

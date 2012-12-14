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

import javax.swing.table.AbstractTableModel;

public abstract class KeyValueTableModel extends AbstractTableModel
{

	private static final long serialVersionUID = 6138113207187331068L;

	protected String[] header = { "Key", "Value" };

	@Override
	public String getColumnName( int column )
	{
		return header[column];
	}

	@Override
	public int getColumnCount()
	{
		return 2;
	}

	@Override
	public boolean isCellEditable( int rowIndex, int columnIndex )
	{
		if( columnIndex == 1 )
			return true;
		return false;
	}

}

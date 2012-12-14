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

import java.util.List;

import javax.swing.table.TableModel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;

import com.google.common.collect.Lists;

public class LTableDataSource extends JRTableModelDataSource
{

	private final TableModel model;

	public LTableDataSource( TableModel model )
	{
		super( model );
		//		System.out.println( "--created: " + model.getColumnName( 0 ) + " " + model.getColumnName( 1 ) );
		//		System.out.println( "--first value: " + model.getValueAt( 0, 0 ) );
		this.model = model;
	}

	@Override
	public Object getFieldValue( JRField jrField ) throws JRException
	{
		System.out.println( "! jrField.getName(): " + jrField.getName() );
		if( jrField.getName().equals( "columns" ) )
		{
			List<String> result = Lists.newArrayList();
			for( int i = 0; i < model.getColumnCount(); i++ )
			{
				result.add( model.getColumnName( i ) );
			}
			return result;
		}
		if( jrField.getName().startsWith( "COLUMN_" ) )
		{
			int index = Integer.parseInt( jrField.getName().substring( 7 ) );
			System.out.println( "!!! index: " + index + " model.getColumnCount(): " + model.getColumnCount() );
			if( index < model.getColumnCount() )
			{
				//				System.out.println( "!!! model.getColumnName( index ): " + model.getColumnName( index ) );
				//				System.out.println( "!!! super.getFieldValue( jrField ): " + super.getFieldValue( jrField ) );
				return super.getFieldValue( jrField );
			}
		}
		return null;
	}
}

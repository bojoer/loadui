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

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.summary.Section;

public class SectionDataSource extends JRAbstractBeanDataSource
{
	protected static final Logger log = LoggerFactory.getLogger( SectionDataSource.class );

	private final List<Section> section;
	private int cnt = -1;

	public SectionDataSource( List<Section> list )
	{
		super( true );
		this.section = list;
	}

	@Override
	public void moveFirst() throws JRException
	{
		cnt = 0;
	}

	@Override
	public Object getFieldValue( JRField field ) throws JRException
	{
		if( field.getName().equals( "values" ) )
			return new ValuesDataSource( section.get( cnt ).getValues() );
		if( field.getName().equals( "tables" ) )
			return new TablesDataSource( section.get( cnt ).getTables() );
		if( field.getName().equals( "title" ) )
		{
			return section.get( cnt ).getTitle();
		}
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		return ++cnt < section.size();
	}

}

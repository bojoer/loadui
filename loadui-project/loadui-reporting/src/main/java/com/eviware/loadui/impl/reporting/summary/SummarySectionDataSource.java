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

import com.eviware.loadui.api.summary.Section;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class SummarySectionDataSource extends JRAbstractBeanDataSource
{

	private final Section section;
	private int cnt = -1;

	public SummarySectionDataSource( Section section )
	{
		super( true );
		this.section = section;
	}

	@Override
	public void moveFirst() throws JRException
	{
		cnt = 0;
	}

	@Override
	public Object getFieldValue( JRField field ) throws JRException
	{
		if( field.getName().equals( "time" ) )
			return section.getValues().get( "duration" );
		if( field.getName().equals( "requests" ) )
			return section.getValues().get( "requests" );
		if( field.getName().equals( "afailures" ) )
			return section.getValues().get( "failures" );
		if( field.getName().equals( "status" ) )
			return section.getValues().get( "status" );

		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		cnt++ ;
		if( cnt == 1 )
			return false;
		return true;
	}

}

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

import java.util.Iterator;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

import com.eviware.loadui.api.summary.Summary;

public class SummaryDataSource extends JRAbstractBeanDataSource
{
	private final Summary summary;
	private Iterator<String> iterator;

	public SummaryDataSource( Summary summary )
	{
		super( true );

		this.summary = summary;
		iterator = summary.getChapters().keySet().iterator();
	}

	@Override
	public void moveFirst() throws JRException
	{
		iterator = summary.getChapters().keySet().iterator();
	}

	@Override
	public Object getFieldValue( JRField field ) throws JRException
	{
		if( field.getName().equals( "chapter" ) )
			return new ChapterDataSource( summary.getChapters().get( iterator.next() ) );
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		return iterator.hasNext();
	}
}

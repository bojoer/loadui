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
package com.eviware.loadui.impl.reporting.statistics;

import java.awt.Image;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.util.FormattingUtils;

public class ExecutionDataSource extends JRAbstractBeanDataSource
{
	private final Date date = new Date();
	private final String label;
	private final Execution execution;
	private final Collection<StatisticPage> pages;
	private final Map<? extends Object, Image> charts;
	private StatisticPage page;
	private Iterator<StatisticPage> iterator;
	private final long startTime;
	private final long endTime;

	public ExecutionDataSource( String label, Execution execution, Collection<StatisticPage> pages,
			Map<? extends Object, Image> charts )
	{
		super( true );

		this.label = label;
		this.execution = execution;
		this.pages = pages;
		this.charts = charts;
		startTime = execution.getStartTime();
		endTime = startTime + execution.getLength();
		iterator = pages.iterator();
	}

	@Override
	public void moveFirst() throws JRException
	{
		iterator = pages.iterator();
	}

	@Override
	public Object getFieldValue( JRField field ) throws JRException
	{
		switch( field.getName() )
		{
		case "loaduiVersion" :
			return LoadUI.VERSION;
		case "currentTime" :
			return date.toString();
		case "projectName" :
			return label;
		case "pageName" :
			return page.getLabel();
		case "startTime" :
			return new Date( startTime ).toString();
		case "endTime" :
			return new Date( endTime ).toString();
		case "duration" :
			return FormattingUtils.formatTime( Math.round( ( endTime - startTime ) / 1000.0 ) );
		case "totalRequests" :
			return execution.getAttribute( "totalRequests", "" );
		case "totalFailures" :
			return execution.getAttribute( "totalFailures", "" );
		case "chartGroup" :
			return new ChartGroupsDataSource( page.getChildren(), charts );
		default :
			return field.getName();
		}
	}

	@Override
	public boolean next() throws JRException
	{
		if( iterator.hasNext() )
		{
			page = iterator.next();
			return true;
		}
		return false;
	}
}

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
package com.eviware.loadui.impl.reporting.statistics;

import java.awt.Image;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.eviware.loadui.api.statistics.model.ChartGroup;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class ChartGroupsDataSource extends JRAbstractBeanDataSource
{
	private final Collection<ChartGroup> chartGroups;
	private final Map<Object, Image> charts;
	private Iterator<ChartGroup> chartGroupIterator;
	private ChartGroup chartGroup;

	public ChartGroupsDataSource( Collection<ChartGroup> chartGroups, Map<Object, Image> charts )
	{
		super( true );

		this.chartGroups = chartGroups;
		this.charts = charts;
		chartGroupIterator = chartGroups.iterator();
	}

	@Override
	public void moveFirst() throws JRException
	{
		chartGroupIterator = chartGroups.iterator();
	}

	@Override
	public Object getFieldValue( JRField field ) throws JRException
	{
		String fieldName = field.getName();

		if( fieldName.equals( "groupName" ) )
			return chartGroup.getLabel();
		if( fieldName.equals( "group" ) )
			return new ChartGroupDataSource( chartGroup, charts );
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		if( chartGroupIterator.hasNext() )
		{
			chartGroup = chartGroupIterator.next();
			return true;
		}
		return false;
	}
}
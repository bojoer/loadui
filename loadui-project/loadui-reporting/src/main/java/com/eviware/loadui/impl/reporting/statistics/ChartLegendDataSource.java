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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

import com.eviware.loadui.api.charting.ChartNamePrettifier;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.google.common.collect.Iterators;

public class ChartLegendDataSource extends JRAbstractBeanDataSource
{
	private final LineChartView chartView;
	private Iterator<LineSegment> iterator;
	private LineSegment lineSegment;

	public ChartLegendDataSource( LineChartView chartView )
	{
		super( true );

		this.chartView = chartView;
		iterator = Iterators.filter( chartView.getSegments().iterator(), LineSegment.class );
	}

	@Override
	public void moveFirst() throws JRException
	{
		iterator = Iterators.filter( chartView.getSegments().iterator(), LineSegment.class );
	}

	@Override
	public boolean next() throws JRException
	{
		if( iterator.hasNext() )
		{
			lineSegment = iterator.next();
			return true;
		}
		return false;
	}

	@Override
	public Object getFieldValue( JRField field ) throws JRException
	{
		String fieldName = field.getName();
		if( fieldName.equals( "statistic" ) )
			return ChartNamePrettifier.nameFor( lineSegment.getStatistic() );
		if( fieldName.equals( "source" ) )
			return ChartNamePrettifier.nameForSource( lineSegment.getStatistic().getSource() );
		if( fieldName.equals( "component" ) )
			return lineSegment.getStatistic().getStatisticVariable().getStatisticHolder().getLabel();
		if( fieldName.equals( "variable" ) )
			return lineSegment.getStatistic().getStatisticVariable().getLabel();
		if( fieldName.equals( "color" ) )
		{
			BufferedImage image = new BufferedImage( 12, 4, BufferedImage.TYPE_INT_RGB );
			Color color = Color.decode( lineSegment.getAttribute( "color", "#000000" ) );
			Graphics2D g = image.createGraphics();
			g.setBackground( color );
			g.clearRect( 0, 0, 12, 4 );
			g.dispose();
			return image;
		}
		return null;
	}
}

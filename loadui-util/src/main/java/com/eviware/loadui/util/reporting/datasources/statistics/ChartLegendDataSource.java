package com.eviware.loadui.util.reporting.datasources.statistics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.statistics.model.chart.LineChartView.LineSegment;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class ChartLegendDataSource extends JRAbstractBeanDataSource
{
	private final LineChartView chartView;
	private Iterator<LineSegment> iterator;
	private LineSegment lineSegment;

	public ChartLegendDataSource( LineChartView chartView )
	{
		super( true );

		this.chartView = chartView;
		iterator = chartView.getSegments().iterator();
	}

	@Override
	public void moveFirst() throws JRException
	{
		iterator = chartView.getSegments().iterator();
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
			return lineSegment.getStatistic().getName();
		if( fieldName.equals( "source" ) )
			return lineSegment.getStatistic().getSource();
		if( fieldName.equals( "component" ) )
			return lineSegment.getStatistic().getStatisticVariable().getStatisticHolder().getLabel();
		if( fieldName.equals( "variable" ) )
			return lineSegment.getStatistic().getStatisticVariable().getName();
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

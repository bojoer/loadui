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
package com.eviware.loadui.api.chart;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class ChartModel
{

	public static final int STYLE_BAR = 0;

	public static final int STYLE_LINE = 1;

	private int style = STYLE_LINE;

	private final CustomAbstractRange xRange;

	private final CustomAbstractRange yRange;

	private CustomAbstractRange y2Range;

	private String title = "";

	private int width = 200;

	private int height = 100;

	private final List<ChartListener> chartListenerList = new ArrayList<>();

	private final List<ChartSerie> series = new ArrayList<>();

	private int legendColumns = -1;

	private boolean testRunning = false;

	public ChartModel( CustomAbstractRange xRange, CustomAbstractRange yRange, int width, int height )
	{
		this.xRange = xRange;
		this.yRange = yRange;
		this.width = width;
		this.height = height;
	}

	public ChartModel( CustomAbstractRange xRange, CustomAbstractRange yRange, CustomAbstractRange y2Range, int width,
			int height )
	{
		this.xRange = xRange;
		this.yRange = yRange;
		this.y2Range = y2Range;
		this.width = width;
		this.height = height;
	}

	public ChartModel( CustomAbstractRange xRange, CustomAbstractRange yRange )
	{
		this.xRange = xRange;
		this.yRange = yRange;
	}

	public void addPoint( int serieIndex, double x, double y )
	{
		Point p = new Point( x, y );
		if( serieIndex >= 0 && serieIndex < series.size() && series.get( serieIndex ).isEnabled() )
			firePointAddedToModel( series.get( serieIndex ), p );
	}

	public void clearSerie( String serieName )
	{
		for( ChartSerie s : series )
		{
			if( s.getName().equals( serieName ) )
			{
				fireSerieCleared( s );
				return;
			}
		}
	}

	public void clear()
	{
		for( ChartSerie serie : series )
			fireSerieCleared( serie );

		fireChartCleared();
	}

	public void enableSerie( String serieName, boolean enable )
	{
		for( ChartSerie serie : series )
		{
			if( serieName.equals( serie.getName() ) )
			{
				serie.setEnabled( enable );
				fireSerieEnabled( serie );
				return;
			}
		}
	}

	public int getStyle()
	{
		return style;
	}

	public void setStyle( int style )
	{
		this.style = style;
	}

	public CustomAbstractRange getXRange()
	{
		return xRange;
	}

	public CustomAbstractRange getYRange()
	{
		return yRange;
	}

	public CustomAbstractRange getY2Range()
	{
		return y2Range;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth( int width )
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight( int height )
	{
		this.height = height;
	}

	public List<ChartSerie> getSeries()
	{
		return series;
	}

	public void addSerie( String name, boolean enabled, boolean defaultAxis )
	{
		ChartSerie cs = new ChartSerie( name, enabled, defaultAxis );
		series.add( cs );
		cs.setIndex( series.size() - 1 );
	}

	public int getSerieIndex( String serieName )
	{
		for( int i = 0; i < series.size(); i++ )
			if( series.get( i ).getName().equals( serieName ) )
				return i;

		return -1;
	}

	public ChartSerie getSerie( String serieName )
	{
		for( ChartSerie serie : series )
			if( serieName.equals( serie.getName() ) )
				return serie;

		return null;
	}

	public void addChartListener( ChartListener chartListener )
	{
		chartListenerList.add( chartListener );
	}

	public void removeChartListener( ChartListener chartListener )
	{
		chartListenerList.remove( chartListener );
	}

	private void firePointAddedToModel( ChartSerie cs, Point p )
	{
		for( ChartListener c : chartListenerList )
			c.pointAddedToModel( cs, p );
	}

	private void fireSerieCleared( ChartSerie cs )
	{
		for( ChartListener c : chartListenerList )
			c.serieCleared( cs );
	}

	private void fireChartCleared()
	{
		for( ChartListener c : chartListenerList )
			c.chartCleared();
	}

	private void fireSerieEnabled( ChartSerie cs )
	{
		for( ChartListener c : chartListenerList )
			c.serieEnabled( cs );
	}

	private void fireTestStateChanged( boolean running )
	{
		for( ChartListener c : chartListenerList )
			c.testStateChanged( running );
	}

	public int getLegendColumns()
	{
		if( legendColumns == -1 )
			return getSeries().size();
		else
			return legendColumns;
	}

	public void setLegendColumns( int legendColumns )
	{
		this.legendColumns = legendColumns;
	}

	public boolean isTestRunning()
	{
		return testRunning;
	}

	public void setTestRunning( boolean testRunning )
	{
		if( this.testRunning != testRunning )
		{
			this.testRunning = testRunning;
			fireTestStateChanged( testRunning );
		}
	}

}
/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.fx.dummy;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.eviware.loadui.api.ui.inspector.Inspector;
import com.jidesoft.chart.Chart;
import com.jidesoft.chart.Legend;
import com.jidesoft.chart.annotation.AutoPositionedLabel;
import com.jidesoft.chart.axis.Axis;
import com.jidesoft.chart.axis.TimeAxis;
import com.jidesoft.chart.model.ChartPoint;
import com.jidesoft.chart.model.Chartable;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.range.NumericRange;
import com.jidesoft.range.Range;
import com.jidesoft.range.TimeRange;

//import com.eviware.loadui.impl.model.ComponentItemImpl;

//TODO: Remove this from here and spring configuration!
public class SwingInspector implements Inspector
{
	// private final JButton panel = new JButton( "I'm a cool Swing button!" );
	private JPanel demoPanel;
	private Chart chart;
	private DefaultChartModel chartModel;
	private int timePoint = 0;
	private ComponentListener resizeListener;
	private JPanel controlPanel;
	private ButtonGroup buttonGroup = new ButtonGroup();

	// private java.util.concurrent.ThreadPoolExecutor executor;

	public SwingInspector()
	{
		// this.executor = executor;
		System.out.println( "getting swing inspector panel" );
	}

	@Override
	public String getName()
	{
		return "Swing Inspector";
	}

	@Override
	public Object getPanel()
	{
		if( demoPanel == null )
			createDemo();
		return demoPanel;
	}

	@Override
	public void onHide()
	{
	}

	@Override
	public void onShow()
	{
	}

	private JPanel createDemo()
	{
		chart = new Chart();
		chartModel = new DefaultChartModel();
		demoPanel = new JPanel();
		demoPanel.setPreferredSize( new Dimension( 500, 500 ) );
		FlowLayout f = new FlowLayout( FlowLayout.CENTER, 10, 10 );
		NumericRange yRange = new NumericRange( 0, 5000 );
		long now = System.currentTimeMillis();
		TimeRange xRange = new TimeRange( now, now );
		final Axis xAxis = new TimeAxis( xRange );
		final Axis yAxis = new Axis( yRange );
		xAxis.setLabel( new AutoPositionedLabel( "Time" ) );
		yAxis.setLabel( new AutoPositionedLabel( "Free Memory" ) );

		chart.addModel( chartModel );
		chart.addModel( createRunningThreadsModel() );
		GridLayout g = new GridLayout( 1, 1, 5, 5 );
		chart.addModel( createMaxThreadsModel() );
		// chart.setStyle(model1, new ChartStyle(Color.red, false, true));
		chart.setXAxis( xAxis );
		chart.setYAxis( yAxis );
		chart.setChartBackground( new GradientPaint( 0.0f, 0.0f, Color.gray, 0.0f, 200.0f, Color.black, false ) );
		chart.setTickColor( Color.white );
		chart.setTitle( new AutoPositionedLabel( "Dynamic Chart", Color.yellow.brighter() ) );

		controlPanel = new JPanel();
		final JRadioButton lineRadioButton = new JRadioButton( "Line" );
		final JRadioButton barsRadioButton = new JRadioButton( "Bars" );
		controlPanel.add( barsRadioButton );
		controlPanel.add( lineRadioButton );
		buttonGroup.add( lineRadioButton );
		buttonGroup.add( barsRadioButton );
		lineRadioButton.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				if( lineRadioButton.isSelected() )
				{
					useLineStyle();
				}
			}
		} );
		barsRadioButton.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				if( barsRadioButton.isSelected() )
				{
					useBarStyle();
				}
			}
		} );
		useLineStyle();
		barsRadioButton.setSelected( true );
		Legend l;

		demoPanel.setLayout( new BorderLayout() );
		demoPanel.add( chart, BorderLayout.CENTER );
		// demoPanel.add(controlPanel, BorderLayout.SOUTH);

		// The action listener to be used by a swing timer
		ActionListener addPointListener = new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				addPointToModel( chartModel );
				if( timePoint > 120 )
				{
					chartModel.removePoint( 0 );
				}
				// chart.repaint();
				Range<Double> timeRange = chartModel.getXRange();
				if( timeRange != null )
				{
					long maxX = ( long )( timeRange.minimum() + 60 * 1000.0 ); // A
					// minute's
					// worth
					// of
					// data
					TimeRange xRange = ( TimeRange )xAxis.getRange();
					xRange.setMin( ( long )timeRange.minimum() );
					xRange.setMax( maxX );
					NumericRange modelYRange = ( NumericRange )chartModel.getYRange();
					double range = modelYRange.maximum() - modelYRange.minimum();
					final double extraSpace = 0.15; // = 15%
					// modelYRange.setMin(0);
					// modelYRange.setMax(modelYRange.getMax() + extraSpace * range);
					NumericRange yRange = ( NumericRange )yAxis.getRange();
					yRange.setMin( 0 );
					yRange.setMax( modelYRange.getMax() + extraSpace * range );
					// yAxis.setRange(modelYRange, true);
				}
			}
		};
		Timer timer = new Timer( 500, addPointListener );
		timer.setInitialDelay( 500 );
		timer.start();

		return demoPanel;
	}

	private void useBarStyle()
	{
		if( resizeListener != null )
		{
			demoPanel.removeComponentListener( resizeListener );
		}
		ChartStyle style = new ChartStyle( Color.green, false, false );
		style.setBarsVisible( true );
		style.setBarColor( Color.green );
		style.setBarWidth( 2 );
		style.setLineStroke( new BasicStroke( 3 ) );
		chart.setStyle( chartModel, style );
		Legend legend = new Legend( chart, 3 );
		HashMap<String, String> d;

	}

	private void useLineStyle()
	{
		final ChartStyle style = new ChartStyle( Color.green );
		style.setLineWidth( 2 );
		if( resizeListener != null )
		{
			demoPanel.removeComponentListener( resizeListener );
		}
		resizeListener = new ComponentAdapter()
		{
			@Override
			public void componentResized( ComponentEvent e )
			{
				resizeGradientFill( style );
			}
		};
		resizeGradientFill( style );
		chart.setStyle( chartModel, style );

		demoPanel.addComponentListener( resizeListener );
	}

	private void resizeGradientFill( ChartStyle style )
	{
		Dimension size = demoPanel.getSize();
		style.setLineFill( new GradientPaint( 0f, 0f, new Color( 0, 255, 128, 180 ), 0, ( float )size.getHeight(),
				new Color( 0, 0, 0, 64 ) ) );
	}

	private void addPointToModel( DefaultChartModel model )
	{
		assert SwingUtilities.isEventDispatchThread();
		long now = System.currentTimeMillis();
		double y = Runtime.getRuntime().freeMemory() / 1000000.0;
		ChartPoint p = new ChartPoint( now, y );
		model.addPoint( p );

		// runningThreadsModel.addPoint( new ChartPoint( now,
		// executor.getActiveCount() ));
		// maxThreadsModel.addPoint( new ChartPoint( now,
		// executor.getLargestPoolSize() ));

		timePoint++ ;
	}

	DefaultChartModel runningThreadsModel;

	private DefaultChartModel createRunningThreadsModel()
	{
		runningThreadsModel = new DefaultChartModel();
		return runningThreadsModel;
	}

	DefaultChartModel maxThreadsModel;

	private DefaultChartModel createMaxThreadsModel()
	{
		maxThreadsModel = new DefaultChartModel();
		return maxThreadsModel;
	}

	@Override
	public String getHelpUrl()
	{
		return "http://www.eviware.com";
	}
}

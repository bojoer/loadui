package com.eviware.loadui.util.reporting.datasources.statistics;

import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.store.Execution;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;

public class ChartDataSource extends JRAbstractBeanDataSource
{
	private final Execution execution;
	private final ChartGroup chartGroup;

	private int i = 10;

	public ChartDataSource( Execution execution, ChartGroup chartGroup )
	{
		super( true );

		this.execution = execution;
		this.chartGroup = chartGroup;
	}

	@Override
	public void moveFirst() throws JRException
	{
		i = 10;
	}

	@Override
	public Object getFieldValue( JRField field ) throws JRException
	{
		if( field.getName().equals( "chart" ) )
			return new BufferedImage( 555, 100 + ( int )( Math.random() * 100 ), BufferedImage.TYPE_3BYTE_BGR );
		if( field.getName().equals( "chartData" ) )
		{
			DefaultXYDataset dataset = new DefaultXYDataset();
			double[][] points1 = new double[][] { new double[] { 0, 0 }, new double[] { 1, 1 }, new double[] { 2, 2 } };
			dataset.addSeries( "Test 1", points1 );
			double[][] points2 = new double[][] { new double[] { 0, 1 }, new double[] { 1, 0 }, new double[] { 2, 2 } };
			dataset.addSeries( "Test 2", points2 );

			return dataset;
		}

		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		return --i > 0;
	}
}

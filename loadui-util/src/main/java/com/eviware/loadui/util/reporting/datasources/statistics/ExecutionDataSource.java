package com.eviware.loadui.util.reporting.datasources.statistics;

import java.awt.Image;
import java.util.Map;

import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.store.Execution;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class ExecutionDataSource extends JRAbstractBeanDataSource
{
	private final Execution execution;
	private final StatisticPage page;
	private final Map<Object, Image> charts;
	private boolean next = true;

	public ExecutionDataSource( Execution execution, StatisticPage page, Map<Object, Image> charts )
	{
		super( true );
		this.execution = execution;
		this.page = page;
		this.charts = charts;
	}

	@Override
	public void moveFirst() throws JRException
	{
		next = true;
	}

	@Override
	public Object getFieldValue( JRField field ) throws JRException
	{
		if( field.getName().equals( "chartGroup" ) )
			return new ChartGroupDataSource( execution, page.getChildAt( 0 ), charts );
		return null;
	}

	@Override
	public boolean next() throws JRException
	{
		if( next )
		{
			next = false;
			return true;
		}
		return false;
	}
}

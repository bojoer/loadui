package com.eviware.loadui.util.reporting.datasources.statistics;

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
			return chartGroup.getTitle();
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
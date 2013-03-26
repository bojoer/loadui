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
package com.eviware.loadui.impl.statistics.model.chart.line;

import java.beans.PropertyChangeEvent;
import java.util.Collection;

import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;

public class SourceLineSegment implements LineSegment
{
	private final ChartGroup chartGroup;
	private final ChartLineSegment parent;
	private final String source;
	private final EventForwarder eventForwarder = new EventForwarder();

	private Statistic<Number> statistic;

	public SourceLineSegment( ChartLineSegment parent, String source )
	{
		this.parent = parent;
		this.source = source;
		chartGroup = parent.getChart().getChartGroup();
		chartGroup.addEventListener( PropertyChangeEvent.class, eventForwarder );
	}

	@Override
	public StatisticHolder getStatisticHolder()
	{
		return parent.getStatisticHolder();
	}

	@Override
	public String getSource()
	{
		return source;
	}

	@Override
	public String getVariableName()
	{
		return parent.getVariableName();
	}

	@Override
	public String getStatisticName()
	{
		return parent.getStatisticName();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Statistic<Number> getStatistic()
	{
		if( statistic == null )
		{
			statistic = ( Statistic<Number> )getStatisticHolder().getStatisticVariable( parent.getVariableName() )
					.getStatistic( parent.getStatisticName(), source );
		}
		return statistic;
	}

	@Override
	public void setAttribute( String key, String value )
	{
		parent.setAttribute( key, value );
	}

	@Override
	public String getAttribute( String key, String defaultValue )
	{
		return parent.getAttribute( key, defaultValue );
	}

	@Override
	public void removeAttribute( String key )
	{
		parent.removeAttribute( key );
	}

	@Override
	public Collection<String> getAttributes()
	{
		return parent.getAttributes();
	}

	@Override
	public boolean isRemoved()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return parent.toString();
	}

	private static class ForwardedPropertyChangeEvent extends PropertyChangeEvent
	{
		private static final long serialVersionUID = -286054404344955979L;

		public ForwardedPropertyChangeEvent( Object source, String propertyName, Object oldValue, Object newValue )
		{
			super( source, propertyName, oldValue, newValue );
		}
	}

	private class EventForwarder implements WeakEventHandler<PropertyChangeEvent>
	{
		@Override
		public void handleEvent( PropertyChangeEvent event )
		{
			if( !( event instanceof ForwardedPropertyChangeEvent ) )
			{
				if( event.getSource() == parent )
				{
					chartGroup.fireEvent( new ForwardedPropertyChangeEvent( SourceLineSegment.this, event.getPropertyName(),
							event.getOldValue(), event.getNewValue() ) );
				}
				else if( event.getSource() == SourceLineSegment.this )
				{
					chartGroup.fireEvent( new ForwardedPropertyChangeEvent( parent, event.getPropertyName(), event
							.getOldValue(), event.getNewValue() ) );
				}
			}
		}
	}

	@Override
	public String getId()
	{
		return parent.getId();
	}

}

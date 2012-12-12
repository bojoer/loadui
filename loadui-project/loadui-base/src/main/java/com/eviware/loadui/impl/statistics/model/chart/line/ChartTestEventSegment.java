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
package com.eviware.loadui.impl.statistics.model.chart.line;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.model.chart.line.TestEventSegment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEvent.Entry;
import com.eviware.loadui.api.testevents.TestEventSourceDescriptor;
import com.eviware.loadui.api.testevents.TestEventTypeDescriptor;
import com.eviware.loadui.util.StringUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ChartTestEventSegment extends AbstractChartSegment implements TestEventSegment.Removable
{
	private static final TestEventSourceDescriptor[] EMPTY_DESCRIPTOR_ARRAY = new TestEventSourceDescriptor[0];

	protected static final Logger log = LoggerFactory.getLogger( ChartTestEventSegment.class );

	private final Function<Entry, TestEvent> getValues = new Function<Entry, TestEvent>()
	{
		@Override
		public TestEvent apply( Entry input )
		{
			return input.getTestEvent();
		}
	};

	private final Predicate<TestEventSourceDescriptor> sourceFilter = new Predicate<TestEventSourceDescriptor>()
	{
		@Override
		public boolean apply( TestEventSourceDescriptor input )
		{
			return sourceLabel.equals( input.getLabel() );
		}
	};

	private final Predicate<TestEventTypeDescriptor> typeFilter = new Predicate<TestEventTypeDescriptor>()
	{
		@Override
		public boolean apply( TestEventTypeDescriptor input )
		{
			return typeLabel.equals( input.getLabel() );
		}
	};

	private final String typeLabel;
	private final String sourceLabel;

	public ChartTestEventSegment( ChartLineChartView chart, String typeLabel, String sourceLabel )
	{
		super( chart, StringUtils.serialize( "TEST_EVENT", typeLabel, sourceLabel ) );

		this.typeLabel = typeLabel;
		this.sourceLabel = sourceLabel;
	}

	@Override
	public String getTypeLabel()
	{
		return typeLabel;
	}

	@Override
	public String getSourceLabel()
	{
		return sourceLabel;
	}

	@Override
	public Iterable<TestEvent> getTestEventsInRange( Execution execution, long startTime, long endTime,
			int interpolationLevel )
	{
		TestEventSourceDescriptor[] descriptors = getDescriptors( execution );

		return descriptors.length == 0 ? ImmutableList.<TestEvent> of() : Iterables.transform(
				execution.getTestEventRange( startTime, endTime, interpolationLevel, descriptors ), getValues );
	}

	@Override
	public void remove()
	{
		super.remove();
		getChart().delete();
	}

	private TestEventSourceDescriptor[] getDescriptors( Execution execution )
	{
		TestEventTypeDescriptor descriptor = Iterables.find( execution.getEventTypes(), typeFilter, null );
		return descriptor == null ? EMPTY_DESCRIPTOR_ARRAY : Lists.newArrayList(
				Iterables.filter( descriptor.getTestEventSources(), sourceFilter ) ).toArray( EMPTY_DESCRIPTOR_ARRAY );
	}
}

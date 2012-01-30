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
package com.eviware.loadui.impl.statistics.store;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.service.importer.OsgiServiceLifecycleListener;

import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestExecutionTask;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.traits.Releasable;
import com.google.common.collect.Maps;

public class TestEventInterpolator implements Releasable, OsgiServiceLifecycleListener
{
	private final static long[] aggregateIntervals = { 6000, // 6 seconds
			240000, // 4 minutes
			7200000, // 2 hours
			43200000 // 12 hours
	};

	protected static final Logger log = LoggerFactory.getLogger( TestEventInterpolator.class );

	private final StartStopTask task = new StartStopTask();
	private final ExecutionManagerImpl manager;
	private final Map<InterpolationKey, InterpolationLevel> interpolators = Maps.newConcurrentMap();
	private TestRunner testRunner;

	public TestEventInterpolator( ExecutionManagerImpl manager )
	{
		this.manager = manager;
	}

	@Override
	public void bind( Object object, @SuppressWarnings( "rawtypes" ) Map properties ) throws Exception
	{
		setTestRunner( ( TestRunner )object );
	}

	@Override
	public void unbind( Object object, @SuppressWarnings( "rawtypes" ) Map properties ) throws Exception
	{
		testRunner = null;
	}

	public void interpolate( String typeLabel, TestEvent.Source<?> source, TestEvent testEvent )
	{
		InterpolationKey key = new InterpolationKey( testEvent.getType(), source );

		InterpolationLevel interpolator = interpolators.get( key );
		if( interpolator == null )
		{
			interpolators.put( key, interpolator = new InterpolationLevel( typeLabel, source, testEvent, 0 ) );
		}

		interpolator.add( testEvent );
	}

	public void setTestRunner( TestRunner testRunner )
	{
		this.testRunner = testRunner;
		testRunner.registerTask( task, Phase.PRE_START, Phase.POST_STOP );
	}

	@Override
	public void release()
	{
		if( testRunner != null )
		{
			testRunner.unregisterTask( task, Phase.values() );
		}
		interpolators.clear();
	}

	private static class InterpolationKey
	{
		private final Class<? extends TestEvent> type;
		private final TestEvent.Source<? extends TestEvent> source;

		private InterpolationKey( Class<? extends TestEvent> type, TestEvent.Source<? extends TestEvent> source )
		{
			this.type = type;
			this.source = source;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ( ( source == null ) ? 0 : source.hashCode() );
			result = prime * result + ( ( type == null ) ? 0 : type.hashCode() );
			return result;
		}

		@Override
		public boolean equals( Object obj )
		{
			if( this == obj )
				return true;
			if( obj == null )
				return false;
			if( getClass() != obj.getClass() )
				return false;
			InterpolationKey other = ( InterpolationKey )obj;
			if( source == null )
			{
				if( other.source != null )
					return false;
			}
			else if( !source.equals( other.source ) )
				return false;
			if( type == null )
			{
				if( other.type != null )
					return false;
			}
			else if( !type.equals( other.type ) )
				return false;
			return true;
		}

	}

	private class StartStopTask implements TestExecutionTask
	{
		@Override
		public void invoke( TestExecution execution, Phase phase )
		{
			switch( phase )
			{
			case PRE_START :
				interpolators.clear();
				break;
			case POST_STOP :
				log.debug( "!!!!flushing interpolations" );
				for( InterpolationLevel interpolator : interpolators.values() )
				{
					interpolator.flushAll();
				}
				interpolators.clear();
				break;
			}
		}
	}

	private class InterpolationLevel
	{
		private final int level;
		private final InterpolationLevel nextLevel;
		private final String typeLabel;
		private final TestEvent.Source<? extends TestEvent> source;
		private final Class<? extends TestEvent> type;

		private long lastFlushTime;
		private long timestampTotal = 0;
		private long eventCount = 0;

		public InterpolationLevel( String typeLabel, TestEvent.Source<? extends TestEvent> source,
				TestEvent initialTestEvent, int level )
		{
			this.level = level;
			this.typeLabel = typeLabel;
			this.source = source;
			nextLevel = level >= aggregateIntervals.length - 1 ? null : new InterpolationLevel( typeLabel, source,
					initialTestEvent, level + 1 );

			lastFlushTime = initialTestEvent.getTimestamp();
			type = initialTestEvent.getType();
			add( initialTestEvent );
		}

		public void add( TestEvent testEvent )
		{
			while( lastFlushTime + aggregateIntervals[level] < testEvent.getTimestamp() )
			{
				flush();
				lastFlushTime += aggregateIntervals[level];
			}

			timestampTotal += testEvent.getTimestamp();
			eventCount++ ;

			if( nextLevel != null )
			{
				nextLevel.add( testEvent );
			}
		}

		public void flush()
		{
			if( eventCount > 0 )
			{
				InterpolatedTestEvent testEvent = InterpolatedTestEvent.createEvent( type, timestampTotal / eventCount,
						eventCount > 1 );

				manager.writeTestEvent( typeLabel, source, testEvent.getTimestamp(),
						InterpolatedTestEvent.dataFor( testEvent ), level + 1 );
				timestampTotal = 0;
				eventCount = 0;
			}
		}

		public void flushAll()
		{
			flush();
			if( nextLevel != null )
			{
				nextLevel.flushAll();
			}
		}
	}
}

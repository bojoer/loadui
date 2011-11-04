package com.eviware.loadui.fx.eventlog;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEvent.Entry;
import com.eviware.loadui.util.FormattingUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public class EventLogTableModel extends AbstractTableModel
{
	public static final Logger log = LoggerFactory.getLogger( EventLogTableModel.class );

	public static EventLogTableModel create( Execution execution )
	{
		return execution != null ? new EventLogTableModel( execution ) : new NullEventLogTableModel();
	}

	private static final long serialVersionUID = -4240376247009167267L;

	private final Cache<Integer, TestEvent.Entry> entryCache = CacheBuilder.newBuilder().maximumSize( 128 )
			.build( new CacheLoader<Integer, TestEvent.Entry>()
			{
				private Iterator<TestEvent.Entry> iterator;
				private boolean iteratorReversed = false;
				private long lastKey = Long.MIN_VALUE;
				private long nextIndex;

				@Override
				public Entry load( Integer key ) throws Exception
				{
					try
					{
						boolean reverse = key < lastKey;
						long distance = Math.abs( key - lastKey );
						boolean keepIterator = ( iteratorReversed == reverse ) && distance < 64 && iterator != null;

						if( !keepIterator )
						{
							//							log.debug( "Not keeping iterator. lastKey:{}, key:{}, reverse:{}", new Object[] { lastKey, key,
							//									reverse } );

							int pos = reverse ? Math.min( key + 64, getRowCount() - 1 ) : key;

							iterator = execution.getTestEvents( pos, reverse ).iterator();
							iteratorReversed = reverse;
							nextIndex = pos;
						}

						while( nextIndex != key && iterator.hasNext() )
						{
							//TODO: With Guava 10.0.1 put entry into the cache.
							//entryCache.asMap().put( nextIndex, iterator.next() );
							iterator.next();
							nextIndex += reverse ? -1 : 1;
						}

						if( !( nextIndex == key && iterator.hasNext() ) )
						{
							//log.debug( "iterator exhausted! nextIndex: {}, key:{}", nextIndex, key );
							//TODO: This shouldn't happen when we populate the cache manually,
							//but it might happen now if we try to get an element which was not available when the iterator was created.
							iterator = execution.getTestEvents( key, reverse ).iterator();
						}

						nextIndex = key + ( reverse ? -1 : 1 );
						return iterator.next();
					}
					finally
					{
						lastKey = key;
					}
				}
			} );

	private final Execution execution;

	private int rowCount;

	private EventLogTableModel( Execution execution )
	{
		this.execution = execution;
		rowCount = execution != null ? execution.getTestEventCount() : 0;
	}

	@Override
	public int getRowCount()
	{
		return rowCount;
	}

	@Override
	public int getColumnCount()
	{
		return 4;
	}

	@Override
	public String getColumnName( int column )
	{
		switch( column )
		{
		case 0 :
			return "Time";
		case 1 :
			return "Event Type";
		case 2 :
			return "Event Source";
		case 3 :
			return "Description";
		}
		return null;
	}

	@Override
	public Object getValueAt( int rowIndex, int columnIndex )
	{
		try
		{
			TestEvent.Entry testEventEntry = entryCache.get( rowIndex );
			switch( columnIndex )
			{
			case 0 :
				return FormattingUtils.formatTimeMillis( testEventEntry.getTestEvent().getTimestamp() );
			case 1 :
				return testEventEntry.getTypeLabel();
			case 2 :
				return testEventEntry.getSourceLabel();
			case 3 :
				return testEventEntry.getTestEvent().toString();
			default :
				return null;
			}
		}
		catch( ExecutionException e )
		{
			return null;
		}
	}

	public void appendRow( TestEvent.Entry testEventEntry )
	{
		//TODO: In Guava 10.0.1, place testEventEntry into the cache
		//entryCache.asMap().put( rowCount, testEventEntry );
		fireTableRowsInserted( rowCount, rowCount++ );
	}

	/**
	 * Empty TableModel with the correct columns.
	 * 
	 * @author dain.nilsson
	 */
	private static class NullEventLogTableModel extends EventLogTableModel
	{
		private static final long serialVersionUID = 1901295390463684070L;

		private NullEventLogTableModel()
		{
			super( null );
		}

		@Override
		public int getRowCount()
		{
			return 0;
		}

		@Override
		public void appendRow( TestEvent.Entry testEventEntry )
		{
		}
	}
}
package com.eviware.loadui.fx.eventlog;

import javax.swing.table.AbstractTableModel;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.util.FormattingUtils;
import com.google.common.collect.Iterables;

public class EventLogTableModel extends AbstractTableModel
{
	public static EventLogTableModel create( Execution execution )
	{
		return execution != null ? new EventLogTableModel( execution ) : new NullEventLogTableModel();
	}

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
		TestEvent.Entry testEventEntry = Iterables.getFirst( execution.getTestEvents( rowIndex, false ), null );
		if( testEventEntry == null )
			return null;

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

	public void appendRow( TestEvent.Entry testEventEntry )
	{
		fireTableRowsInserted( rowCount, rowCount++ );
	}

	/**
	 * Empty TableModel with the correct columns.
	 * 
	 * @author dain.nilsson
	 */
	private static class NullEventLogTableModel extends EventLogTableModel
	{
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
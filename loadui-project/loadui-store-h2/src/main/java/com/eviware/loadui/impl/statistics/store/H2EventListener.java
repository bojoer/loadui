package com.eviware.loadui.impl.statistics.store;

import java.sql.SQLException;

import org.h2.api.DatabaseEventListener;

public class H2EventListener implements DatabaseEventListener
{
	@Override
	public void init( String url )
	{
	}

	@Override
	public void opened()
	{
	}

	@Override
	public void diskSpaceIsLow()
	{
		ExecutionManagerImpl.signalLowDiskspace();
	}

	@Override
	public void exceptionThrown( SQLException e, String sql )
	{
	}

	@Override
	public void setProgress( int state, String name, int x, int max )
	{
	}

	@Override
	public void closingDatabase()
	{
	}
}

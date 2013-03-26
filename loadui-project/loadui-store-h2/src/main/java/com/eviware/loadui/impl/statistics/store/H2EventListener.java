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

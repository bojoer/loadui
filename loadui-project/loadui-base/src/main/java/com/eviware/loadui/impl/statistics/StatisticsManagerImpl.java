/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.impl.statistics;

import java.util.Collection;
import java.util.EventObject;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.util.events.EventSupport;

/**
 * Implementation for the StatisticsManager.
 * 
 * @author dain.nilsson
 */
public class StatisticsManagerImpl implements StatisticsManager
{
	private final EventSupport eventSupport = new EventSupport();

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<T> listener )
	{
		eventSupport.addEventListener( type, listener );
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<T> listener )
	{
		eventSupport.removeEventListener( type, listener );
	}

	@Override
	public void clearEventListeners()
	{
		eventSupport.clearEventListeners();
	}

	@Override
	public void fireEvent( EventObject event )
	{
		eventSupport.fireEvent( event );
	}

	@Override
	public void registerStatisticHolder( StatisticHolder statisticHolder )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deregisterStatisticHolder( StatisticHolder statisticHolder )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<StatisticHolder> getStatisticHolders()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getMinimumWriteDelay()
	{
		return 1000;
	}
}
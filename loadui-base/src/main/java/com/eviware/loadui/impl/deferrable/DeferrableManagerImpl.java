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
package com.eviware.loadui.impl.deferrable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.eviware.loadui.api.deferrable.Deferrable;
import com.eviware.loadui.api.deferrable.DeferrableManager;

public class DeferrableManagerImpl implements DeferrableManager, Runnable
{
	private final Set<Deferrable> deferrables = new HashSet<Deferrable>();
	private final ScheduledExecutorService executor;
	private final boolean stopExecutor;
	private ScheduledFuture<?> future;

	public DeferrableManagerImpl( ScheduledExecutorService executorService, long delay, TimeUnit unit )
	{
		if( executorService == null )
		{
			executor = Executors.newSingleThreadScheduledExecutor();
			stopExecutor = true;
		}
		else
		{
			executor = executorService;
			stopExecutor = false;
		}

		future = executor.scheduleWithFixedDelay( this, delay, delay, unit );
	}

	public void destroy()
	{
		future.cancel( false );
		if( stopExecutor )
			executor.shutdown();
	}

	@Override
	public void defer( Deferrable deferrable )
	{
		try
		{
			if( !deferrable.run() )
			{
				synchronized( deferrables )
				{
					deferrables.add( deferrable );
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			System.exit( 0 );
		}
	}

	@Override
	public void cancel( Deferrable deferrable )
	{
		synchronized( deferrables )
		{
			deferrables.remove( deferrable );
		}
	}

	@Override
	public Collection<Deferrable> getDeferrables()
	{
		return Collections.unmodifiableCollection( deferrables );
	}

	@Override
	public void resolve()
	{
		synchronized( deferrables )
		{
			boolean resolved = true;
			while( resolved )
			{
				resolved = false;
				for( Iterator<Deferrable> iter = deferrables.iterator(); iter.hasNext(); )
				{
					try
					{
						if( iter.next().run() )
						{
							iter.remove();
							resolved = true;
						}
					}
					catch( Exception e )
					{
						e.printStackTrace();
						System.exit( 0 );
					}
				}
			}
		}
	}

	@Override
	public void run()
	{
		resolve();
	}
}

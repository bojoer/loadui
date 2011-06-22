/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.api.ui.charts.tools;

import java.util.Random;
import javax.swing.SwingUtilities;

import com.eviware.loadui.api.ui.charts.UpdateListener;

/**
 * 
 * @author robert
 */

public class SystemDataCollector implements Runnable
{

	private static final Random random = new Random();
	private UpdateListener updateListener = null;
	private Thread thread = null;
	private boolean stop = false;

	private final Runtime runtime = Runtime.getRuntime();

	public SystemDataCollector( UpdateListener ul )
	{
		thread = new Thread( this );
		this.updateListener = ul;
	}

	public void start()
	{
		thread.start();
	}

	public void stop()
	{
		stop = true;
	}

	@Override
	public void run()
	{

		while( !stop )
		{

			final int free = ( int )Math.abs( runtime.freeMemory() * 100 / runtime.totalMemory() );
			final int bussy = 100 - free;

			final int timeStep = 2;
			/**
			 * Update listeners via Event Dispatch Thread
			 */
			SwingUtilities.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					updateListener.update( timeStep, free, bussy, 0 );
				}
			} );

			try
			{
				Thread.sleep( 500 );
			}
			catch( InterruptedException e )
			{
			}
		}
	}
}

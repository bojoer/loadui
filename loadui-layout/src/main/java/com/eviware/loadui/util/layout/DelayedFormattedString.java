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
package com.eviware.loadui.util.layout;

import java.util.Timer;
import java.util.TimerTask;

public class DelayedFormattedString extends FormattedString
{
	private int delay;
	private static Timer timer = new Timer();
	private UpdateTask updateTask = new UpdateTask();

	public DelayedFormattedString( String pattern, Object... args )
	{
		this( pattern, 1000, args );
	}

	public DelayedFormattedString( String pattern, int delay, Object... args )
	{
		super( pattern, args );
		this.delay = delay;

		update();

		timer.schedule( updateTask, delay, delay );
	}

	@Override
	public void release()
	{
		super.release();
		updateTask.cancel();
	}

	private class UpdateTask extends TimerTask
	{
		@Override
		public void run()
		{
			update();
		}
	}
}

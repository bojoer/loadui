package com.eviware.loadui.util.timers;

import java.util.TimerTask;

public class LouadUIGarbageCollectorTimer extends TimerTask
{

	@Override
	public void run()
	{
			System.gc();
	}

}

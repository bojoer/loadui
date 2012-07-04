package com.eviware.loadui.components.soapui;

import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunnable;

class SoapUILoadTestRunner implements LoadTestRunner
{
	/**
	 * 
	 */
	private final SoapUISamplerComponent soapUISamplerComponent;

	/**
	 * @param soapUISamplerComponent
	 */
	SoapUILoadTestRunner( SoapUISamplerComponent soapUISamplerComponent )
	{
		this.soapUISamplerComponent = soapUISamplerComponent;
	}

	@Override
	public LoadTest getLoadTest()
	{
		return this.soapUISamplerComponent.soapuiLoadTest;
	}

	@Override
	public float getProgress()
	{
		return 0;
	}

	@Override
	public int getRunningThreadCount()
	{
		return this.soapUISamplerComponent.getCurrentlyRunning();
	}

	@Override
	public boolean hasStopped()
	{
		return !isRunning();
	}

	@Override
	public void cancel( String reason )
	{
	}

	@Override
	public void fail( String reason )
	{
	}

	@Override
	public String getReason()
	{
		return null;
	}

	@Override
	public TestRunContext getRunContext()
	{
		return null;
	}

	@Override
	public long getStartTime()
	{
		return 0;
	}

	@Override
	public Status getStatus()
	{
		return null;
	}

	@Override
	public TestRunnable getTestRunnable()
	{
		return null;
	}

	@Override
	public long getTimeTaken()
	{
		return 0;
	}

	@Override
	public void start( boolean async )
	{
	}

	@Override
	public Status waitUntilFinished()
	{
		return null;
	}

	@Override
	public boolean isRunning()
	{
		return soapUISamplerComponent.isOnRunningCanvas();
	}
}
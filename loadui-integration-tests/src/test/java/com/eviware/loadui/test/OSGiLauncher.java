package com.eviware.loadui.test;

import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import com.eviware.loadui.launcher.LoadUILauncher;

public class OSGiLauncher extends LoadUILauncher
{
	public OSGiLauncher( String[] args )
	{
		super( args );
	}

	public Properties getConfig()
	{
		return configProps;
	}

	@Override
	public void init()
	{
		super.init();
	}

	@Override
	public void start()
	{
		super.start();
	}

	public BundleContext getBundleContext()
	{
		return framework.getBundleContext();
	}

	public void stop() throws BundleException
	{
		framework.stop();
	}
}

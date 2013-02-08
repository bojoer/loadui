package com.eviware.loadui.cmd;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.springframework.osgi.context.BundleContextAware;

public class LoaduiBundleContextAware implements BundleContextAware
{

	private BundleContext context;

	@Override
	public void setBundleContext( BundleContext bundleContext )
	{
		this.context = bundleContext;
	}

	public void stopFramework( Integer... optionalCode ) throws BundleException
	{
		context.getBundle( 0 ).stop();
		int code = optionalCode == null || optionalCode.length == 0 ? 0 : optionalCode[0];
		System.exit( code );
	}

}

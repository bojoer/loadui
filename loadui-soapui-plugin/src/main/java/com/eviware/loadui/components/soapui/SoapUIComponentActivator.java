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
package com.eviware.loadui.components.soapui;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.component.categories.MiscCategory;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.ui.ApplicationState;
import com.eviware.loadui.api.ui.WindowController;
import com.eviware.loadui.integration.CajoServer;
import com.eviware.loadui.integration.LoadUIIntegrator;
import com.eviware.loadui.util.soapui.CajoClient;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUICore;
import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.SoapUIPro;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.http.ProxyUtils;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.support.UISupport;

public final class SoapUIComponentActivator implements BundleActivator
{
	private BundleContext bundleContext;
	private LoadUIIntegrator loadUIIntegrator;
	private SoapUIBehaviorProvider provider;

	private static final Logger log = LoggerFactory.getLogger( SoapUIComponentActivator.class );

	@Override
	public void start( BundleContext context ) throws Exception
	{
		SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();

		bundleContext = context;
		loadUIIntegrator = LoadUIIntegrator.getInstance();
		final ComponentDescriptor componentDescriptor = new ComponentDescriptor( SoapUISamplerComponent.TYPE,
				RunnerCategory.CATEGORY, "soapUI Runner", "Runs a soapUI TestCase.", getClass().getResource(
						"/images/SoapuiRunner.png" ).toURI() );

		final ComponentDescriptor mockServiceDescriptor = new ComponentDescriptor( MockServiceComponent.TYPE,
				MiscCategory.CATEGORY, "soapUI MockService", "Runs a soapUI MockService.", getClass().getResource(
						"/images/SoapuiMockService.png" ).toURI() );

		context.addServiceListener( new ServiceListener()
		{
			@Override
			public void serviceChanged( ServiceEvent event )
			{
				switch( event.getType() )
				{
				case ServiceEvent.REGISTERED :
					ComponentRegistry registry = ( ComponentRegistry )bundleContext.getService( event.getServiceReference() );
					provider = new SoapUIBehaviorProvider();
					provider.setRegistry( registry );
					loadUIIntegrator.setComponentRegistry( registry );

					registry.registerDescriptor( componentDescriptor, provider );
					registry.registerDescriptor( mockServiceDescriptor, provider );
				}
			}
		}, "(objectclass=" + ComponentRegistry.class.getName() + ")" );

		context.addServiceListener( new ServiceListener()
		{
			@Override
			public void serviceChanged( ServiceEvent event )
			{
				switch( event.getType() )
				{
				case ServiceEvent.REGISTERED :
					WindowController windowControllerRegistry = ( WindowController )bundleContext.getService( event
							.getServiceReference() );
					loadUIIntegrator.setWindowController( windowControllerRegistry );

				}
			}
		}, "(objectclass=" + WindowController.class.getName() + ")" );

		context.addServiceListener( new ServiceListener()
		{
			@Override
			public void serviceChanged( ServiceEvent event )
			{
				switch( event.getType() )
				{
				case ServiceEvent.REGISTERED :
					ApplicationState appStateRegistry = ( ApplicationState )bundleContext.getService( event
							.getServiceReference() );
					loadUIIntegrator.setApplicationState( appStateRegistry );

				}
			}
		}, "(objectclass=" + ApplicationState.class.getName() + ")" );

		context.addServiceListener( new ServiceListener()
		{
			@Override
			public void serviceChanged( ServiceEvent event )
			{
				switch( event.getType() )
				{
				case ServiceEvent.REGISTERED :
					WorkspaceProvider workspaceProviderRegistry = ( WorkspaceProvider )bundleContext.getService( event
							.getServiceReference() );
					loadUIIntegrator.setWorkspaceProvider( workspaceProviderRegistry );
					loadUIIntegrator.setComponentDescriptor( componentDescriptor );
					loadUIIntegrator.setMockServiceDescriptor( mockServiceDescriptor );
					CajoServer cajoServer = CajoServer.getInstance();
					cajoServer.setLoadUILuncher( loadUIIntegrator );
					Thread cajoThread = new Thread( cajoServer, "CajoServer" );
					cajoThread.setDaemon( true );
					cajoThread.start();
					CajoClient.getInstance().setWorkspaceProviderRegistry( workspaceProviderRegistry );
					// CajoClient.getInstance().setPathToSoapUIBat(
					// workspaceProviderRegistry.getWorkspace().getProperty(
					// WorkspaceItem.SOAPUI_CAJO_PORT_PROPERTY )
					// .getStringValue() );

				}
			}
		}, "(objectclass=" + WorkspaceProvider.class.getName() + ")" );

		try
		{
			String logFolder = System.getProperty( "loadui.home" ) + File.separatorChar + "logs";
			File file = new File( logFolder );
			if( !file.exists() )
				if( !file.mkdirs() )
					log.error( "Unable to create directory: {}", file.getAbsolutePath() );

			if( !LoadUI.isController() )
			{
				if( System.getProperty( "soapui.scripting.library" ) == null )
					System.setProperty( "soapui.scripting.library", new File( System.getProperty( LoadUI.LOADUI_HOME ),
							"custom-soapui-scripts" ).getAbsolutePath() );
			}

			System.setProperty( "soapui.logroot", logFolder );
			SoapUI.setSoapUICore( new SoapUIPro.SoapUIProCore( true, null ), true );
			ProxyUtils.setProxyEnabled( SoapUI.getSettings().getBoolean( ProxySettings.ENABLE_PROXY ) );

			if( LoadUI.isController() )
				UISupport.setMainFrame( null );

			SchemaUtils.getExcludedTypes();
		}
		finally
		{
			state.restore();
		}
	}

	@Override
	public void stop( BundleContext context ) throws Exception
	{
		if( provider != null )
			provider.destroy();
	}

	public SoapUIBehaviorProvider getProvider()
	{
		return provider;
	}

	public static File loadSettings( File settingsFile )
	{
		try
		{
			if( settingsFile != null && settingsFile.exists() )
			{
				SoapUI.getSoapUICore().importSettings( settingsFile );
			}
			else
			{
				settingsFile = new File( System.getProperty( "user.home" ) + File.separator
						+ SoapUICore.DEFAULT_SETTINGS_FILE );
				if( settingsFile.exists() )
				{
					SoapUI.getSoapUICore().importSettings( settingsFile );
				}
			}
			Settings settings = SoapUI.getSettings();
			settings.reloadSettings();

			ProxyUtils.setProxyEnabled( SoapUI.getSettings().getBoolean( ProxySettings.ENABLE_PROXY ) );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		return settingsFile;
	}

	public static String findRelativePath( File basepath, File path )
	{
		if( path != null )
			return PathUtils.relativize( path.getPath(), basepath.getPath() );

		return "";
	}
}

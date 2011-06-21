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
package com.eviware.loadui.impl.model;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.core.convert.ConversionService;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.config.LoaduiWorkspaceDocumentConfig;
import com.eviware.loadui.impl.addressable.AddressableRegistryImpl;
import com.eviware.loadui.util.BeanInjector;

public class WorkspaceProviderImplTest
{
	@Before
	public void setup()
	{
		ConversionService csrv = mock( ConversionService.class );
		BundleContext bundleContext = mock( BundleContext.class );

		ServiceReference arMock = mock( ServiceReference.class );
		when( bundleContext.getServiceReference( AddressableRegistry.class.getName() ) ).thenReturn( arMock );
		when( bundleContext.getService( arMock ) ).thenReturn( new AddressableRegistryImpl() );

		ServiceReference csMock = mock( ServiceReference.class );
		when( bundleContext.getServiceReference( ConversionService.class.getName() ) ).thenReturn( csMock );
		when( bundleContext.getService( csMock ) ).thenReturn( csrv );

		ServiceReference sesMock = mock( ServiceReference.class );
		when( bundleContext.getServiceReference( ScheduledExecutorService.class.getName() ) ).thenReturn( sesMock );
		when( bundleContext.getService( sesMock ) ).thenReturn( Executors.newSingleThreadScheduledExecutor() );

		BeanInjector.setBundleContext( bundleContext );
	}

	@Test
	public void shouldCreateWorkspace() throws IOException
	{
		File tmp = File.createTempFile( "tmp", ".xml" );

		LoaduiWorkspaceDocumentConfig config = LoaduiWorkspaceDocumentConfig.Factory.newInstance();
		config.addNewLoaduiWorkspace();
		config.save( tmp );

		WorkspaceProvider provider = new WorkspaceProviderImpl();

		WorkspaceItem workspace = provider.loadWorkspace( tmp );

		assertThat( workspace, notNullValue() );
	}
}

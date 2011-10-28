/*
 * Copyright 2011 SmartBear Software
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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.addon.AddonRegistry;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.config.LoaduiWorkspaceDocumentConfig;
import com.eviware.loadui.impl.addressable.AddressableRegistryImpl;
import com.eviware.loadui.util.test.BeanInjectorMocker;

public class WorkspaceProviderImplTest
{
	@Before
	public void setup()
	{
		new BeanInjectorMocker().put( AddressableRegistry.class, new AddressableRegistryImpl() ).put(
				ScheduledExecutorService.class, Executors.newSingleThreadScheduledExecutor() );
	}

	@Test
	public void shouldCreateWorkspace() throws IOException
	{
		File tmp = File.createTempFile( "tmp", ".xml" );

		LoaduiWorkspaceDocumentConfig config = LoaduiWorkspaceDocumentConfig.Factory.newInstance();
		config.addNewLoaduiWorkspace();
		config.save( tmp );

		WorkspaceProvider provider = new WorkspaceProviderImpl( mock( AddonRegistry.class ) );

		WorkspaceItem workspace = provider.loadWorkspace( tmp );

		assertThat( workspace, notNullValue() );
	}
}

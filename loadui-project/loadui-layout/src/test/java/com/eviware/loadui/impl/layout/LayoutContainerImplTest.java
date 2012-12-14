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
package com.eviware.loadui.impl.layout;

import java.util.Collections;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.google.common.collect.ImmutableMap;

public class LayoutContainerImplTest
{
	private LayoutContainer layoutContainer;

	@Before
	public void setup()
	{
		layoutContainer = new LayoutContainerImpl( ImmutableMap.of( "label", "constraints" ) );
	}

	@Test
	public void shouldNotBeModifiableAfterFreezing()
	{
		LayoutComponent component1 = mock( LayoutComponent.class );
		LayoutComponent component2 = mock( LayoutComponent.class );

		layoutContainer.add( component1 );
		layoutContainer.freeze();

		assertThat( layoutContainer.isFrozen(), is( true ) );

		try
		{
			layoutContainer.add( component2 );
			fail( "Should have thrown an UnsupportedOperationException" );
		}
		catch( UnsupportedOperationException e )
		{
		}
		try
		{
			layoutContainer.add( 0, component2 );
			fail( "Should have thrown an UnsupportedOperationException" );
		}
		catch( UnsupportedOperationException e )
		{
		}
		try
		{
			layoutContainer.addAll( Collections.singleton( component2 ) );
			fail( "Should have thrown an UnsupportedOperationException" );
		}
		catch( UnsupportedOperationException e )
		{
		}
		try
		{
			layoutContainer.addAll( 0, Collections.singleton( component2 ) );
			fail( "Should have thrown an UnsupportedOperationException" );
		}
		catch( UnsupportedOperationException e )
		{
		}
		try
		{
			layoutContainer.clear();
			fail( "Should have thrown an UnsupportedOperationException" );
		}
		catch( UnsupportedOperationException e )
		{
		}
		try
		{
			layoutContainer.remove( 0 );
			fail( "Should have thrown an UnsupportedOperationException" );
		}
		catch( UnsupportedOperationException e )
		{
		}
		try
		{
			layoutContainer.remove( component1 );
			fail( "Should have thrown an UnsupportedOperationException" );
		}
		catch( UnsupportedOperationException e )
		{
		}
		try
		{
			layoutContainer.removeAll( Collections.singleton( component1 ) );
			fail( "Should have thrown an UnsupportedOperationException" );
		}
		catch( UnsupportedOperationException e )
		{
		}
		try
		{
			layoutContainer.retainAll( Collections.emptyList() );
			fail( "Should have thrown an UnsupportedOperationException" );
		}
		catch( UnsupportedOperationException e )
		{
		}
		try
		{
			layoutContainer.iterator().remove();
			fail( "Should have thrown an UnsupportedOperationException" );
		}
		catch( UnsupportedOperationException e )
		{
		}

		assertThat( layoutContainer.size(), is( 1 ) );
	}
}

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
package com.eiware.loadui.util.collections;

import java.util.EventObject;

import org.junit.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.util.collections.CollectionEventSupport;

public class CollectionEventSupportTest
{
	private CollectionEventSupport<Object, Object> collectionEventSupport;
	private EventFirer eventFirerMock;

	@Before
	public void setup()
	{
		eventFirerMock = mock( EventFirer.class );
		collectionEventSupport = new CollectionEventSupport<Object, Object>( eventFirerMock, "COLLECTION" );
	}

	@Test
	public void shouldReturnEmptyCollectionWhenEmpty()
	{
		assertThat( collectionEventSupport.getItems(), notNullValue() );
		assertThat( collectionEventSupport.getItems().isEmpty(), is( true ) );
	}

	@Test
	public void shouldFireEventAddedOnSuccessfulAdd()
	{
		collectionEventSupport.addItem( "ONE" );
		collectionEventSupport.addItem( "TWO" );
		verify( eventFirerMock, times( 2 ) ).fireEvent( ( EventObject )any() );

		collectionEventSupport.addItem( "ONE" );
		assertThat( collectionEventSupport.getItems().size(), is( 2 ) );

		verifyNoMoreInteractions( eventFirerMock );
	}

	@Test
	public void shouldFireEventRemovedOnSuccessfulRemove()
	{
		collectionEventSupport.addItem( "ONE" );
		collectionEventSupport.addItem( "TWO" );
		collectionEventSupport.addItem( "THREE" );
		verify( eventFirerMock, times( 3 ) ).fireEvent( ( EventObject )any() );

		collectionEventSupport.removeItem( "FOUR" );
		verify( eventFirerMock, times( 3 ) ).fireEvent( ( EventObject )any() );

		collectionEventSupport.removeItem( "TWO" );
		verify( eventFirerMock, times( 4 ) ).fireEvent( ( EventObject )any() );
		assertThat( collectionEventSupport.getItems().size(), is( 2 ) );

		verifyNoMoreInteractions( eventFirerMock );
	}

	@Test
	public void shouldReleaseChildrenWhenReleased()
	{
		Releasable releasableMock1 = mock( Releasable.class );
		Releasable releasableMock2 = mock( Releasable.class );

		collectionEventSupport.addItem( releasableMock1 );
		collectionEventSupport.addItem( "A String" );
		collectionEventSupport.addItem( releasableMock2 );

		collectionEventSupport.release();

		verify( releasableMock1 ).release();
		verify( releasableMock2 ).release();
	}

	@Test
	public void shouldClearWhenReleased()
	{
		collectionEventSupport.addItem( "ONE" );
		collectionEventSupport.addItem( "TWO" );
		collectionEventSupport.addItem( "THREE" );

		collectionEventSupport.release();

		assertThat( collectionEventSupport.getItems(), notNullValue() );
		assertThat( collectionEventSupport.getItems().isEmpty(), is( true ) );
	}

	@Test( expected = NullPointerException.class )
	public void shouldNotAcceptNullAttachments()
	{
		collectionEventSupport.addItemWith( "NULL ATTACHMENT", null );
	}

	@Test
	public void shouldKeepAttachments()
	{
		Object attachment1 = new Object();
		Object attachment2 = new Object();
		Object attachment3 = new Object();

		collectionEventSupport.addItemWith( "ONE", attachment1 );
		collectionEventSupport.addItem( "NO ATTACHMENT" );
		collectionEventSupport.addItemWith( "TWO", attachment2 );
		//Shouldn't change the attachment since TWO already exists.
		collectionEventSupport.addItemWith( "TWO", attachment3 );

		assertThat( collectionEventSupport.getAttachment( "ONE" ), is( attachment1 ) );
		assertThat( collectionEventSupport.getAttachment( "TWO" ), is( attachment2 ) );
		assertThat( collectionEventSupport.getAttachment( "NO ATTACHMENT" ), nullValue() );

		collectionEventSupport.removeItem( "TWO" );
		collectionEventSupport.addItemWith( "TWO", attachment3 );
		assertThat( collectionEventSupport.getAttachment( "TWO" ), is( attachment3 ) );
	}
}

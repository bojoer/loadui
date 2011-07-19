package com.eiware.loadui.util.collections;

import java.util.EventObject;

import org.junit.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.util.collections.CollectionEventSupport;

public class CollectionEventSupportTest
{
	private CollectionEventSupport<String> collectionEventSupport;
	private EventFirer eventFirerMock;

	@Before
	public void setup()
	{
		eventFirerMock = mock( EventFirer.class );
		collectionEventSupport = new CollectionEventSupport<String>( eventFirerMock, "COLLECTION" );
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
}

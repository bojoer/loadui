package com.eviware.loadui.ui.fx.views.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import org.junit.Test;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionsInfo;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionsInfo.Data;

public class FXExecutionsInfoTest
{

	@Test
	public void testNormalCase()
	{
		FxExecutionsInfo info = new FxExecutionsInfo();

		ObservableList<Execution> archived = FXCollections.observableArrayList();
		ObservableList<Execution> recent = FXCollections.observableArrayList();
		Property<Execution> current = new SimpleObjectProperty<>();

		class DataContainer
		{
			Data data;
		}
		;
		final DataContainer container = new DataContainer();

		info.runWhenReady( new Callback<ExecutionsInfo.Data, Void>()
		{
			@Override
			public Void call( ExecutionsInfo.Data data )
			{
				container.data = data;
				return null;
			}
		} );

		assertNull( container.data );
		info.setArchivedExecutions( archived );
		assertNull( container.data );
		info.setRecentExecutions( recent );
		assertNull( container.data );
		info.setCurrentExecution( current );
		assertNull( container.data );

		HBox menuParent = new HBox();

		// container.data should be set after we set the last item
		info.setMenuParent( menuParent );
		assertNotNull( container.data );

		VBox menuItem = new VBox();
		info.addToMenu( menuItem );

		// menuItem should go into the menuParent we added to info
		assertEquals( 1, menuParent.getChildren().size() );
		assertSame( menuItem, menuParent.getChildren().get( 0 ) );

		Execution e1 = mock( Execution.class );

		// check if the execution is correctly set
		assertNull( container.data.getCurrentExecution().getValue() );
		current.setValue( e1 );
		assertSame( e1, container.data.getCurrentExecution().getValue() );

		// check if lists are set right
		assertSame( archived, container.data.getArchivedExecutions() );
		assertSame( recent, container.data.getRecentExecutions() );

	}

	@Test
	public void testWhenManyCallbacksAreAdded()
	{
		FxExecutionsInfo info = new FxExecutionsInfo();

		ObservableList<Execution> archived = FXCollections.observableArrayList();
		ObservableList<Execution> recent = FXCollections.observableArrayList();
		Property<Execution> current = new SimpleObjectProperty<>();

		class DataContainer
		{
			Data[] data = new Data[3];
		}
		;

		final DataContainer container = new DataContainer();

		info.runWhenReady( new Callback<ExecutionsInfo.Data, Void>()
		{
			@Override
			public Void call( ExecutionsInfo.Data data )
			{
				container.data[0] = data;
				return null;
			}
		} );
		info.runWhenReady( new Callback<ExecutionsInfo.Data, Void>()
		{
			@Override
			public Void call( ExecutionsInfo.Data data )
			{
				container.data[1] = data;
				return null;
			}
		} );

		info.setArchivedExecutions( archived );
		info.setRecentExecutions( recent );
		info.setCurrentExecution( current );
		HBox menuParent = new HBox();
		info.setMenuParent( menuParent );

		// everything has been set now, callbacks should have been called
		assertNotNull( container.data[0] );
		assertNotNull( container.data[1] );

		// last item not set yet
		assertNull( container.data[2] );

		info.runWhenReady( new Callback<ExecutionsInfo.Data, Void>()
		{
			@Override
			public Void call( ExecutionsInfo.Data data )
			{
				container.data[2] = data;
				return null;
			}
		} );

		// callback should be called immediately
		assertNotNull( container.data[2] );

	}

	@Test
	public void testImpatientUserAddingMenuItemsEarly()
	{
		FxExecutionsInfo info = new FxExecutionsInfo();

		ObservableList<Execution> archived = FXCollections.observableArrayList();
		ObservableList<Execution> recent = FXCollections.observableArrayList();
		Property<Execution> current = new SimpleObjectProperty<>();
		info.setCurrentExecution( current );

		info.setArchivedExecutions( archived );
		info.setRecentExecutions( recent );

		Label l1 = new Label();
		VBox v1 = new VBox();
		HBox h1 = new HBox();

		// adding items carelessly before information is ready should still work
		info.addToMenu( l1 );
		info.addToMenu( v1 );
		info.addToMenu( h1 );

		// last piece missing, should trigger adding stuff to the menu
		HBox menuParent = new HBox();
		info.setMenuParent( menuParent );

		assertSame( l1, menuParent.getChildren().get( 0 ) );
		assertSame( v1, menuParent.getChildren().get( 1 ) );
		assertSame( h1, menuParent.getChildren().get( 2 ) );
	}

}

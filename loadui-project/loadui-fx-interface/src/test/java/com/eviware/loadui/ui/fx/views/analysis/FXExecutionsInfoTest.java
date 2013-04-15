/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.views.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import com.eviware.loadui.test.categories.GUITest;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import org.junit.Test;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionsInfo;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionsInfo.Data;
import org.junit.experimental.categories.Category;

@Category( GUITest.class )
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
		container.data.addToMenu( menuItem );

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
		
		// unset the data to see if it will be set again (should not)
		container.data[0] = null;
		container.data[1] = null;

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

		// older callbacks should not run again
		assertNull( container.data[0] );
		assertNull( container.data[1] );
		
	}
	
	@Test
	public void testAlwaysRun()
	{
		FxExecutionsInfo info = new FxExecutionsInfo();

		ObservableList<Execution> archived = FXCollections.observableArrayList();
		ObservableList<Execution> recent = FXCollections.observableArrayList();
		Property<Execution> current = new SimpleObjectProperty<>();

		class DataContainer
		{
			Data[] data = new Data[5];
		}
		;

		final DataContainer container = new DataContainer();

		info.alwaysRunWhenReady( new Callback<ExecutionsInfo.Data, Void>()
		{
			@Override
			public Void call( ExecutionsInfo.Data data )
			{
				container.data[0] = data;
				return null;
			}
		} );
		info.alwaysRunWhenReady( new Callback<ExecutionsInfo.Data, Void>()
		{
			@Override
			public Void call( ExecutionsInfo.Data data )
			{
				container.data[1] = data;
				return null;
			}
		} );
		
		// this one should run only once
		info.runWhenReady( new Callback<ExecutionsInfo.Data, Void>()
				{
					@Override
					public Void call( ExecutionsInfo.Data data )
					{
						container.data[2] = data;
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
		assertNotNull( container.data[2] );

		// last items not set yet
		assertNull( container.data[3] );
		assertNull( container.data[4] );
		
		// unset the data to see if it will be set again
		container.data[0] = null; // should run
		container.data[1] = null; // should run
		container.data[2] = null; // should NOT
		
		info.reset();
		
		info.setArchivedExecutions( archived );
		info.setRecentExecutions( recent );
		info.setCurrentExecution( current );
		info.setMenuParent( menuParent );

		info.alwaysRunWhenReady( new Callback<ExecutionsInfo.Data, Void>()
		{
			@Override
			public Void call( ExecutionsInfo.Data data )
			{
				container.data[3] = data;
				return null;
			}
		} );
		info.runWhenReady( new Callback<ExecutionsInfo.Data, Void>()
				{
					@Override
					public Void call( ExecutionsInfo.Data data )
					{
						container.data[4] = data;
						return null;
					}
				} );
		
		assertNotNull( container.data[0] );
		assertNotNull( container.data[1] );
		assertNull( container.data[2] );
		assertNotNull( container.data[3] );
		assertNotNull( container.data[4] );
		
	}
	
	
	
	

}

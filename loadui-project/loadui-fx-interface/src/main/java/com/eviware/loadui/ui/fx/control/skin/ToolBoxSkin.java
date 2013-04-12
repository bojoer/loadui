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
package com.eviware.loadui.ui.fx.control.skin;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.PopupControl;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.ui.fx.control.ScrollableList;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.control.behavior.ToolBoxBehavior;
import com.eviware.loadui.ui.fx.views.analysis.linechart.LineChartViewNode;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.SkinBase;

public class ToolBoxSkin<E extends Node> extends SkinBase<ToolBox<E>, BehaviorBase<ToolBox<E>>>
{
	protected static final Logger log = LoggerFactory.getLogger( LineChartViewNode.class );

	private final ObservableMap<String, ToolBoxCategory> categoriesByName = FXCollections.observableHashMap();
	private final Comparator<ToolBoxCategory> categoryComparator = new Comparator<ToolBoxCategory>()
	{
		@Override
		public int compare( ToolBoxCategory o1, ToolBoxCategory o2 )
		{
			Comparator<String> comparator = getSkinnable().getCategoryComparator();
			if( comparator == null )
			{
				return Ordering.natural().compare( o1.category, o2.category );
			}
			else
			{
				return comparator.compare( o1.category, o2.category );
			}
		}
	};

	private final ScrollableList<ToolBoxCategory> categoryList = new ScrollableList<>();
	private final ToolBoxExpander expander;

	public ToolBoxSkin( final ToolBox<E> toolBox )
	{
		super( toolBox, new ToolBoxBehavior<>( toolBox ) );

		categoryList.setOrientation( Orientation.VERTICAL );
		categoryList.sizePerItemProperty().bind( toolBox.heightPerItemProperty() );

		//Keep pager items synchronized with the categories.
		categoriesByName.addListener( new MapChangeListener<String, ToolBoxCategory>()
		{
			@Override
			public void onChanged( MapChangeListener.Change<? extends String, ? extends ToolBoxCategory> change )
			{
				List<ToolBoxCategory> items = categoryList.getItems();
				if( change.wasRemoved() )
				{
					ToolBoxCategory value = change.getValueRemoved();
					items.remove( value );
				}
				if( change.wasAdded() )
				{
					ToolBoxCategory value = change.getValueAdded();
					int index = -Collections.binarySearch( items, value, categoryComparator ) - 1;
					items.add( index, value );
				}
			}
		} );

		toolBox.categoryComparatorProperty().addListener( new ChangeListener<Comparator<String>>()
		{
			@Override
			public void changed( ObservableValue<? extends Comparator<String>> arg0, Comparator<String> arg1,
					Comparator<String> arg2 )
			{
				FXCollections.sort( categoryList.getItems(), categoryComparator );
			}
		} );

		toolBox.getComparators().addListener( new MapChangeListener<String, Comparator<? super E>>()
		{
			@Override
			public void onChanged( MapChangeListener.Change<? extends String, ? extends Comparator<? super E>> change )
			{
				String categoryName = change.getKey();
				if( categoryName == null )
				{
					for( ToolBoxCategory category : categoriesByName.values() )
					{
						FXCollections.sort( category.categoryItems, toolBox.getComparator( category.category ) );
					}
				}
				else
				{
					ToolBoxCategory category = categoriesByName.get( categoryName );
					if( category != null )
					{
						FXCollections.sort( category.categoryItems, toolBox.getComparator( categoryName ) );
					}
				}
			}
		} );

		//Keep categories updated with the correct children (unsorted).
		toolBox.getItems().addListener( new ListChangeListener<E>()
		{
			@Override
			public void onChanged( ListChangeListener.Change<? extends E> change )
			{
				Set<ToolBoxCategory> possiblyEmpty = Sets.newHashSet();
				while( change.next() )
				{
					for( E removed : change.getRemoved() )
					{
						if( categoriesByName.get( ToolBox.getCategory( removed ) ) == null )
						{
							throw new RuntimeException( " Cannot find the category for toolbox item (" + removed
									+ "), you should probably set the category on creation of this object" );
						}

						ToolBoxCategory category = categoriesByName.get( ToolBox.getCategory( removed ) );
						category.categoryItems.remove( removed );

						if( category.categoryItems.isEmpty() )
						{
							categoryList.getItems().remove( category );
						}
						possiblyEmpty.add( category );
					}

					for( E added : change.getAddedSubList() )
					{
						String categoryName = ToolBox.getCategory( added );
						ToolBoxCategory category = categoriesByName.get( categoryName );
						if( category == null )
						{
							categoriesByName.put( categoryName, category = new ToolBoxCategory( categoryName ) );
						}
						int index = Math.max( 0, -Collections.binarySearch( category.categoryItems, added,
								toolBox.getComparator( categoryName ) ) - 1 );
						category.categoryItems.add( index, added );
						possiblyEmpty.remove( category );
					}
				}

				for( ToolBoxCategory category : possiblyEmpty )
				{
					if( category.categoryItems.isEmpty() )
					{
						categoriesByName.remove( category.category );
					}
				}
			}
		} );

		expander = new ToolBoxExpander();

		for( E item : toolBox.getItems() )
		{
			 
			String categoryName = ToolBox.getCategory( item );
									
			ToolBoxCategory category = categoriesByName.get( categoryName );
			if( category == null )
			{
				categoriesByName.put( categoryName, category = new ToolBoxCategory( categoryName ) );
			}
			category.categoryItems.add( item );
		}

		for( ToolBoxCategory category : categoriesByName.values() )
		{
			FXCollections.sort( category.categoryItems, toolBox.getComparator( category.category ) );
		}

		getChildren().setAll( VBoxBuilder.create().children( toolBox.getLabel(), categoryList ).build() );
	}

	private class ToolBoxCategory extends BorderPane
	{
		private final ObservableList<E> categoryItems = FXCollections.observableArrayList();
		private final ObjectBinding<E> shownElement = Bindings.when( expander.expandedCategory.isNotEqualTo( this ) )
				.then( Bindings.valueAt( categoryItems, 0 ) ).otherwise( ( E )null );
		private final String category;
		private final Button expanderButton;
		private final ItemHolder itemHolder;

		public ToolBoxCategory( String category )
		{
			getStyleClass().setAll( "category" );
			setId( category );
			this.category = category;

			itemHolder = new ItemHolder( category );
			
			
			shownElement.addListener( new ChangeListener<E>()
			{
				@Override
				public void changed( ObservableValue<? extends E> arg0, E oldVal, E newVal )
				{
					if( newVal != null )
					{
						itemHolder.items.setAll( Collections.singleton( newVal ) );
					}
					else
					{
						itemHolder.items.clear();
					}
				}
			} );

			setLeft( itemHolder );

			expanderButton = ButtonBuilder.create().build();
			expanderButton.getStyleClass().addAll( "expander-button", "toolbar-button", "styleable-graphic" );
			expanderButton.disableProperty().bind( Bindings.size( categoryItems ).lessThan( 2 ) );
						
			maxHeightProperty().bind(
					Bindings.when( expander.expandedCategory.isEqualTo( this ) ).then( heightProperty() )
							.otherwise( USE_COMPUTED_SIZE ) );
			minHeightProperty().bind(
					Bindings.when( expander.expandedCategory.isEqualTo( this ) ).then( heightProperty() )
							.otherwise( USE_COMPUTED_SIZE ) );

			itemHolder.getCategory().visibleProperty().bind( Bindings.when( expander.showingProperty() ).then( expander.expandedCategory.isEqualTo( this ).not() ).otherwise( true ) );
			/*
			expander.showingProperty().addListener( new InvalidationListener(){
				@Override
				public void invalidated( Observable arg0 )
				{
					if(expander.expandedCategory.isEqualTo( ToolBoxCategory.this ).get()){
						itemHolder.getCategory().visibleProperty().set( false );
					}else{
						itemHolder.getCategory().visibleProperty().set( true );
					}
				}
			});*/
			
			expanderButton.setOnAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent event )
				{
					
					expander.show( ToolBoxCategory.this );
				}
			} );

			setAlignment( expanderButton, Pos.CENTER_RIGHT );

			setRight( expanderButton );
		}
	}

	private class ItemHolder extends VBox
	{
		private final ObservableList<E> items = FXCollections.observableArrayList();

		private final Label category;
		
		public ItemHolder( String category )
		{
			this.category = LabelBuilder.create().text( category ).styleClass( "category-label" ).build(); 
			setAlignment( Pos.CENTER_LEFT );
			this.setAlignment( Pos.TOP_LEFT );
			getStyleClass().setAll( "item-holder" );
			HBox hbox = HBoxBuilder.create().styleClass( "items" ).alignment( Pos.TOP_LEFT ).build();
			Bindings.bindContent( hbox.getChildren(), items );
			getChildren().setAll( this.category, hbox );
		}
		
		public Label getCategory(){
			return category; 
		}
	}

	private class ToolBoxExpander extends PopupControl
	{
		private final ObjectProperty<ToolBoxCategory> expandedCategory = new SimpleObjectProperty<>( this,
				"expandedCategory" );

		private ToolBoxExpander()
		{
			getStyleClass().setAll( "tool-box-expander" );
			setAutoFix( false );
			setAutoHide( true );
			setOnHidden( new EventHandler<WindowEvent>()
			{
				@Override
				public void handle( WindowEvent event )
				{
					expandedCategory.set( null );
				}
			} );
		}
		
		public void show( ToolBoxCategory category )
		{
			expandedCategory.set( category );
			
			ItemHolder itemHolder = new ItemHolder( category.category );
			itemHolder.setMinWidth( ToolBoxSkin.this.getMinWidth() );
			itemHolder.setMinHeight( category.itemHolder.getMinHeight() );
			itemHolder.items.setAll( category.categoryItems );
						
			//The padding here allows the ItemHolder to grow beyond its usual size using negative insets, while still remaining in its correct position.
			StackPane pane = new StackPane();
			double padding = 12;
			pane.setPadding( new Insets( padding, 0, 0, 12 ) );
			pane.getChildren().setAll( itemHolder );

			bridge.getChildren().setAll( pane );
			Scene scene = category.getScene();

			Bounds sceneBounds = category.localToScene( category.getBoundsInLocal() );
			final double xPos = sceneBounds.getMinX() + scene.getX() + scene.getWindow().getX();
			final double yPos = sceneBounds.getMinY() + scene.getY() + scene.getWindow().getY();

			super.show( category, xPos - padding, yPos - padding );
		}
	}
}

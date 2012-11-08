package com.eviware.loadui.ui.fx.control.skin;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
import javafx.scene.control.PopupControl;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Region;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.WindowEvent;

import com.eviware.loadui.ui.fx.control.ScrollableList;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.control.behavior.ToolBoxBehavior;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.SkinBase;

public class ToolBoxSkin<E extends Node> extends SkinBase<ToolBox<E>, BehaviorBase<ToolBox<E>>>
{
	private final ObservableMap<String, ToolBoxCategory> categories = FXCollections.observableHashMap();
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
		categories.addListener( new MapChangeListener<String, ToolBoxCategory>()
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
					for( ToolBoxCategory category : categories.values() )
					{
						FXCollections.sort( category.categoryItems, toolBox.getComparator( category.category ) );
					}
				}
				else
				{
					ToolBoxCategory category = categories.get( categoryName );
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
						ToolBoxCategory category = categories.get( ToolBox.getCategory( removed ) );
						category.categoryItems.remove( removed );
						possiblyEmpty.add( category );
					}

					for( E added : change.getAddedSubList() )
					{
						String categoryName = ToolBox.getCategory( added );
						ToolBoxCategory category = categories.get( categoryName );
						if( category == null )
						{
							categories.put( categoryName, category = new ToolBoxCategory( categoryName ) );
						}
						int index = -Collections.binarySearch( category.categoryItems, added,
								toolBox.getComparator( categoryName ) ) - 1;
						category.categoryItems.add( index, added );
						possiblyEmpty.remove( category );
					}
				}

				for( ToolBoxCategory category : possiblyEmpty )
				{
					if( category.categoryItems.isEmpty() )
					{
						categories.remove( category.category );
					}
				}
			}
		} );

		expander = new ToolBoxExpander();

		for( E item : toolBox.getItems() )
		{
			String categoryName = ToolBox.getCategory( item );
			ToolBoxCategory category = categories.get( categoryName );
			if( category == null )
			{
				categories.put( categoryName, category = new ToolBoxCategory( categoryName ) );
			}
			category.categoryItems.add( item );
		}

		for( ToolBoxCategory category : categories.values() )
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
			expanderButton.getStyleClass().add( "expander-button" );
			expanderButton.disableProperty().bind( Bindings.size( categoryItems ).lessThan( 2 ) );

			maxHeightProperty().bind(
					Bindings.when( expander.expandedCategory.isEqualTo( this ) ).then( heightProperty() )
							.otherwise( USE_COMPUTED_SIZE ) );
			minHeightProperty().bind(
					Bindings.when( expander.expandedCategory.isEqualTo( this ) ).then( heightProperty() )
							.otherwise( USE_COMPUTED_SIZE ) );

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

		public ItemHolder( String category )
		{
			getStyleClass().setAll( "item-holder" );
			HBox hbox = HBoxBuilder.create().styleClass( "items" ).build();
			Bindings.bindContent( hbox.getChildren(), items );
			getChildren().setAll( new Label( category ), hbox );
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

			Region line = RegionBuilder.create().maxHeight( 1 ).minHeight( 1 ).styleClass( "line" ).build();

			ItemHolder itemHolder = new ItemHolder( category.category );
			itemHolder.setMinWidth( ToolBoxSkin.this.getWidth() );
			itemHolder.setMinHeight( category.itemHolder.getHeight() );
			itemHolder.items.setAll( category.categoryItems );
			itemHolder.getChildren().addAll( line );

			//The padding here allows the ItemHolder to grow beyond its usual size using negative insets, while still remaining in its correct position.
			StackPane pane = new StackPane();
			double padding = 10;
			pane.setPadding( new Insets( padding ) );
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
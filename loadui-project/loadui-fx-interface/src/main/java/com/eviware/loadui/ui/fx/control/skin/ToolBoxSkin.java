package com.eviware.loadui.ui.fx.control.skin;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Separator;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.control.behavior.ToolBoxBehavior;
import com.eviware.loadui.ui.fx.util.Pager;
import com.google.common.collect.Sets;
import com.sun.javafx.scene.control.skin.SkinBase;

public class ToolBoxSkin<E extends Node> extends SkinBase<ToolBox<E>, ToolBoxBehavior<E>>
{
	private final ObservableMap<String, ToolBoxCategory> categories = FXCollections.observableHashMap();
	private final Map<Integer, Double> sizes = new HashMap<>();
	private final Comparator<ToolBoxCategory> categoryComparator = new Comparator<ToolBoxCategory>()
	{
		@Override
		public int compare( ToolBoxCategory o1, ToolBoxCategory o2 )
		{
			return getSkinnable().getCategoryComparator().compare( o1.category, o2.category );
		}
	};

	private final Pager<ToolBoxCategory> pager;
	private final ToolBoxRegion mainRegion;
	private final ToolBoxExpander expander;

	public ToolBoxSkin( ToolBox<E> toolBox )
	{
		super( toolBox, new ToolBoxBehavior<>( toolBox ) );

		pager = new Pager<>();
		pager.setFluentMode( true );

		//Keep pager items synchronized with the categories.
		categories.addListener( new MapChangeListener<String, ToolBoxCategory>()
		{
			@Override
			public void onChanged( MapChangeListener.Change<? extends String, ? extends ToolBoxCategory> change )
			{
				sizes.clear();
				if( change.wasRemoved() )
				{
					pager.getItems().remove( change.getValueRemoved() );
				}
				if( change.wasAdded() )
				{
					//TODO: Sort using toolBox.categoryComparator
					pager.getItems().add( change.getValueAdded() );
				}
			}
		} );

		toolBox.categoryComparatorProperty().addListener( new ChangeListener<Comparator<String>>()
		{
			@Override
			public void changed( ObservableValue<? extends Comparator<String>> arg0, Comparator<String> arg1,
					Comparator<String> arg2 )
			{
				sizes.clear();
				FXCollections.sort( pager.getItems(), categoryComparator );
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
						category.categoryItems.add( added );
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

		mainRegion = new ToolBoxRegion( pager.getShownItems() );
		expander = new ToolBoxExpander();

		getChildren().setAll( mainRegion );
	}

	@Override
	protected void layoutChildren()
	{
		super.layoutChildren();

		double height = getHeight();
		double prefHeight = prefHeight( -1 );
		int itemsPerPage = pager.getItemsPerPage();
		sizes.put( itemsPerPage, prefHeight );

		if( height > prefHeight )
		{
			if( pager.getItems().size() - pager.getOffset() - pager.getItemsPerPage() > 0 )
			{
				Double requiredSize = sizes.get( itemsPerPage + 1 );
				if( requiredSize == null || requiredSize < height )
				{
					pager.setItemsPerPage( itemsPerPage + 1 );
				}
			}
		}
		else if( itemsPerPage > 1 )
		{
			pager.setItemsPerPage( itemsPerPage - 1 );
		}
	}

	private class ToolBoxRegion extends VBox
	{
		private final Label label = new Label();

		private ToolBoxRegion( final ObservableList<ToolBoxCategory> items )
		{
			getStyleClass().setAll( "tool-box-region" );
			Labeled labeled = getSkinnable();

			label.alignmentProperty().bind( labeled.alignmentProperty() );
			label.contentDisplayProperty().bind( labeled.contentDisplayProperty() );
			label.fontProperty().bind( labeled.fontProperty() );
			label.graphicProperty().bind( labeled.graphicProperty() );
			label.graphicTextGapProperty().bind( labeled.graphicTextGapProperty() );
			label.textAlignmentProperty().bind( labeled.textAlignmentProperty() );
			label.textFillProperty().bind( labeled.textFillProperty() );
			label.textOverrunProperty().bind( labeled.textOverrunProperty() );
			label.textProperty().bind( labeled.textProperty() );
			label.underlineProperty().bind( labeled.underlineProperty() );
			label.wrapTextProperty().bind( labeled.wrapTextProperty() );

			items.addListener( new InvalidationListener()
			{
				@Override
				public void invalidated( Observable arg0 )
				{
					refresh( items );
				}
			} );

			setOnScroll( new EventHandler<ScrollEvent>()
			{
				@Override
				public void handle( ScrollEvent event )
				{
					if( event.getDeltaY() < 0 )
					{
						pager.setPage( Math.min( pager.getPage() + 1, pager.getNumPages() - 1 ) );
					}
					else
					{
						pager.setPage( Math.max( pager.getPage() - 1, 0 ) );
					}
				}
			} );

			refresh( items );
		}

		private void refresh( Iterable<ToolBoxCategory> items )
		{
			getChildren().setAll( label, new Separator() );
			for( ToolBoxCategory category : items )
			{
				getChildren().addAll( category, new Separator() );
			}
		}
	}

	private class ToolBoxCategory extends BorderPane
	{
		private final ObservableList<E> categoryItems = FXCollections.observableArrayList();
		private final BooleanProperty expanded = new SimpleBooleanProperty( this, "expanded", false );
		private final ObjectBinding<E> shownElement = Bindings.when( expanded.not() )
				.then( Bindings.valueAt( categoryItems, 0 ) ).otherwise( ( E )null );
		private final String category;
		private final Button expanderButton;
		private final ItemHolder itemHolder;

		public ToolBoxCategory( String category )
		{
			getStyleClass().setAll( "category" );
			this.category = category;

			expanderButton = ButtonBuilder.create().build();
			expanderButton.getStyleClass().add( "expander-button" );

			expanderButton.setOnAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent event )
				{
					expanded.set( true );
					setPrefHeight( getHeight() );
					setMinHeight( getHeight() );
					expander.setOnHidden( new EventHandler<WindowEvent>()
					{
						@Override
						public void handle( WindowEvent windowEvent )
						{
							expanded.set( false );
							setPrefHeight( USE_COMPUTED_SIZE );
							setMinHeight( USE_COMPUTED_SIZE );
						}
					} );

					expander.show( ToolBoxCategory.this );
				}
			} );

			setAlignment( expanderButton, Pos.CENTER_RIGHT );
			setRight( expanderButton );

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
		}
	}

	private class ItemHolder extends VBox
	{
		private final ObservableList<E> items = FXCollections.observableArrayList();

		public ItemHolder( String category )
		{
			getStyleClass().setAll( "item-holder" );
			HBox hbox = HBoxBuilder.create().build();
			Bindings.bindContent( hbox.getChildren(), items );
			getChildren().setAll( new Label( category ), hbox );
		}
	}

	private class ToolBoxExpander extends PopupControl
	{
		private ToolBoxExpander()
		{
			getStyleClass().setAll( "tool-box-expander" );
			setAutoFix( false );
			setAutoHide( true );
		}

		public void show( ToolBoxCategory category )
		{
			ItemHolder itemHolder = new ItemHolder( category.category );
			itemHolder.setMinWidth( ToolBoxSkin.this.getWidth() );
			itemHolder.setMinHeight( category.itemHolder.getHeight() );
			itemHolder.items.setAll( category.categoryItems );

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

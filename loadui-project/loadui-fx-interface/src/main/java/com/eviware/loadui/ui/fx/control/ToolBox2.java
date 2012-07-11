package com.eviware.loadui.ui.fx.control;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.PopupControl;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

import com.eviware.loadui.ui.fx.util.Pager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Holds items belonging to multiple categories in a toolbox. Categories can be
 * expanded to show their contents.
 * 
 * @author dain.nilsson
 */
@DefaultProperty( "items" )
public class ToolBox2<T extends Node> extends VBox
{
	private static final String TOOL_BOX_PROPERTY = "tool-box";
	private static final String CATEGORY_PROPERTY = "tool-box-category";
	private static final String DEFAULT_STYLE_CLASS = "tool-box";

	/**
	 * Sets the category for a child when contained by a ToolBox. When no
	 * category has been set, or it has been explicitly set to null, the child's
	 * toString() value will be used as a category.
	 * 
	 * @param node
	 * @param category
	 */
	public static void setCategory( Node node, String category )
	{
		node.getProperties().put( CATEGORY_PROPERTY, category );
		Object toolBox = node.getProperties().get( TOOL_BOX_PROPERTY );
		if( toolBox instanceof ToolBox2 )
		{
			( ( ToolBox2<?> )toolBox ).refreshItems();
		}
	}

	/**
	 * Returns the child's Category.
	 * 
	 * @param node
	 * @return
	 */
	public static String getCategory( Node node )
	{
		Object category = node.getProperties().get( CATEGORY_PROPERTY );
		return String.valueOf( category == null ? node : category );
	}

	private static final Callable<Void> noOperation = new Callable<Void>()
	{
		@Override
		public Void call() throws Exception
		{
			return null;
		}
	};

	private final InvalidationListener redoLayout = new InvalidationListener()
	{
		@Override
		public void invalidated( Observable arg0 )
		{
			redoLayout();
		}
	};

	private final InvalidationListener refreshItems = new InvalidationListener()
	{
		@Override
		public void invalidated( Observable arg0 )
		{
			refreshItems();
		}
	};

	private final Label title = new Label( "Toolbox" );
	private final VBox contentBox = new VBox();
	private final Pager<T> pager = new Pager<>( 3 );
	private final ToolBoxExpander expander = new ToolBoxExpander();
	private final Cache<Integer, Double> sizeCache = CacheBuilder.newBuilder().weakKeys().build();
	//private final ObjectBinding<Void> cacheClearTrigger;
	private TreeMap<String, ToolBoxCategory> categories = new TreeMap<>();

	public ToolBox2( String title )
	{
		this();
		setText( title );
	}

	public ToolBox2()
	{
		getStyleClass().add( DEFAULT_STYLE_CLASS );
		pager.setFluentMode( true );
		title.getStyleClass().add( "title" );
		getChildren().setAll( title, contentBox );

		//cacheClearTrigger = Bindings.createObjectBinding( noOperation, pager.getItems(), pager.offsetProperty() );
		//cacheClearTrigger.addListener( redoLayout );

		Bindings.createObjectBinding( noOperation, pager.getItems(), pager.offsetProperty() ).addListener( redoLayout );

		pager.itemsPerPageProperty().addListener( refreshItems );

		redoLayout();
	}

	@Override
	protected void layoutChildren()
	{
		super.layoutChildren();

		final double prefHeight = prefHeight( -1 );
		final int itemsPerPage = pager.getItemsPerPage();
		final double height = getHeight();

		if( sizeCache.getIfPresent( itemsPerPage ) == null )
		{
			sizeCache.put( itemsPerPage, prefHeight );
		}

		if( height > prefHeight )
		{
			Double nextHeight = sizeCache.getIfPresent( itemsPerPage + 1 );
			if( nextHeight != null && height > nextHeight )
			{
				pager.setItemsPerPage( itemsPerPage + 1 );
			}
		}
		else
		{
			pager.setItemsPerPage( Math.max( 1, itemsPerPage - 1 ) );
		}
	}

	public void setCategoryComparator( Comparator<String> comparator )
	{
		TreeMap<String, ToolBoxCategory> newCategories = new TreeMap<>( comparator );
		newCategories.putAll( categories );
		categories = newCategories;
		refreshItems();
	}

	public void setText( String label )
	{
		title.setText( label );
	}

	public ObservableList<T> getItems()
	{
		return pager.getItems();
	}

	private void redoLayout()
	{
		sizeCache.invalidateAll();
		pager.setItemsPerPage( Math.max( 1, pager.getItems().size() - pager.getOffset() ) );
		refreshItems();
	}

	private void refreshItems()
	{
		if( expander.isShowing() )
		{
			expander.hide();
		}

		contentBox.getChildren().setAll( new Separator() );
		categories.clear();
		for( Node node : pager.getShownItems() )
		{
			node.getProperties().put( TOOL_BOX_PROPERTY, this );

			String category = getCategory( node );
			ToolBoxCategory toolBoxCategory = null;
			if( !categories.containsKey( category ) )
			{
				categories.put( category, toolBoxCategory = new ToolBoxCategory( category ) );
			}
			else
			{
				toolBoxCategory = categories.get( category );
			}
			toolBoxCategory.categoryItems.add( node );
		}

		for( ToolBoxCategory category : categories.values() )
		{
			category.refresh();
			contentBox.getChildren().addAll( category, new Separator() );
		}
	}

	private class ItemHolder extends VBox
	{
		private ItemHolder( String title, Collection<? extends Node> nodes )
		{
			getStyleClass().setAll( "item-holder" );
			getChildren().setAll( new Label( title ), HBoxBuilder.create().children( nodes ).build() );
		}
	}

	private class ToolBoxCategory extends BorderPane
	{
		private final ObservableList<Node> categoryItems = FXCollections.observableArrayList();
		private final String category;
		private final Button expanderButton;

		private ToolBoxCategory( String category )
		{
			getStyleClass().setAll( "tool-box-category" );
			this.category = category;

			expanderButton = ButtonBuilder.create().build();
			expanderButton.getStyleClass().add( "expander-button" );
			setAlignment( expanderButton, Pos.CENTER_RIGHT );
			setRight( expanderButton );

			expanderButton.setOnAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent event )
				{
					setPrefHeight( getHeight() );
					setMinHeight( getHeight() );
					expander.setOnHidden( new EventHandler<WindowEvent>()
					{
						@Override
						public void handle( WindowEvent event )
						{
							refresh();
							setPrefHeight( USE_COMPUTED_SIZE );
							setMinHeight( USE_COMPUTED_SIZE );
						}
					} );

					expander.show( ToolBoxCategory.this );
				}
			} );
		}

		private void refresh()
		{
			expanderButton.setDisable( categoryItems.size() < 2 );
			setLeft( new ItemHolder( category, categoryItems.subList( 0, 1 ) ) );
		}
	}

	private class ToolBoxExpander extends PopupControl
	{
		private ToolBoxExpander()
		{
			getStyleClass().setAll( "tool-box-expander" );
			setAutoFix( true );
			setAutoHide( true );
		}

		public void show( ToolBoxCategory category )
		{
			//The padding here allows the ItemHolder to grow beyond its usual size using negative insets, while still remaining in its correct position.
			ItemHolder itemHolder = new ItemHolder( category.category, category.categoryItems );
			StackPane pane = new StackPane();
			double allowedPadding = 10;
			pane.setPadding( new Insets( allowedPadding ) );
			pane.getChildren().setAll( itemHolder );

			bridge.getChildren().setAll( pane );
			Scene scene = category.getScene();
			Bounds sceneBounds = category.localToScene( category.getBoundsInLocal() );
			super.show( category, sceneBounds.getMinX() + scene.getX() + scene.getWindow().getX() - allowedPadding,
					sceneBounds.getMinY() + scene.getY() + scene.getWindow().getY() - allowedPadding );
		}
	}
}

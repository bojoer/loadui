package com.eviware.loadui.ui.fx.control;

import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

import com.eviware.loadui.ui.fx.util.Pager;
import com.google.common.base.Preconditions;

@DefaultProperty( "items" )
public class ScrollableList<E extends Node> extends StackPane
{
	private static final String DEFAULT_STYLE_CLASS = "scrollable-list";

	private final Pager<E> pager = new Pager<>();
	private final FixedSpaceBox itemBox = new FixedSpaceBox();
	private final Region placeholder = RegionBuilder.create().styleClass( "placeholder" ).build();
	private final Button prevButton;
	private final Button nextButton;

	private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>( this, "orientation",
			Orientation.VERTICAL );
	private final DoubleProperty sizePerItem = new SimpleDoubleProperty( this, "sizePerItem", 100.0 );
	private final DoubleProperty spacing = new SimpleDoubleProperty( this, "spacing", 0.0 );

	public ScrollableList()
	{
		prevButton = ButtonBuilder.create().styleClass( "nav", "prev" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				pager.prevPage();
			}
		} ).build();

		nextButton = ButtonBuilder.create().styleClass( "nav", "next" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				pager.nextPage();
			}

		} ).build();

		initialize();
	}

	private void buildBox()
	{
		Pane box = null;
		prevButton.getStyleClass().removeAll( "up", "left" );
		nextButton.getStyleClass().removeAll( "down", "right" );

		if( isVertical() )
		{
			prevButton.getStyleClass().add( "up" );
			nextButton.getStyleClass().add( "down" );
			box = VBoxBuilder.create().styleClass( "box" ).alignment( Pos.CENTER )
					.children( prevButton, itemBox, nextButton ).build();
		}
		else
		{
			prevButton.getStyleClass().add( "left" );
			nextButton.getStyleClass().add( "right" );
			box = HBoxBuilder.create().styleClass( "box" ).alignment( Pos.CENTER )
					.children( prevButton, itemBox, nextButton ).build();
		}
		box.setOnScroll( new EventHandler<ScrollEvent>()
		{
			@Override
			public void handle( ScrollEvent event )
			{
				if( event.getDeltaY() > 0 || event.getDeltaX() > 0 )
				{
					pager.prevPage();
				}
				else if( event.getDeltaY() < 0 || event.getDeltaX() < 0 )
				{
					pager.nextPage();
				}
				event.consume();
			}
		} );

		getChildren().setAll( box );
	}

	private void initialize()
	{
		getStyleClass().setAll( DEFAULT_STYLE_CLASS );

		VBox.setVgrow( itemBox, Priority.ALWAYS );
		HBox.setHgrow( itemBox, Priority.ALWAYS );

		pager.setFluentMode( true );
		pager.getShownItems().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				if( pager.getShownItems().isEmpty() )
				{
					itemBox.getChildren().setAll( placeholder );
				}
				else
				{
					itemBox.getChildren().setAll( pager.getShownItems() );
				}
			}
		} );

		pager.itemsPerPageProperty().bind( Bindings.max( 1, itemBox.sizeForItems.divide( sizePerItem ) ) );

		prevButton.disableProperty().bind( pager.hasPrevProperty().not() );
		nextButton.disableProperty().bind( pager.hasNextProperty().not() );

		orientationProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable observable )
			{
				buildBox();
			}
		} );

		buildBox();
	}

	public ObservableList<E> getItems()
	{
		return pager.getItems();
	}

	public DoubleProperty sizePerItemProperty()
	{
		return sizePerItem;
	}

	public double getSizePerItem()
	{
		return sizePerItem.get();
	}

	public void setSizePerItem( double value )
	{
		Preconditions.checkArgument( value > 0, "sizePerItem must be >0, was %d", value );
		this.sizePerItem.set( value );
	}

	public DoubleProperty spacingProperty()
	{
		return spacing;
	}

	public double getSpacing()
	{
		return spacing.get();
	}

	public void setSpacing( double value )
	{
		Preconditions.checkArgument( value >= 0, "spacing must be >=0, was %d", value );
		this.spacing.set( value );
	}

	public Property<Orientation> orientationProperty()
	{
		return orientation;
	}

	public Orientation getOrientation()
	{
		return orientation.get();
	}

	public void setOrientation( Orientation value )
	{
		orientation.set( value );
	}

	public IntegerProperty pageProperty()
	{
		return pager.pageProperty();
	}

	public int getPage()
	{
		return pager.getPage();
	}

	public void setPage( int value )
	{
		pager.setPage( value );
	}

	public int getNumPages()
	{
		return pager.getNumPages();
	}

	public ReadOnlyIntegerProperty numPagesProperty()
	{
		return pager.numPagesProperty();
	}

	public ObservableList<E> getShownItems()
	{
		return pager.getShownItems();
	}

	private boolean isVertical()
	{
		return getOrientation() == Orientation.VERTICAL;
	}

	private class FixedSpaceBox extends Pane
	{
		private final DoubleProperty sizeForItems = new SimpleDoubleProperty( getHeight() );

		public FixedSpaceBox()
		{
			getStyleClass().setAll( "item-box" );
		}

		@Override
		protected double computePrefWidth( double height )
		{
			double insets = getInsets().getLeft() + getInsets().getRight();
			return insets
					+ ( isVertical() ? getMaxPrefWidth( height ) : getSizePerItem() * pager.getItems().size() + getSpacing()
							* ( pager.getItems().size() - 1 ) );
		}

		@Override
		protected double computePrefHeight( double width )
		{
			double insets = getInsets().getTop() + getInsets().getBottom();
			return insets
					+ ( isVertical() ? getSizePerItem() * pager.getItems().size() + getSpacing()
							* ( pager.getItems().size() - 1 ) : getMaxPrefHeight( width ) );
		}

		@Override
		protected double computeMinWidth( double height )
		{
			double insets = getInsets().getLeft() + getInsets().getRight();
			return insets + ( isVertical() ? getMaxMinWidth( height ) : getSizePerItem() );
		}

		@Override
		protected double computeMinHeight( double width )
		{
			double insets = getInsets().getTop() + getInsets().getBottom();
			return insets + ( isVertical() ? getSizePerItem() : getMaxMinHeight( width ) );
		}

		private double getMaxPrefHeight( double width )
		{
			double maxHeight = -1;
			for( Node child : pager.getItems() )
			{
				maxHeight = Math.max( maxHeight, child.prefHeight( width ) );
			}

			return maxHeight;
		}

		private double getMaxMinHeight( double width )
		{
			double maxHeight = -1;
			for( Node child : pager.getItems() )
			{
				maxHeight = Math.max( maxHeight, child.minHeight( width ) );
			}

			return maxHeight;
		}

		private double getMaxPrefWidth( double height )
		{
			double maxHeight = -1;
			for( Node child : pager.getItems() )
			{
				maxHeight = Math.max( maxHeight, child.prefWidth( height ) );
			}

			return maxHeight;
		}

		private double getMaxMinWidth( double height )
		{
			double maxHeight = -1;
			for( Node child : pager.getItems() )
			{
				maxHeight = Math.max( maxHeight, child.minWidth( height ) );
			}

			return maxHeight;
		}

		@Override
		protected void layoutChildren()
		{
			double top = getInsets().getTop();
			double right = getInsets().getRight();
			double bottom = getInsets().getBottom();
			double left = getInsets().getLeft();
			double height = getHeight() - top - bottom;
			double width = getWidth() - left - right;
			double childSize = ScrollableList.this.getSizePerItem();

			boolean vertical = isVertical();
			double childWidth = vertical ? width : childSize;
			double childHeight = vertical ? childSize : height;
			sizeForItems.set( vertical ? height : width );

			for( Node child : getChildren() )
			{
				layoutInArea( child, left, top, childWidth, childHeight, childHeight, HPos.CENTER, VPos.BOTTOM );
				if( vertical )
				{
					top += childSize + getSpacing();
				}
				else
				{
					left += childSize + getSpacing();
				}
			}
		}
	}
}
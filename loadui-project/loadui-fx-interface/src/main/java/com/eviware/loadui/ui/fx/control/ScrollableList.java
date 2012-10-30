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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

import com.eviware.loadui.ui.fx.util.Pager;
import com.google.common.base.Preconditions;

@DefaultProperty( "items" )
public class ScrollableList<E extends Node> extends StackPane
{
	private static final String DEFAULT_STYLE_CLASS = "scroll-list";

	private final Pager<E> pager = new Pager<>();
	private final FixedSpaceBox itemBox = new FixedSpaceBox();
	private final Button prevButton;
	private final Button nextButton;

	private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>( this, "orientation",
			Orientation.VERTICAL );
	private final DoubleProperty sizePerItem = new SimpleDoubleProperty( this, "sizePerItem", 100.0 );

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
			box = VBoxBuilder.create().alignment( Pos.CENTER ).children( prevButton, itemBox, nextButton ).build();
		}
		else
		{
			prevButton.getStyleClass().add( "left" );
			nextButton.getStyleClass().add( "right" );
			box = HBoxBuilder.create().alignment( Pos.CENTER ).children( prevButton, itemBox, nextButton ).build();
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
		Bindings.bindContent( itemBox.getChildren(), pager.getShownItems() );

		pager.itemsPerPageProperty().bind(
				Bindings.max( 1,
						Bindings.when( Bindings.equal( orientation, Orientation.VERTICAL ) ).then( itemBox.heightProperty() )
								.otherwise( itemBox.widthProperty() ).divide( sizePerItem ) ) );

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
		public FixedSpaceBox()
		{
			getStyleClass().setAll( "item-box" );
		}

		@Override
		protected double computePrefWidth( double height )
		{
			double insets = getInsets().getLeft() + getInsets().getRight();
			return insets + ( isVertical() ? getMaxPrefWidth( height ) : getSizePerItem() * pager.getItems().size() );
		}

		@Override
		protected double computePrefHeight( double width )
		{
			double insets = getInsets().getTop() + getInsets().getBottom();
			return insets + ( isVertical() ? getSizePerItem() * pager.getItems().size() : getMaxPrefHeight( width ) );
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
			double size = vertical ? height : width;

			double padding = size - pager.getItemsPerPage() * childSize;
			double childWidth = vertical ? width : childSize;
			double childHeight = vertical ? childSize : height;

			if( vertical )
			{
				top += padding / 2;
			}
			else
			{
				left += padding / 2;
			}

			for( Node child : getChildren() )
			{
				layoutInArea( child, left, top, childWidth, childHeight, childHeight, HPos.CENTER, VPos.BOTTOM );
				if( vertical )
				{
					top += childSize;
				}
				else
				{
					left += childSize;
				}
			}
		}
	}
}
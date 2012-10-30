package com.eviware.loadui.ui.fx.control.skin;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableObjectValue;
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
import javafx.scene.layout.VBoxBuilder;

import com.eviware.loadui.ui.fx.control.ScrollList;
import com.eviware.loadui.ui.fx.util.Pager;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.SkinBase;

public class ScrollListSkin<E extends Node> extends SkinBase<ScrollList<E>, BehaviorBase<ScrollList<E>>>
{
	private final Pager<E> pager;
	private final Button prevButton;
	private final Button nextButton;
	private final FixedSpaceBox itemBox;

	public ScrollListSkin( ScrollList<E> scrollList )
	{
		super( scrollList, new BehaviorBase<>( scrollList ) );

		pager = new Pager<>( scrollList.getItems() );
		pager.setFluentMode( true );

		itemBox = new FixedSpaceBox();
		HBox.setHgrow( itemBox, Priority.ALWAYS );
		Bindings.bindContent( itemBox.getChildren(), pager.getShownItems() );

		pager.itemsPerPageProperty().bind(
				Bindings
						.when(
								Bindings.equal( ( ObservableObjectValue<?> )scrollList.orientationProperty(),
										Orientation.VERTICAL ) ).then( itemBox.heightProperty() )
						.otherwise( itemBox.widthProperty() ).divide( scrollList.sizePerItemProperty() ) );

		prevButton = ButtonBuilder.create().styleClass( "nav" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				pager.prevPage();
			}
		} ).build();
		prevButton.disableProperty().bind( pager.hasPrevProperty().not() );

		nextButton = ButtonBuilder.create().styleClass( "nav" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				pager.nextPage();
			}

		} ).build();
		nextButton.disableProperty().bind( pager.hasNextProperty().not() );

		scrollList.orientationProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable observable )
			{
				buildBox();
			}
		} );

		buildBox();
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

	private boolean isVertical()
	{
		return getSkinnable().getOrientation() == Orientation.VERTICAL;
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
			return insets
					+ ( isVertical() ? getMaxPrefWidth( height ) : getSkinnable().getSizePerItem() * pager.getItems().size() );
		}

		@Override
		protected double computePrefHeight( double width )
		{
			double insets = getInsets().getTop() + getInsets().getBottom();
			return insets
					+ ( isVertical() ? getSkinnable().getSizePerItem() * pager.getItems().size() : getMaxPrefHeight( width ) );
		}

		@Override
		protected double computeMinWidth( double height )
		{
			double insets = getInsets().getLeft() + getInsets().getRight();
			return insets + ( isVertical() ? getMaxMinWidth( height ) : getSkinnable().getSizePerItem() );
		}

		@Override
		protected double computeMinHeight( double width )
		{
			double insets = getInsets().getTop() + getInsets().getBottom();
			return insets + ( isVertical() ? getSkinnable().getSizePerItem() : getMaxMinHeight( width ) );
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
			double childSize = getSkinnable().getSizePerItem();

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

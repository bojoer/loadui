package com.eviware.loadui.ui.fx.control.skin;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.util.Callback;

import com.eviware.loadui.ui.fx.control.PageList;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Pager;
import com.google.common.base.Function;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.SkinBase;

public class PageListSkin<E extends Node> extends SkinBase<PageList<E>, BehaviorBase<PageList<E>>>
{
	private final Pager<E> pager;
	private final ObservableList<Label> labels;

	public PageListSkin( PageList<E> pageList )
	{
		super( pageList, new BehaviorBase<>( pageList ) );

		pager = new Pager<>( pageList.getItems() );
		pager.setFluentMode( true );

		FixedSpaceBox itemBox = new FixedSpaceBox();
		HBox.setHgrow( itemBox, Priority.ALWAYS );
		Bindings.bindContent( itemBox.getChildren(), pager.getShownItems() );

		pager.itemsPerPageProperty().bind( itemBox.widthProperty().divide( pageList.widthPerItemProperty() ) );

		Label label = pageList.getLabel();
		StackPane.setAlignment( label, Pos.TOP_LEFT );

		Label pageNum = new Label();
		pageNum.textProperty().bind(
				Bindings.format( "Page %d of %d", pager.pageProperty().add( 1 ), pager.numPagesProperty() ) );
		StackPane.setAlignment( pageNum, Pos.TOP_RIGHT );

		Button prevButton = ButtonBuilder.create().onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				pager.prevPage();
			}
		} ).build();
		prevButton.disableProperty().bind( pager.hasPrevProperty().not() );

		Button nextButton = ButtonBuilder.create().onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				pager.nextPage();
			}

		} ).build();
		nextButton.disableProperty().bind( pager.hasNextProperty().not() );

		FixedSpaceBox labelBox = new FixedSpaceBox();
		HBox.setHgrow( labelBox, Priority.ALWAYS );

		labels = ObservableLists.transform( pager.getShownItems(), new Function<E, Label>()
		{
			@Override
			public Label apply( E input )
			{
				Callback<? super E, ? extends Label> labelFactory = getSkinnable().getLabelFactory();
				return labelFactory != null ? labelFactory.call( input ) : new Label( input.toString() );
			}
		} );
		Bindings.bindContent( labelBox.getChildren(), labels );

		VBox vbox = VBoxBuilder
				.create()
				.children( StackPaneBuilder.create().children( label, pageNum ).build(),
						HBoxBuilder.create().alignment( Pos.CENTER ).children( prevButton, itemBox, nextButton ).build(),
						new Separator(), labelBox ).build();
		vbox.setOnScroll( new EventHandler<ScrollEvent>()
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
		getChildren().setAll( vbox );
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
			return getInsets().getLeft() + getSkinnable().getWidthPerItem() * pager.getItems().size()
					+ getInsets().getRight();
		}

		@Override
		protected double computeMinWidth( double height )
		{
			return getInsets().getLeft() + getSkinnable().getWidthPerItem() + getInsets().getRight();
		}

		@Override
		protected double computePrefHeight( double width )
		{
			return computeMinHeight( width );
		}

		@Override
		protected double computeMinHeight( double width )
		{
			return getInsets().getTop() + getMaxPrefHeight( width ) + getInsets().getBottom();
		}

		private double getMaxPrefHeight( double width )
		{
			double maxHeight = -1;
			for( Node child : getChildren() )
			{
				maxHeight = Math.max( maxHeight, child.prefHeight( width ) );
			}

			return maxHeight;
		}

		@Override
		protected void layoutChildren()
		{
			double top = getInsets().getTop();
			double left = getInsets().getLeft();
			double bottom = getInsets().getBottom();
			double height = getHeight() - top - bottom;
			double childWidth = getSkinnable().getWidthPerItem();

			double padding = getWidth() - pager.getItemsPerPage() * childWidth;
			left += padding / 2;

			for( Node child : getChildren() )
			{
				layoutInArea( child, left, top, childWidth, height, height, HPos.CENTER, VPos.BOTTOM );
				left += childWidth;
			}
		}
	}
}

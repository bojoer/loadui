package com.eviware.loadui.ui.fx.control.skin;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.shape.RectangleBuilder;
import javafx.util.Callback;
import javafx.util.StringConverter;

import com.eviware.loadui.ui.fx.control.Carousel;
import com.eviware.loadui.ui.fx.util.Pager;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.SkinBase;

public class CarouselSkin<E extends Node> extends SkinBase<Carousel<E>, BehaviorBase<Carousel<E>>>
{
	private final Carousel<E> carousel;
	private final Pager<Node> pager;
	private final IntegerProperty depth = new SimpleIntegerProperty( this, "depth", 2 );

	private static Node createPlaceholder()
	{
		return RectangleBuilder.create().build();
	}

	public CarouselSkin( final Carousel<E> carousel )
	{
		super( carousel, new BehaviorBase<>( carousel ) );

		this.carousel = carousel;
		pager = new Pager<>();
		pager.setFluentMode( true );
		final ObservableList<Node> blankNodesLeft = FXCollections.observableArrayList();
		final ObservableList<Node> blankNodesRight = FXCollections.observableArrayList();
		depth.addListener( new ChangeListener<Number>()
		{
			@Override
			public void changed( ObservableValue<? extends Number> arg0, Number oldValue, Number newValue )
			{
				List<Node> newBlankNodesLeft = new ArrayList<>();
				List<Node> newBlankNodesRight = new ArrayList<>();
				for( int i = newValue.intValue(); i > 0; i-- )
				{
					newBlankNodesLeft.add( createPlaceholder() );
					newBlankNodesRight.add( createPlaceholder() );
				}
				blankNodesLeft.setAll( newBlankNodesLeft );
				blankNodesRight.setAll( newBlankNodesRight );
			}
		} );

		for( int i = depth.get(); i > 0; i-- )
		{
			blankNodesLeft.add( createPlaceholder() );
			blankNodesRight.add( createPlaceholder() );
		}

		@SuppressWarnings( "unchecked" )
		ObservableList<Node> pagerItems = FXCollections.concat( blankNodesLeft,
				( ObservableList<Node> )carousel.getItems(), blankNodesRight );
		Bindings.bindContent( pager.getItems(), pagerItems );
		pager.itemsPerPageProperty().bind( depth.multiply( 2 ).add( 1 ) );

		carousel.selectedProperty().addListener( new ChangeListener<E>()
		{
			@Override
			public void changed( ObservableValue<? extends E> arg0, E oldValue, E newValue )
			{
				pager.setPage( carousel.getItems().indexOf( newValue ) );
			}
		} );

		ComboBox<E> comboBox = new ComboBox<>( carousel.getItems() );
		comboBox.setCellFactory( new Callback<ListView<E>, ListCell<E>>()
		{
			@Override
			public ListCell<E> call( ListView<E> listView )
			{
				return new ListCell<E>()
				{
					@Override
					protected void updateItem( E item, boolean empty )
					{
						super.updateItem( item, empty );
						StringConverter<E> converter = carousel.getConverter();
						setText( item == null ? "" : ( converter == null ? item.toString() : converter.toString( item ) ) );
					}
				};
			}
		} );
		comboBox.prefWidthProperty().bind( widthProperty() );
		comboBox.valueProperty().bindBidirectional( carousel.selectedProperty() );

		getChildren().setAll(
				VBoxBuilder.create().styleClass( "vbox" )
						.children( new VisualScroller(), new Separator(), carousel.getLabel(), comboBox ).build() );
	}

	private void selectPrevious()
	{
		int index = carousel.getItems().indexOf( carousel.getSelected() );
		if( index > 0 )
		{
			carousel.setSelected( carousel.getItems().get( index - 1 ) );
		}
	}

	private void selectNext()
	{
		int index = carousel.getItems().indexOf( carousel.getSelected() );
		if( index + 1 < carousel.getItems().size() )
		{
			carousel.setSelected( carousel.getItems().get( index + 1 ) );
		}
	}

	private class VisualScroller extends HBox
	{
		private final Button prevButton = new Button();
		private final Button nextButton = new Button();

		private VisualScroller()
		{
			getStyleClass().add( "carousel-display" );

			prevButton.setOnAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent arg0 )
				{
					selectPrevious();
				}
			} );
			prevButton.disableProperty().bind( pager.pageProperty().isEqualTo( 0 ) );

			nextButton.setOnAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent arg0 )
				{
					selectNext();
				}
			} );
			nextButton.disableProperty().bind( pager.pageProperty().greaterThanOrEqualTo( pager.numPagesProperty() ) );

			CarouselDisplay display = new CarouselDisplay();
			HBox.setHgrow( display, Priority.ALWAYS );

			getChildren().setAll( prevButton, display, nextButton );
		}
	}

	private class CarouselDisplay extends Pane
	{
		private CarouselDisplay()
		{
			getStyleClass().add( "item-display" );

			pager.getShownItems().addListener( new InvalidationListener()
			{
				@Override
				public void invalidated( Observable arg0 )
				{
					updateChildren();
				}
			} );

			updateChildren();
		}

		@Override
		protected void layoutChildren()
		{
			//TODO: Improve this and add effects.
			List<Node> managed = getManagedChildren();

			double width = getWidth();
			double height = getHeight();
			double top = getInsets().getTop();
			double right = getInsets().getRight();
			double left = getInsets().getLeft();
			double bottom = getInsets().getBottom();
			double step = ( width - left - right ) / managed.size();

			HPos hpos = HPos.CENTER;
			left -= step;
			right -= step;
			for( int i = 0; i < managed.size(); i++ )
			{
				if( i % 2 == 0 )
				{
					if( i == managed.size() - 1 )
					{
						hpos = HPos.CENTER;
					}
					else
					{
						left += step;
						hpos = HPos.LEFT;
					}
				}
				else
				{
					right += step;
					hpos = HPos.RIGHT;
				}

				Node child = managed.get( i );
				layoutInArea( child, left, top, width - left - right, height - top - bottom, height / 2, Insets.EMPTY,
						false, false, hpos, VPos.CENTER );
			}
		}

		private void updateChildren()
		{
			List<Node> items = pager.getShownItems();
			List<Node> displayOrder = new ArrayList<>();
			for( int i = 0; i < items.size() / 2; i++ )
			{
				displayOrder.add( items.get( i ) );
				displayOrder.add( items.get( items.size() - ( i + 1 ) ) );
			}
			displayOrder.add( items.get( items.size() / 2 ) );

			getChildren().setAll( displayOrder );
		}
	}
}

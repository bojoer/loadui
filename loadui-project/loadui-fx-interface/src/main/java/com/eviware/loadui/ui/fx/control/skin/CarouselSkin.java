package com.eviware.loadui.ui.fx.control.skin;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.effect.ColorAdjustBuilder;
import javafx.scene.effect.Reflection;
import javafx.scene.effect.ReflectionBuilder;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.shape.RectangleBuilder;
import javafx.util.Callback;
import javafx.util.Duration;
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
						.children( new CarouselDisplay(), new Separator(), carousel.getLabel(), comboBox ).build() );
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

	private class CarouselDisplay extends HBox
	{
		private final Button prevButton = new Button();
		private final Button nextButton = new Button();

		private CarouselDisplay()
		{
			getStyleClass().add( "carousel-display" );

			final ItemDisplay display = new ItemDisplay();

			prevButton.setOnAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent arg0 )
				{
					display.animatePrevious();
				}
			} );
			prevButton.disableProperty().bind( pager.pageProperty().isEqualTo( 0 ) );

			nextButton.setOnAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent event )
				{
					display.animateNext();
				}
			} );
			nextButton.disableProperty().bind(
					pager.pageProperty().greaterThanOrEqualTo( pager.numPagesProperty().subtract( 1 ) ) );

			HBox.setHgrow( display, Priority.ALWAYS );

			getChildren().setAll( prevButton, display, nextButton );
		}
	}

	private class ItemDisplay extends Pane
	{
		private final Reflection reflection = ReflectionBuilder.create().build();
		private final DoubleProperty rotationStep = new SimpleDoubleProperty( this, "rotationStep" );
		private final Timeline animateNextTimeline = TimelineBuilder
				.create()
				.onFinished( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent arg0 )
					{
						animate();
					}
				} )
				.keyFrames( new KeyFrame( new Duration( 150 ), new KeyValue( rotationStep, -0.5 ) ),
						new KeyFrame( new Duration( 150 ), new EventHandler<ActionEvent>()
						{
							@Override
							public void handle( ActionEvent event )
							{
								selectNext();
							}
						}, new KeyValue( rotationStep, 0.5 ) ),
						new KeyFrame( new Duration( 300 ), new KeyValue( rotationStep, 0 ) ) ).build();

		private final Timeline animatePrevTimeline = TimelineBuilder
				.create()
				.onFinished( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent arg0 )
					{
						animate();
					}
				} )
				.keyFrames( new KeyFrame( new Duration( 150 ), new KeyValue( rotationStep, 0.5 ) ),
						new KeyFrame( new Duration( 150 ), new EventHandler<ActionEvent>()
						{
							@Override
							public void handle( ActionEvent event )
							{
								selectPrevious();
							}
						}, new KeyValue( rotationStep, -0.5 ) ),
						new KeyFrame( new Duration( 300 ), new KeyValue( rotationStep, 0 ) ) ).build();
		private int animationQueue = 0;

		private ItemDisplay()
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

			rotationStep.addListener( new InvalidationListener()
			{
				@Override
				public void invalidated( Observable arg0 )
				{
					requestLayout();
				}
			} );

			setOnScroll( new EventHandler<ScrollEvent>()
			{
				@Override
				public void handle( ScrollEvent event )
				{
					if( event.getDeltaY() < 0 )
					{
						animateNext();
					}
					else
					{
						animatePrevious();
					}
				}
			} );

			updateChildren();
		}

		@Override
		protected void layoutChildren()
		{
			List<Node> managed = pager.getShownItems();

			double top = getInsets().getTop();
			double right = getInsets().getRight();
			double left = getInsets().getLeft();
			double bottom = getInsets().getBottom();
			double width = getWidth() - left - right;
			double height = getHeight() - top - bottom;
			double childWidth = getMaxPrefWidth( managed, height );

			double span = ( width - childWidth ) / 2;
			double radianStep = Math.PI / ( managed.size() - 1 );

			for( int i = 0; i < managed.size(); i++ )
			{
				double radians = ( rotationStep.get() + i ) * radianStep;

				double z = Math.sin( radians ) / 2 + 0.5;
				double areaX = left + ( 1 - Math.cos( radians ) ) * span;
				Node child = managed.get( i );
				child.setScaleX( z );
				child.setScaleY( z );
				child.setEffect( ColorAdjustBuilder.create().brightness( z - 1 ).input( reflection ).build() );
				layoutInArea( child, areaX, top, childWidth, height, height / 2, HPos.CENTER, VPos.CENTER );
			}
		}

		private void animateNext()
		{
			if( animationQueue < 0 )
			{
				animationQueue = 1;
				animateNextTimeline.setRate( animatePrevTimeline.getRate() * 1.25 );
			}
			else
			{
				animationQueue++ ;
				animateNextTimeline.setRate( animateNextTimeline.getRate() * 1.25 );
			}
			animate();
		}

		private void animatePrevious()
		{
			if( animationQueue > 0 )
			{
				animationQueue = -1;
				animatePrevTimeline.setRate( animateNextTimeline.getRate() * 1.25 );
			}
			else
			{
				animationQueue-- ;
				animatePrevTimeline.setRate( animatePrevTimeline.getRate() * 1.25 );
			}
			animate();
		}

		private void resetAnimation()
		{
			animateNextTimeline.setRate( 1 );
			animatePrevTimeline.setRate( 1 );
			animationQueue = 0;
		}

		private void animate()
		{
			if( animatePrevTimeline.getStatus() == Status.RUNNING || animateNextTimeline.getStatus() == Status.RUNNING )
			{
				return;
			}

			if( animationQueue > 0 && pager.getPage() < pager.getNumPages() - 1 )
			{
				animateNextTimeline.setRate( animationQueue-- );
				animateNextTimeline.playFromStart();
			}
			else if( animationQueue < 0 && pager.getPage() > 0 )
			{
				animatePrevTimeline.setRate( -animationQueue++ );
				animatePrevTimeline.playFromStart();
			}
			else
			{
				resetAnimation();
			}
		}

		private double getMaxPrefWidth( List<Node> managed, double height )
		{
			double maxWidth = -1;
			for( Node child : managed )
			{
				maxWidth = Math.max( maxWidth, child.minWidth( height ) );
			}

			return maxWidth;
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
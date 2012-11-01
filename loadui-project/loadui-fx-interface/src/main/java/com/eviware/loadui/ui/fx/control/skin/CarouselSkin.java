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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.effect.ColorAdjustBuilder;
import javafx.scene.effect.Reflection;
import javafx.scene.effect.ReflectionBuilder;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
	private final Pager<Node> pager = new Pager<>();
	private final IntegerProperty depth = new SimpleIntegerProperty( this, "depth", 2 );

	private static Node createPlaceholder()
	{
		return RectangleBuilder.create().id( "placeholder" ).build();
	}

	public CarouselSkin( final Carousel<E> carousel )
	{
		super( carousel, new BehaviorBase<>( carousel ) );

		this.carousel = carousel;

		pager.setFluentMode( true );
		pager.itemsPerPageProperty().bind( depth.multiply( 2 ).add( 1 ) );

		InvalidationListener updatePagerItems = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				List<Node> nodes = new ArrayList<>();
				for( int i = depth.get(); i > 0; i-- )
				{
					nodes.add( createPlaceholder() );
				}
				nodes.addAll( carousel.getItems() );
				for( int i = depth.get(); i > 0; i-- )
				{
					nodes.add( createPlaceholder() );
				}

				pager.getItems().setAll( nodes );
			}
		};

		carousel.getItems().addListener( updatePagerItems );
		depth.addListener( updatePagerItems );
		updatePagerItems.invalidated( null );

		InvalidationListener updatePage = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				int index = carousel.getItems().indexOf( carousel.getSelected() );
				pager.setPage( Math.max( index, 0 ) );
			}
		};

		carousel.selectedProperty().addListener( updatePage );
		carousel.getItems().addListener( updatePage );
		updatePage.invalidated( null );

		ComboBox<E> comboBox = new ComboBox<>();
		comboBox.setMaxWidth( Double.MAX_VALUE );
		Callback<ListView<E>, ListCell<E>> cellFactory = new Callback<ListView<E>, ListCell<E>>()
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
		};
		comboBox.setButtonCell( cellFactory.call( null ) );
		comboBox.setCellFactory( cellFactory );
		comboBox.setItems( carousel.getItems() );
		comboBox.valueProperty().bindBidirectional( carousel.selectedProperty() );

		CarouselDisplay carouselDisplay = new CarouselDisplay();
		VBox.setVgrow( carouselDisplay, Priority.SOMETIMES );
		VBox vbox = VBoxBuilder.create().styleClass( "vbox" )
				.children( carouselDisplay, new Separator(), carousel.getLabel(), comboBox ).build();

		getChildren().setAll( vbox );
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

	private class CarouselDisplay extends StackPane
	{
		private final Button prevButton = ButtonBuilder.create().styleClass( "nav", "prev", "left" ).build();
		private final Button nextButton = ButtonBuilder.create().styleClass( "nav", "next", "right" ).build();

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

			setAlignment( prevButton, Pos.CENTER_LEFT );
			setAlignment( nextButton, Pos.CENTER_RIGHT );
			getChildren().setAll( display, prevButton, nextButton );
		}
	}

	private class ItemDisplay extends Pane
	{
		private final Reflection reflection = ReflectionBuilder.create().build();
		private final Region mouseBlocker = new Region();
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

			mouseBlocker.setMouseTransparent( false );
			mouseBlocker.setOnMouseClicked( new EventHandler<MouseEvent>()
			{
				@Override
				public void handle( MouseEvent event )
				{
					if( event.getButton() == MouseButton.PRIMARY )
					{
						if( event.getX() < mouseBlocker.getWidth() / 2 )
						{
							animatePrevious();
						}
						else
						{
							animateNext();
						}
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

			layoutInArea( mouseBlocker, left, top, width, height, height / 2, HPos.CENTER, VPos.CENTER );
		}

		@Override
		protected double computePrefWidth( double height )
		{
			return getInsets().getLeft() + 3 * getMaxPrefWidth( carousel.getItems(), height ) + getInsets().getRight();
		}

		@Override
		protected double computePrefHeight( double width )
		{
			return computeMinHeight( width );
		}

		@Override
		protected double computeMinHeight( double width )
		{
			return getInsets().getTop() + getMaxPrefHeight( carousel.getItems(), width ) + getInsets().getBottom();
		}

		@Override
		protected double computeMinWidth( double height )
		{
			return getInsets().getLeft() + getMaxPrefWidth( carousel.getItems(), height ) + getInsets().getRight();
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

		private double getMaxPrefWidth( List<? extends Node> managed, double height )
		{
			double maxWidth = -1;
			for( Node child : managed )
			{
				maxWidth = Math.max( maxWidth, child.prefWidth( height ) );
			}

			return maxWidth;
		}

		private double getMaxPrefHeight( List<? extends Node> managed, double width )
		{
			double maxHeight = -1;
			for( Node child : managed )
			{
				maxHeight = Math.max( maxHeight, child.prefHeight( width ) );
			}

			return maxHeight;
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
			displayOrder.add( mouseBlocker );
			if( items.size() > depth.get() * 2 )
			{
				displayOrder.add( items.get( items.size() / 2 ) );
			}

			getChildren().setAll( displayOrder );
		}
	}
}
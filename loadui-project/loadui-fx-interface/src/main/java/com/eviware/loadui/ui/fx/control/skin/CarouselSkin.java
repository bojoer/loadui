package com.eviware.loadui.ui.fx.control.skin;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.shape.Rectangle;

import com.eviware.loadui.ui.fx.control.Carousel;
import com.eviware.loadui.ui.fx.util.Pager;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.SkinBase;

public class CarouselSkin<E extends Node> extends SkinBase<Carousel<E>, BehaviorBase<Carousel<E>>>
{
	private final Pager<Node> pager;

	public CarouselSkin( Carousel<E> control )
	{
		super( control, new BehaviorBase<>( control ) );

		pager = new Pager<>();
		pager.setFluentMode( true );
		ObservableList<Node> blankNodeList = FXCollections.<Node> singletonObservableList( new Rectangle() );
		@SuppressWarnings( "unchecked" )
		ObservableList<Node> pagerItems = FXCollections.concat( blankNodeList,
				( ObservableList<Node> )control.getItems(), blankNodeList );
		Bindings.bindContent( pager.getItems(), pagerItems );
		pager.setItemsPerPage( 3 );

		ComboBox<E> comboBox = new ComboBox<>();
		comboBox.converterProperty().bind( control.converterProperty() );
		comboBox.prefWidthProperty().bind( widthProperty() );
		Bindings.bindContent( comboBox.getItems(), control.getItems() );
		comboBox.valueProperty().bindBidirectional( control.selectedProperty() );

		getChildren().setAll(
				VBoxBuilder.create().styleClass( "vbox" )
						.children( new VisualScroller(), new Separator(), control.getLabel(), comboBox ).build() );
	}

	private class VisualScroller extends HBox
	{
		private final Button upButton = new Button();
		private final Button downButton = new Button();

		private VisualScroller()
		{
			getStyleClass().add( "carousel-display" );
			CarouselDisplay display = new CarouselDisplay();
			HBox.setHgrow( display, Priority.ALWAYS );
			getChildren().setAll( upButton, display, downButton );
		}
	}

	private class CarouselDisplay extends HBox
	{
		private CarouselDisplay()
		{
			getStyleClass().add( "item-display" );
			Bindings.bindContent( getChildren(), pager.getShownItems() );
		}
	}
}

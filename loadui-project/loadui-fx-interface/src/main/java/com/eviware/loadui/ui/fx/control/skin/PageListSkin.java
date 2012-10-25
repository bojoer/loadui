package com.eviware.loadui.ui.fx.control.skin;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.layout.VBoxBuilder;

import com.eviware.loadui.ui.fx.control.PageList;
import com.eviware.loadui.ui.fx.util.Pager;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.SkinBase;

public class PageListSkin extends SkinBase<PageList<Node>, BehaviorBase<PageList<Node>>>
{
	private final Pager<Node> pager;

	public PageListSkin( PageList<Node> pageList )
	{
		super( pageList, new BehaviorBase<>( pageList ) );

		pager = new Pager<>( pageList.getItems() );
		pager.setFluentMode( true );

		final HBox itemBox = HBoxBuilder.create().styleClass( "item-box" ).alignment( Pos.BOTTOM_CENTER ).build();
		HBox.setHgrow( itemBox, Priority.ALWAYS );
		Bindings.bindContent( itemBox.getChildren(), pager.getShownItems() );

		pager.itemsPerPageProperty().bind(
				itemBox.widthProperty().divide( pageList.widthPerItemProperty().add( itemBox.spacingProperty() ) ) );

		Label label = pageList.getLabel();
		StackPane.setAlignment( label, Pos.TOP_LEFT );

		Label pageNum = new Label();
		pageNum.textProperty().bind(
				Bindings.format( "Page %d of %d", pager.pageProperty().add( 1 ), pager.numPagesProperty() ) );
		StackPane.setAlignment( pageNum, Pos.TOP_RIGHT );

		Button prevButton = ButtonBuilder.create().text( "Prev" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				pager.setPage( pager.getPage() - 1 );
			}
		} ).build();
		prevButton.disableProperty().bind( pager.pageProperty().isEqualTo( 0 ) );

		Button nextButton = ButtonBuilder.create().text( "Next" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				pager.setPage( pager.getPage() + 1 );
			}
		} ).build();
		nextButton.disableProperty().bind(
				pager.pageProperty().greaterThanOrEqualTo( pager.numPagesProperty().subtract( 1 ) ) );

		getChildren().setAll(
				VBoxBuilder
						.create()
						.children(
								StackPaneBuilder.create().children( label, pageNum ).build(),
								HBoxBuilder.create().alignment( Pos.CENTER ).children( prevButton, itemBox, nextButton )
										.build(), new Separator() ).build() );
	}
}

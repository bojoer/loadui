package com.eviware.loadui.ui.fx.views.assertions;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuButtonBuilder;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.MenuItemsProvider;
import com.eviware.loadui.ui.fx.MenuItemsProvider.Options;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.util.Properties;

public class AssertionView extends VBox implements Deletable
{
	protected static final Logger log = LoggerFactory.getLogger( AssertionView.class );

	private final Runnable deleteAction = new Runnable()
	{
		@Override
		public void run()
		{
			delete();
		}
	};

	public AssertionView( final AssertionItem<?> assertion )
	{
		getStyleClass().add( "assertion-view" );

		MenuItem[] menuItems = MenuItemsProvider.createWith( this, assertion, Options.are().delete( deleteAction ) )
				.items();

		final MenuButton menuButton = MenuButtonBuilder.create().items( menuItems ).build();
		menuButton.textProperty().bind( Properties.forLabel( assertion ) );
		menuButton.getItems().setAll( menuItems );
		final ContextMenu ctxMenu = ContextMenuBuilder.create().items( menuItems ).build();

		setOnContextMenuRequested( new EventHandler<ContextMenuEvent>()
		{
			@Override
			public void handle( ContextMenuEvent event )
			{
				// never show contextMenu when on top of the menuButton
				if( !NodeUtils.isMouseOn( menuButton ) )
				{
					MenuItemsProvider.showContextMenu( menuButton, ctxMenu );
					event.consume();
				}
			}
		} );

		final Label failures = new Label( "0" );
		assertion.addEventListener( BaseEvent.class, new com.eviware.loadui.api.events.EventHandler<BaseEvent>()
		{
			@Override
			public void handleEvent( BaseEvent event )
			{
				if( AssertionItem.FAILURE_COUNT.equals( event.getKey() ) )
				{
					Platform.runLater( new Runnable()
					{
						@Override
						public void run()
						{
							failures.setText( Long.toString( assertion.getFailureCount() ) );
							log.debug( "getFailureCount:" + assertion.getFailureCount() );
						}
					} );
				}
			}
		} );

		HBox display = HBoxBuilder.create().spacing( 20 ).build();
		display.getStyleClass().add( "display" );

		final Label constraintType = new Label( assertion.getConstraint().constraintType() );
		final Label constraintValue = new Label( assertion.getConstraint().value() );
		VBox rangeVbox = VBoxBuilder.create().children( constraintType, constraintValue ).build();

		String tolerance = assertion.getToleranceAllowedOccurrences() == 0 ? "-" : assertion
				.getToleranceAllowedOccurrences() + " times / " + assertion.getTolerancePeriod() + " sec";
		final Label toleranceLabel = new Label( tolerance );
		VBox toleranceVbox = VBoxBuilder.create().children( new Label( "Tolerance" ), toleranceLabel ).build();
		VBox failuresVbox = VBoxBuilder.create().children( new Label( "Failures" ), failures ).build();
		display.getChildren().setAll( rangeVbox, toleranceVbox, failuresVbox );

		String holderName = "";
		if( assertion.getParent() instanceof Labeled )
		{
			holderName = ( ( Labeled )assertion.getParent() ).getLabel();
		}
		Label holderLabel = LabelBuilder.create().minWidth( 200.0 ).text( holderName ).build();

		Label assertionLegend = LabelBuilder.create().text( String.valueOf( assertion.getValue() ) )
				.maxWidth( Double.MAX_VALUE ).build();

		HBox contentPane = HBoxBuilder.create().children( holderLabel, assertionLegend, display )
				.styleClass( "content-pane" ).spacing( 30 ).build();
		HBox.setHgrow( assertionLegend, Priority.ALWAYS );

		getChildren().setAll( menuButton, contentPane );
	}

	@Override
	public void delete()
	{
		log.debug( "Deleting Assertion" );
		//TODO remove assertion from any charts it may be in
	}

}

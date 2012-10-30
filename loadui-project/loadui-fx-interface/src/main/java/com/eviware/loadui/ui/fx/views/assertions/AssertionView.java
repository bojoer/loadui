package com.eviware.loadui.ui.fx.views.assertions;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuButtonBuilder;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.traits.Deletable;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.Properties;

public class AssertionView<T> extends VBox implements Deletable
{
	protected static final Logger log = LoggerFactory.getLogger( AssertionView.class );
	private final AssertionItem<T> assertion;

	public AssertionView( final AssertionItem<T> assertion )
	{
		this.assertion = assertion;

		//setText( item.getLabel() );

		MenuItem rename = MenuItemBuilder.create().text( "Rename" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				rename();
			}
		} ).build();
		MenuItem delete = MenuItemBuilder.create().text( "Delete" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				delete();
			}
		} ).build();

		MenuButton menu = MenuButtonBuilder.create().items( rename, delete ).build();
		menu.textProperty().bind( Properties.forLabel( assertion ) );

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
		Label holderLabel = new Label( holderName );

		Label assertionLegend = new Label( String.valueOf( assertion.getValue() ) );

		HBox hBox = HBoxBuilder.create().children( holderLabel, assertionLegend, display ).spacing( 30 ).build();

		getChildren().setAll( menu, hBox );
	}

	@Override
	public void delete()
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_DELETE, assertion ) );
	}

	public void rename()
	{
		if( assertion instanceof AssertionItem.Mutable<?> )
			fireEvent( IntentEvent.create( IntentEvent.INTENT_RENAME, ( AssertionItem.Mutable<?> )assertion ) );
	}
}

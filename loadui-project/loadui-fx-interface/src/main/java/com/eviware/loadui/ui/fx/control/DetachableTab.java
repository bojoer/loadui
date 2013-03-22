package com.eviware.loadui.ui.fx.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.scene.layout.PaneBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.WindowEvent;

import com.eviware.loadui.ui.fx.api.LoaduiFXConstants;
import com.eviware.loadui.ui.fx.api.intent.AbortableBlockingTask;
import com.eviware.loadui.ui.fx.api.intent.BlockingTask;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;

public class DetachableTab extends Tab
{
	private final BooleanProperty detachedProperty = new SimpleBooleanProperty( false );
	private int detachedId;

	public final BooleanProperty detachedProperty()
	{
		return detachedProperty;
	}

	public final boolean isDetached()
	{
		return detachedProperty.get();
	}

	public final void setDetached( boolean detached )
	{
		detachedProperty.set( detached );
	}

	private final ObjectProperty<Pane> detachableContentProperty = new SimpleObjectProperty<>();

	public final ObjectProperty<? extends Pane> detachableContentProperty()
	{
		return detachableContentProperty;
	}

	public final Pane getDetachableContent()
	{
		return detachableContentProperty.get();
	}

	public final void setDetachableContent( Node eventFirer, Pane detachableContent )
	{
		detachableContentProperty.set( detachableContent );
		this.eventFirer = eventFirer;
	}

	private Stage detachedStage;
	private Scene scene;
	private Node eventFirer;
	private final DetachedTabsHolder tabRefs;

	public DetachableTab()
	{
		this( null, DetachedTabsHolder.get() );
	}

	public DetachableTab( String label, DetachedTabsHolder tabRefs )
	{
		super( label );
		this.tabRefs = tabRefs;

		getStyleClass().add( "detachable-tab" );

		contentProperty().bind(
				Bindings
						.when( detachedProperty )
						.<Pane> then(
								PaneBuilder.create().id( "placeholder" ).style( "-fx-background-color: darkgrey;" ).build() )
						.otherwise( detachableContentProperty ) );

		detachedProperty.addListener( new ChangeListener<Boolean>()
		{

			@Override
			public void changed( ObservableValue<? extends Boolean> _, Boolean oldValue, Boolean hasToDetach )
			{
				if( hasToDetach )
					doDetach();
				else
					doReattach();
			}
		} );

		Button detachButton = ButtonBuilder.create().styleClass( "styleable-graphic" )
				.onAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent arg0 )
					{
						setDetached( !isDetached() );
					}
				} ).build();
		detachButton.visibleProperty().bind( selectedProperty() );
		setGraphic( detachButton );
	}

	private void doDetach()
	{
		final StackPane detachedTabContainer;
		final Node detachableContent = getDetachableContent();
		detachedStage = StageBuilder
				.create()
				.icons( ( ( Stage )getTabPane().getScene().getWindow() ).getIcons() )
				.title( getText() )
				.width( getTabPane().getWidth() )
				.height( getTabPane().getHeight() )
				.scene(
						scene = SceneBuilder
								.create()
								.root(
										detachedTabContainer = StackPaneBuilder.create().children( detachableContent )
												.styleClass( "detached-content" ).build() )
								.stylesheets( LoaduiFXConstants.getLoaduiStylesheets() ).build() ).build();
		detachableContent.setVisible( true );
		detachedStage.setOnHidden( new EventHandler<WindowEvent>()
		{
			@Override
			public void handle( WindowEvent event )
			{
				setDetached( false );
			}
		} );

		BlockingTask.install( scene );
		AbortableBlockingTask.install( scene );
		detachedId = tabRefs.add( detachedTabContainer );

		final EventHandler<Event> intentHandler = new EventHandler<Event>()
		{

			@Override
			public void handle( Event event )
			{
				if( !event.isConsumed() )
					eventFirer.fireEvent( event );
			}
		};
		detachedStage.addEventHandler( IntentEvent.ANY, intentHandler );

		detachedStage.show();
	}

	private void doReattach()
	{
		if( detachedStage != null )
		{
			tabRefs.remove( detachedId );
			detachedStage.setOnHidden( null );
			detachedStage.close();
			detachedStage = null;
		}
		if( scene != null )
		{
			BlockingTask.uninstall( scene );
			AbortableBlockingTask.uninstall( scene );
			scene = null;
		}
	}

}

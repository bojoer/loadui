package com.eviware.loadui.ui.fx.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Tab;
import javafx.scene.layout.RegionBuilder;
import javafx.scene.layout.StackPaneBuilder;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.WindowEvent;

import com.eviware.loadui.ui.fx.api.intent.BlockingTask;

public class DetachableTab extends Tab
{
	private final BooleanProperty detachedProperty = new SimpleBooleanProperty( false );

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

	private final ObjectProperty<Node> detachableContentProperty = new SimpleObjectProperty<>();

	public final ObjectProperty<Node> detachableContentProperty()
	{
		return detachableContentProperty;
	}

	public final Node getDetachableContent()
	{
		return detachableContentProperty.get();
	}

	public final void setDetachableContent( Node detachableContent )
	{
		detachableContentProperty.set( detachableContent );
	}

	private Stage detachedStage;

	public DetachableTab()
	{
		this( null );
	}

	public DetachableTab( String label )
	{
		super( label );

		contentProperty().bind(
				Bindings
						.when( detachedProperty )
						.<Node> then(
								RegionBuilder.create().id( "placeholder" ).style( "-fx-background-color: darkgrey;" ).build() )
						.otherwise( detachableContentProperty ) );

		detachedProperty.addListener( new ChangeListener<Boolean>()
		{
			@Override
			public void changed( ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2 )
			{
				if( isDetached() )
				{
					final Node detachableContent = getDetachableContent();
					Scene scene;
					detachedStage = StageBuilder
							.create()
							.title( getText() )
							.icons( ( ( Stage )getTabPane().getScene().getWindow() ).getIcons() )
							.width( getTabPane().getWidth() )
							.height( getTabPane().getHeight() )
							.scene(
									scene = SceneBuilder.create()
											.root( StackPaneBuilder.create().children( detachableContent ).build() ).build() )
							.build();
					detachableContent.setVisible( true );
					detachedStage.setOnHidden( new EventHandler<WindowEvent>()
					{
						@Override
						public void handle( WindowEvent event )
						{
							setDetached( false );
						}
					} );
					//TODO: Forward all IntentEvents to the parent scene?
					BlockingTask.install( scene );
					detachedStage.show();
				}
				else
				{
					if( detachedStage != null )
					{
						detachedStage.setOnHidden( null );
						detachedStage.close();
						detachedStage = null;
					}
				}
			}
		} );

		setGraphic( ButtonBuilder.create().text( "D" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent arg0 )
			{
				setDetached( !isDetached() );
			}
		} ).build() );
	}
}

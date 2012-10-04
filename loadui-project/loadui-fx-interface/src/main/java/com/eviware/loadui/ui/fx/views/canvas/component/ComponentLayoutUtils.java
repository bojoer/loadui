package com.eviware.loadui.ui.fx.views.canvas.component;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.control.TooltipBuilder;
import javafx.scene.layout.VBoxBuilder;

import org.tbee.javafx.scene.layout.MigPane;

import com.eviware.loadui.api.layout.ActionLayoutComponent;
import com.eviware.loadui.api.layout.LabelLayoutComponent;
import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.api.layout.SeparatorLayoutComponent;
import com.eviware.loadui.api.layout.TableLayoutComponent;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.layout.FormattedString;

public class ComponentLayoutUtils
{
	public static Node instantiateLayout( LayoutComponent component )
	{
		//Legacy rules that we need, that pre-emp anything else
		if( component.has( "widget" ) )
		{
			//TODO: Add all the stuff from the old WidgetRegistry
			if( "display".equals( component.get( "widget" ) ) )
			{
				LayoutContainer container = ( LayoutContainer )component;
				MigPane pane = new MigPane( container.getLayoutConstraints(), container.getColumnConstraints(),
						container.getRowConstraints() );
				pane.getStyleClass().add( "display" );
				for( LayoutComponent child : container )
				{
					pane.add( instantiateLayout( child ), child.getConstraints() );
				}
				return pane;
			}
		}
		else if( component.has( "fString" ) )
		{
			final FormattedString fString = ( FormattedString )component.get( "fString" );
			final Label label = new Label( fString.getCurrentValue() );
			fString.addObserver( new Observer()
			{
				@Override
				public void update( Observable o, Object arg )
				{
					Platform.runLater( new Runnable()
					{
						@Override
						public void run()
						{
							label.setText( fString.getCurrentValue() );
						}
					} );
				}
			} );

			return VBoxBuilder.create()
					.children( LabelBuilder.create().text( ( String )component.get( "label" ) ).build(), label ).build();
		}

		if( component instanceof LayoutContainer )
		{
			LayoutContainer container = ( LayoutContainer )component;
			MigPane pane = new MigPane( container.getLayoutConstraints(), container.getColumnConstraints(),
					container.getRowConstraints() );
			for( LayoutComponent child : container )
			{
				pane.add( instantiateLayout( child ), child.getConstraints() );
			}
			return pane;
		}
		else if( component instanceof ActionLayoutComponent )
		{
			final ActionLayoutComponent action = ( ActionLayoutComponent )component;
			final Button button = ButtonBuilder.create().text( action.getLabel() ).disable( !action.isEnabled() ).build();
			button.setOnAction( new EventHandler<ActionEvent>()
			{
				@Override
				public void handle( ActionEvent arg0 )
				{
					System.out.println( "CLICK: " + action );
					if( action.isAsynchronous() )
					{
						BeanInjector.getBean( ExecutorService.class ).submit( action.getAction() );
					}
					else
					{
						button.fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, action.getAction() ) );
					}
				}
			} );
			action.registerListener( new ActionLayoutComponent.ActionEnabledListener()
			{
				@Override
				public void stateChanged( ActionLayoutComponent source )
				{
					button.setDisable( !source.isEnabled() );
				}
			} );

			return button;
		}
		else if( component instanceof LabelLayoutComponent )
		{
			return new Label( ( ( LabelLayoutComponent )component ).getLabel() );
		}
		else if( component instanceof PropertyLayoutComponent )
		{
			//TODO: Make editable Property
			return new Label( ( ( PropertyLayoutComponent<?> )component ).getLabel() );
		}
		else if( component instanceof SeparatorLayoutComponent )
		{
			SeparatorLayoutComponent separator = ( SeparatorLayoutComponent )component;
			return new Separator( separator.isVertical() ? Orientation.VERTICAL : Orientation.HORIZONTAL );
		}
		else if( component instanceof TableLayoutComponent )
		{
			//TODO: Table stuff
			return new TableView<>();
		}

		return LabelBuilder.create().text( "Unhandled: " + component )
				.tooltip( TooltipBuilder.create().text( component.toString() ).build() )
				.style( "-fx-background-color: red;" ).maxWidth( 80 ).build();
	}
}

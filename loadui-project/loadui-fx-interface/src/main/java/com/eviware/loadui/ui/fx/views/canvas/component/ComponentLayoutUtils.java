package com.eviware.loadui.ui.fx.views.canvas.component;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TooltipBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBoxBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;

import com.eviware.loadui.api.layout.ActionLayoutComponent;
import com.eviware.loadui.api.layout.LabelLayoutComponent;
import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.api.layout.OptionsProvider;
import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.api.layout.SeparatorLayoutComponent;
import com.eviware.loadui.api.layout.TableLayoutComponent;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.impl.layout.OptionsProviderImpl;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.Knob;
import com.eviware.loadui.ui.fx.control.OptionsSlider;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.layout.FormattedString;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ComponentLayoutUtils
{
	protected static final Logger log = LoggerFactory.getLogger( ComponentLayoutUtils.class );

	@SuppressWarnings( "unchecked" )
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
			else if( "selectorWidget".equals( component.get( "widget" ) ) )
			{

				Iterable<String> options = ( Iterable<String> )component.get( "labels" );

				boolean showLabels = ( boolean )Objects.firstNonNull( component.get( "showLabels" ), true );
				OptionsSlider slider = new OptionsSlider( Iterables.filter( options, String.class ) );
				slider.setShowLabels( showLabels );

				if( component.has( "images" ) )
				{
					List<ImageView> images = Lists.newArrayList();
					Iterable<String> imageNames = ( Iterable<String> )component.get( "images" );

					for( String imageName : imageNames )
					{
						ImageView image = new ImageView( new Image( ComponentLayoutUtils.class.getClassLoader()
								.getResource( "images/options/" + imageName ).toExternalForm() ) );
						images.add( image );
					}
					slider.getImages().setAll( images );
				}

				Property<String> loadUiProperty = ( Property<String> )component.get( "selected" );
				slider.selectedProperty().bindBidirectional( Properties.convert( loadUiProperty ) );

				Label propertyLabel = LabelBuilder.create().text( ( String )component.get( "label" ) ).build();

				return VBoxBuilder.create().children( propertyLabel, slider ).build();
			}
		}
		else if( component.has( "component" ) )
		{
			Object c = component.get( "component" );
			if( c instanceof Node )
			{
				return ( Node )c;
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
			return createPropertyNode( ( PropertyLayoutComponent<?> )component );
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

	@SuppressWarnings( "unchecked" )
	public static Node createPropertyNode( PropertyLayoutComponent<?> property )
	{
		Class<?> type = property.getProperty().getType();
		Label propertyLabel = LabelBuilder.create().text( property.getLabel() ).build();
		if( property.isReadOnly() )
		{
			Label label = new Label();
			label.textProperty().bind( Bindings.convert( Properties.convert( property.getProperty() ) ) );
			return VBoxBuilder.create().children( propertyLabel, label ).build();
		}
		else if( property.has( "options" ) )
		{
			Object opts = property.get( "options" );
			OptionsProvider<Object> options;
			if( opts instanceof OptionsProvider<?> )
			{
				options = ( OptionsProvider<Object> )opts;
			}
			else if( opts instanceof Iterable<?> )
			{
				options = new OptionsProviderImpl<>( ( Iterable<Object> )opts );
			}
			else
			{
				options = new OptionsProviderImpl<>( opts );
			}

			OptionsSlider slider;
			if( options.iterator().next() instanceof String )
			{
				slider = new OptionsSlider( Lists.newArrayList( Iterables.filter( options, String.class ) ) );
				slider.selectedProperty().bindBidirectional(
						( javafx.beans.property.Property<String> )Properties.convert( property.getProperty() ) );
				slider.setSelected( property.getProperty().getStringValue() );
				log.debug( " slider.getSelected(): " + slider.getSelected() );
			}
			else
				throw new RuntimeException( "options just supports sliders at the moment" );

			return VBoxBuilder.create().children( propertyLabel, slider ).build();
		}
		else if( type == String.class )
		{
			TextField textField = new TextField();
			textField.textProperty().bindBidirectional( Properties.convert( ( Property<String> )property.getProperty() ) );
			return VBoxBuilder.create().children( propertyLabel, textField ).build();

		}
		else if( Number.class.isAssignableFrom( type ) )
		{
			Knob knob = new Knob( property.getLabel() );
			knob.valueProperty().bindBidirectional( Properties.convert( ( Property<Number> )property.getProperty() ) );
			if( property.has( "min" ) )
			{
				knob.setMin( ( ( Number )property.get( "min" ) ).doubleValue() );
			}
			if( property.has( "max" ) )
			{
				knob.setMax( ( ( Number )property.get( "max" ) ).doubleValue() );
			}
			if( property.has( "step" ) )
			{
				knob.setStep( ( ( Number )property.get( "step" ) ).doubleValue() );
			}
			if( property.has( "span" ) )
			{
				knob.setSpan( ( ( Number )property.get( "span" ) ).doubleValue() );
			}

			return knob;
		}
		else if( type == Boolean.class )
		{
			CheckBox checkBox = new CheckBox( property.getLabel() );
			checkBox.selectedProperty().bindBidirectional(
					Properties.convert( ( Property<Boolean> )property.getProperty() ) );
			return checkBox;
		}

		return LabelBuilder.create().text( "Unhandled: " + property )
				.tooltip( TooltipBuilder.create().text( property.toString() ).build() )
				.style( "-fx-background-color: red;" ).maxWidth( 80 ).build();
	}
}

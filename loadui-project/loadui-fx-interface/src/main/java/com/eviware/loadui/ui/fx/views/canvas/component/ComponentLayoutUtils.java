package com.eviware.loadui.ui.fx.views.canvas.component;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TooltipBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBoxBuilder;
import javafx.util.Callback;

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
import com.eviware.loadui.api.model.PropertyHolder;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.impl.layout.OptionsProviderImpl;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.Knob;
import com.eviware.loadui.ui.fx.control.OptionsSlider;
import com.eviware.loadui.ui.fx.input.SelectableImpl;
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

	public static Node createPropertyNode( PropertyLayoutComponent<?> propLayoutComp )
	{
		Class<?> type = propLayoutComp.getProperty().getType();
		Label propertyLabel = LabelBuilder.create().text( propLayoutComp.getLabel() ).build();
		if( propLayoutComp.isReadOnly() )
		{
			return createLabel( propLayoutComp, propertyLabel );
		}
		else if( propLayoutComp.has( "options" ) )
		{
			return createOptionsNode( propLayoutComp, propertyLabel );
		}
		else if( type == String.class )
		{
			return createTextNode( propLayoutComp, propertyLabel );

		}
		else if( Number.class.isAssignableFrom( type ) )
		{
			return createKnob( propLayoutComp );
		}
		else if( type == Boolean.class )
		{
			return createCheckBox( propLayoutComp );
		}

		return LabelBuilder.create().text( "Unhandled: " + propLayoutComp )
				.tooltip( TooltipBuilder.create().text( propLayoutComp.toString() ).build() )
				.style( "-fx-background-color: red;" ).maxWidth( 80 ).build();
	}

	@SuppressWarnings( "unchecked" )
	private static Node createCheckBox( PropertyLayoutComponent<?> propLayoutComp )
	{
		CheckBox checkBox = new CheckBox( propLayoutComp.getLabel() );
		javafx.beans.property.Property<Boolean> jfxProp = Properties.convert( ( Property<Boolean> )propLayoutComp
				.getProperty() );
		checkBox.selectedProperty().bindBidirectional( jfxProp );
		return nodeWithProperty( checkBox, jfxProp );
	}

	@SuppressWarnings( "unchecked" )
	private static Node createKnob( PropertyLayoutComponent<?> propLayoutComp )
	{
		Knob knob = new Knob( propLayoutComp.getLabel() );
		javafx.beans.property.Property<Number> jfxProp = Properties.convert( ( Property<Number> )propLayoutComp
				.getProperty() );
		knob.valueProperty().bindBidirectional( jfxProp );
		if( propLayoutComp.has( "min" ) )
		{
			knob.setMin( ( ( Number )propLayoutComp.get( "min" ) ).doubleValue() );
		}
		if( propLayoutComp.has( "max" ) )
		{
			knob.setMax( ( ( Number )propLayoutComp.get( "max" ) ).doubleValue() );
		}
		if( propLayoutComp.has( "step" ) )
		{
			knob.setStep( ( ( Number )propLayoutComp.get( "step" ) ).doubleValue() );
		}
		if( propLayoutComp.has( "span" ) )
		{
			knob.setSpan( ( ( Number )propLayoutComp.get( "span" ) ).doubleValue() );
		}

		return nodeWithProperty( knob, jfxProp );
	}

	@SuppressWarnings( "unchecked" )
	private static Node createTextNode( PropertyLayoutComponent<?> propLayoutComp, Label propertyLabel )
	{
		final TextField textField = new TextField();
		textField.focusedProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(javafx.beans.Observable _) {
				if ( textField.isFocused() )
					SelectableImpl.deselectAll();
			}
		});
		
		javafx.beans.property.Property<String> jfxProp = Properties.convert( ( Property<String> )propLayoutComp
				.getProperty() );
		textField.textProperty().bindBidirectional( jfxProp );
		return nodeWithProperty( VBoxBuilder.create().children( propertyLabel, textField ).build(), jfxProp );
	}

	@SuppressWarnings( "unchecked" )
	private static Node createOptionsNode( PropertyLayoutComponent<?> propLayoutComp, Label propertyLabel )
	{
		log.debug( "OPTIONS NODE: " + propLayoutComp );

		Object opts = propLayoutComp.get( "options" );
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

		if( "combobox".equalsIgnoreCase( ( String )propLayoutComp.get( "widget" ) ) )
		{
			final OptionsProvider<Object> finalOptions = options;

			ObservableList<Object> observableList = FXCollections.observableArrayList( Lists.newArrayList( options
					.iterator() ) );

			Callback<ListView<Object>, ListCell<Object>> cellFactory = new Callback<ListView<Object>, ListCell<Object>>()
			{
				@Override
				public ListCell<Object> call( ListView<Object> listView )
				{
					return new ListCell<Object>()
					{
						@Override
						protected void updateItem( Object item, boolean empty )
						{
							super.updateItem( item, empty );
							setText( finalOptions.labelFor( item ) );
						}
					};
				}
			};
			ComboBox<Object> comboBox = new ComboBox<>();
			comboBox.setButtonCell( cellFactory.call( null ) );
			comboBox.setCellFactory( cellFactory );
			comboBox.setItems( observableList );
			javafx.beans.property.Property<Object> jfxProp = ( javafx.beans.property.Property<Object> )Properties
					.convert( propLayoutComp.getProperty() );
			comboBox.valueProperty().bindBidirectional( jfxProp );

			return nodeWithProperty( VBoxBuilder.create().children( propertyLabel, comboBox ).build(), jfxProp );
		}

		OptionsSlider slider;
		javafx.beans.property.Property<String> jfxProp;

		if( options.iterator().next() instanceof String )
		{
			slider = new OptionsSlider( Lists.newArrayList( Iterables.filter( options, String.class ) ) );
			jfxProp = ( javafx.beans.property.Property<String> )Properties.convert( propLayoutComp.getProperty() );
			slider.selectedProperty().bindBidirectional( jfxProp );
			slider.setSelected( propLayoutComp.getProperty().getStringValue() );
			log.debug( " slider.getSelected(): " + slider.getSelected() );
		}
		else
			throw new RuntimeException( "options just supports sliders at the moment" );

		return nodeWithProperty( VBoxBuilder.create().children( propertyLabel, slider ).build(), jfxProp );
	}

	private static Node createLabel( PropertyLayoutComponent<?> propLayoutComp, Label propertyLabel )
	{
		Label label = new Label();
		javafx.beans.property.Property<?> jfxProp = Properties.convert( propLayoutComp.getProperty() );
		label.textProperty().bind( Bindings.convert( jfxProp ) );
		return nodeWithProperty( VBoxBuilder.create().children( propertyLabel, label ).build(), jfxProp );
	}

	private static Node nodeWithProperty( Node node, javafx.beans.property.Property<?> jfxProp )
	{
		// it is necessary to keep a strong reference to the property so it won't be garbage collected
		// taking the handlers bound to it with it
		node.getProperties().put( "_KEEP_STRONG_REF_TO_JFX_PROPERTY_", jfxProp );
		return node;
	}

}

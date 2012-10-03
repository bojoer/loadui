package com.eviware.loadui.ui.fx.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.layout.VBox;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.eviware.loadui.api.property.Property;
import com.google.common.base.Objects;

public class SettingsDialog extends ConfirmationDialog
{
	public static final double VERTICAL_SPACING = 12;
	@Nonnull
	private final TabPane tabPane = new TabPane();
	@Nonnull
	private final List<SettingsTab> tabs;
	private final EventHandler<ActionEvent> onSaveHandler = new OnSaveHandler();

	public SettingsDialog( @Nonnull Node owner, @Nonnull String title, @Nonnull List<SettingsTab> tabs )
	{
		super( owner, title, "Save" );
		this.tabs = tabs;
		tabPane.getTabs().addAll( tabs );
		getItems().add( tabPane );
		setOnConfirm( onSaveHandler );
	}

	public class OnSaveHandler implements EventHandler<ActionEvent>
	{
		@Override
		public void handle( ActionEvent event )
		{
			boolean savingSucceeded = true;
			for( SettingsTab tab : tabs )
			{
				savingSucceeded = savingSucceeded && tab.save();
			}
			if( savingSucceeded )
				close();
		}
	}

	public static class SettingsTabBuilder
	{
		private final SettingsTab tab;

		public static SettingsTabBuilder create( @Nonnull String label )
		{
			return new SettingsTabBuilder( label );
		}

		private SettingsTabBuilder( String label )
		{
			tab = new SettingsTab( label );
		}

		@SuppressWarnings( "unchecked" )
		public SettingsTabBuilder field( @Nonnull String label, @Nonnull Property<?> property )
		{
			if( property.getType().equals( String.class ) )
				tab.addStringField( label, ( Property<String> )property );
			else if( property.getType().equals( Long.class ) )
				tab.addLongField( label, ( Property<Long> )property );
			else if( property.getType().equals( Boolean.class ) )
				tab.addBooleanField( label, ( Property<Boolean> )property );
			return this;
		}

		@Nonnull
		public SettingsTab build()
		{
			return tab;
		}
	}

	public static class SettingsTab extends Tab
	{
		private final Map<TextField, Property<String>> textFieldToStringProperty = new HashMap<>();
		private final Map<TextField, Property<Long>> textFieldToLongProperty = new HashMap<>();
		private final Map<CheckBox, Property<Boolean>> checkBoxToProperty = new HashMap<>();
		private final VBox vBox = new VBox( VERTICAL_SPACING );

		private SettingsTab( String label )
		{
			super( label );
			setClosable( false );
			setContent( vBox );
		}

		private void addStringField( String label, Property<String> property )
		{
			TextField field = TextFieldBuilder.create().id( toCssId( label ) ).text( property.getValue() ).build();
			vBox.getChildren().addAll( new Label( label + ":" ), field );
			textFieldToStringProperty.put( field, property );
		}

		private void addLongField( String label, Property<Long> property )
		{
			TextField field = TextFieldBuilder.create().id( toCssId( label ) )
					.text( Objects.firstNonNull( property.getValue(), "" ).toString() ).build();
			vBox.getChildren().addAll( new Label( label + ":" ), field );
			textFieldToLongProperty.put( field, property );
		}

		private void addBooleanField( String label, Property<Boolean> property )
		{
			CheckBox field = CheckBoxBuilder.create().id( toCssId( label ) ).text( label ).selected( property.getValue() )
					.build();
			vBox.getChildren().add( field );
			checkBoxToProperty.put( field, property );
		}

		private static String toCssId( String label )
		{
			return label.toLowerCase().replace( " ", "-" );
		}

		private boolean save()
		{
			boolean wasValid = true;
			for( Entry<TextField, Property<String>> entry : textFieldToStringProperty.entrySet() )
			{
				TextField textField = entry.getKey();
				entry.getValue().setValue( textField.getText() );
			}
			for( Entry<TextField, Property<Long>> entry : textFieldToLongProperty.entrySet() )
			{
				TextField textField = entry.getKey();
				Long newValue = getLong( textField );
				if( newValue == null )
					wasValid = false;
				else
					entry.getValue().setValue( newValue );
			}
			for( Entry<CheckBox, Property<Boolean>> entry : checkBoxToProperty.entrySet() )
			{
				CheckBox checkBox = entry.getKey();
				entry.getValue().setValue( checkBox.isSelected() );
			}
			return wasValid;
		}

	}
}
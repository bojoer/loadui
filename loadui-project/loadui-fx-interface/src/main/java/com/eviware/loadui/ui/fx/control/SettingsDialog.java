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

import javax.annotation.Nonnull;

import com.eviware.loadui.api.property.Property;

public class SettingsDialog extends ConfirmationDialog
{
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
			for( SettingsTab tab : tabs )
			{
				tab.save();
			}

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

		public SettingsTabBuilder stringField( @Nonnull String label, @Nonnull Property<String> property )
		{
			tab.addStringField( label, property );
			return this;
		}

		public SettingsTabBuilder booleanField( @Nonnull String label, @Nonnull Property<Boolean> property )
		{
			tab.addBooleanField( label, property );
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
		@Nonnull
		private final Map<Node, Property<?>> fieldToProperty = new HashMap<>();
		private final VBox vBox = new VBox( 12 );

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
			fieldToProperty.put( field, property );
		}

		private void addBooleanField( String label, Property<Boolean> property )
		{
			CheckBox field = CheckBoxBuilder.create().id( toCssId( label ) ).text( label ).selected( property.getValue() )
					.build();
			vBox.getChildren().add( field );
			fieldToProperty.put( field, property );
		}

		private static String toCssId( String label )
		{
			return label.toLowerCase().replace( " ", "-" );
		}

		private void save()
		{
			for( Entry<Node, Property<?>> entry : fieldToProperty.entrySet() )
			{
				Node field = entry.getKey();
				if( field instanceof TextField )
				{
					TextField textField = ( TextField )field;
					entry.getValue().setValue( textField.getText() );
				}
				if( field instanceof CheckBox )
				{
					CheckBox checkBox = ( CheckBox )field;
					entry.getValue().setValue( checkBox.isSelected() );
				}
			}
		}
	}
}
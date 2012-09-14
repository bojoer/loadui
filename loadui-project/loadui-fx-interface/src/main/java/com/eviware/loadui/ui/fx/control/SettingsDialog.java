package com.eviware.loadui.ui.fx.control;

import java.io.File;
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
import javafx.stage.FileChooser;

import com.eviware.loadui.api.property.Property;

// FIXME Composition instead?
public class SettingsDialog extends ConfirmationDialog
{
	private final TabPane tabPane = new TabPane();
	private final List<SettingsTab> tabs;
	private final EventHandler<ActionEvent> onSaveHandler = new OnSaveHandler();

	public SettingsDialog( Node owner, String title, List<SettingsTab> tabs )
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

		public static SettingsTabBuilder create( String label )
		{
			return new SettingsTabBuilder( label );
		}

		private SettingsTabBuilder( String label )
		{
			tab = new SettingsTab( label );
		}

		public SettingsTabBuilder stringField( String label, Property<String> property )
		{
			tab.addStringField( label, property );
			return this;
		}

		public SettingsTabBuilder booleanField( String label, Property<Boolean> property )
		{
			tab.addBooleanField( label, property );
			return this;
		}

		public SettingsTab build()
		{
			return tab;
		}
	}

	public static class SettingsTab extends Tab
	{
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
			String s = label.toLowerCase().replace( " ", "-" );
			System.out.println( s );
			return s;
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
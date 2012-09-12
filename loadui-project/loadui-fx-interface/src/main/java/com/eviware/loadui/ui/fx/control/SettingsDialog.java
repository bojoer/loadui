package com.eviware.loadui.ui.fx.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

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

		public SettingsTabBuilder textField( String label, Property<String> property )
		{
			tab.addTextField( label, property );
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
		private final GridPane grid = new GridPane();

		private SettingsTab( String label )
		{
			super( label );
			setClosable( false );
			setContent( grid );
			grid.setHgap( 10 );
			grid.setVgap( 12 );
		}

		private void addTextField( String label, Property<String> property )
		{
			grid.add( new Label( label + ":" ), 0, 0 );
			TextField field = new TextField( property.getValue() );
			grid.add( field, 1, 0 );
			fieldToProperty.put( field, property );
		}

		private void save()
		{
			for( Entry<Node, Property<?>> entry : fieldToProperty.entrySet() )
			{
				entry.getValue().setValue( ( ( TextField )entry.getKey() ).getText() );
			}
		}
	}
}
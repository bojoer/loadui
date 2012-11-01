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
import javafx.scene.layout.VBox;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.ui.fx.control.fields.Field;
import com.eviware.loadui.ui.fx.control.fields.Validatable;
import com.eviware.loadui.ui.fx.control.fields.ValidatableCheckBox;
import com.eviware.loadui.ui.fx.control.fields.ValidatableLongField;
import com.eviware.loadui.ui.fx.control.fields.ValidatableStringField;
import com.eviware.loadui.ui.fx.control.fields.ValidatableTextField;
import com.eviware.loadui.ui.fx.util.UIUtils;
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
			boolean wasValid = true;
			for( SettingsTab tab : tabs )
			{
				wasValid = wasValid && tab.validate();
			}
			if( wasValid )
			{
				for( SettingsTab tab : tabs )
				{
					tab.save();
				}
				close();
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

		public <T> SettingsTabBuilder field( @Nonnull String label, @Nonnull Property<T> property )
		{
			tab.addField( label, property );
			return this;
		}

		public SettingsTabBuilder id( String id )
		{
			tab.setId( id );
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
		private final Map<Field<?>, Property<?>> fieldToProperty = new HashMap<>();
		private final VBox vBox = new VBox( VERTICAL_SPACING );

		private SettingsTab( String label )
		{
			super( label );
			setClosable( false );
			setContent( vBox );
		}

		private void addField( String label, Property<?> property )
		{
			if( property.getType().equals( Boolean.class ) )
			{
				ValidatableCheckBox checkBox = new ValidatableCheckBox( label );
				checkBox.setSelected( ( Boolean )property.getValue() );
				checkBox.setId( UIUtils.toCssId( label ) );
				vBox.getChildren().add( checkBox );
				fieldToProperty.put( checkBox, property );
			}
			else
			{
				ValidatableTextField<?> textField;
				if( property.getType().equals( Long.class ) )
				{
					textField = ValidatableLongField.Builder.create()
							.text( Objects.firstNonNull( property.getValue(), "" ).toString() ).build();
				}
				else
				{
					textField = new ValidatableStringField();
					textField.setText( property.getValue().toString() );
				}
				textField.setId( UIUtils.toCssId( label ) );
				vBox.getChildren().addAll( new Label( label + ":" ), textField );
				fieldToProperty.put( textField, property );
			}
		}

		private boolean validate()
		{
			boolean wasValid = true;
			for( Entry<Field<?>, Property<?>> entry : fieldToProperty.entrySet() )
			{
				Validatable field = entry.getKey();
				wasValid = wasValid && field.isValid();
			}
			return wasValid;
		}

		private void save()
		{
			for( Entry<Field<?>, Property<?>> entry : fieldToProperty.entrySet() )
			{
				Field<?> field = entry.getKey();
				entry.getValue().setValue( field.getValue() );
			}
		}

	}
}
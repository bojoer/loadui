package com.eviware.loadui.ui.fx.control;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.layout.ActionLayoutComponent;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.fields.Field;
import com.eviware.loadui.ui.fx.control.fields.Validatable;
import com.eviware.loadui.ui.fx.control.fields.ValidatableCheckBox;
import com.eviware.loadui.ui.fx.control.fields.ValidatableComboBoxField;
import com.eviware.loadui.ui.fx.control.fields.ValidatableLongField;
import com.eviware.loadui.ui.fx.control.fields.ValidatableStringField;
import com.eviware.loadui.ui.fx.control.fields.ValidatableTextField;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;

public class SettingsTab extends Tab
{
	private final BiMap<Field<?>, Property<?>> fieldToLoaduiProperty = HashBiMap.create();
	private final BiMap<Field<?>, FieldSaveHandler<?>> fieldToFieldSaveHandler = HashBiMap.create();
	private final BiMap<Field<?>, javafx.beans.property.Property<?>> fieldToJavafxProperty = HashBiMap.create();
	private final VBox vBox = new VBox( SettingsDialog.VERTICAL_SPACING );

	SettingsTab( String label )
	{
		super( label );
		setClosable( false );
		setContent( vBox );
	}

	public Field<?> getFieldFor( Property<?> loaduiProperty )
	{
		return fieldToLoaduiProperty.inverse().get( loaduiProperty );
	}

	public Field<?> getFieldFor( javafx.beans.property.Property<?> fxProperty )
	{
		return fieldToJavafxProperty.inverse().get( fxProperty );
	}

	void addField( String label, Property<?> property )
	{
		if( property.getType().equals( Boolean.class ) )
		{
			ValidatableCheckBox checkBox = new ValidatableCheckBox( label );
			checkBox.setSelected( ( Boolean )property.getValue() );
			checkBox.setId( UIUtils.toCssId( label ) );
			vBox.getChildren().add( checkBox );
			fieldToLoaduiProperty.put( checkBox, property );
		}
		else if( property.getType().isEnum() )
		{
			Object[] enumValues = new Object[0];
			try
			{
				enumValues = ( Object[] )property.getType().getMethod( "values" ).invoke( null );
			}
			catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
					| SecurityException e )
			{
				e.printStackTrace();
			}
			ValidatableComboBoxField combo = new ValidatableComboBoxField();
			combo.setItems( FXCollections.observableArrayList( enumValues ) );
			combo.getSelectionModel().select( property.getValue() );
			combo.setId( UIUtils.toCssId( label ) );
			vBox.getChildren().add( combo );

			// TODO: We need a ValidatableComboBoxField for this.
			fieldToLoaduiProperty.put( combo, property );
		}
		else
		{
			ValidatableTextField<?> textField;

			if( property.getType().equals( String.class ) )
			{
				textField = new ValidatableStringField();
				textField.setText( Objects.firstNonNull( property.getValue(), "" ).toString() );
			}
			else
			{
				textField = ValidatableLongField.Builder.create()
						.text( Objects.firstNonNull( property.getValue(), "" ).toString() ).build();
			}
			textField.setId( UIUtils.toCssId( label ) );
			vBox.getChildren().addAll( new Label( label + ":" ), textField );
			fieldToLoaduiProperty.put( textField, property );
		}
	}

	void addField( String label, javafx.beans.property.Property<?> property )
	{
		if( property.getValue() instanceof String )
		{
			ValidatableTextField<?> textField = new ValidatableStringField();
			textField.setText( Objects.firstNonNull( property.getValue(), "" ).toString() );
			textField.setId( UIUtils.toCssId( label ) );
			vBox.getChildren().addAll( new Label( label + ":" ), textField );
			fieldToJavafxProperty.put( textField, property );
		}
		else
		{
			throw new UnsupportedOperationException( "This operation is not yet available for class "
					+ property.getValue().getClass().getName() );
		}
	}

	<T> void addField( String label, T initialValue, FieldSaveHandler<T> fieldSaveHandler )
	{
		if( initialValue instanceof String )
		{
			ValidatableTextField<?> textField = new ValidatableStringField();
			textField.setText( ( String )initialValue );
			textField.setId( UIUtils.toCssId( label ) );
			vBox.getChildren().addAll( new Label( label + ":" ), textField );
			fieldToFieldSaveHandler.put( textField, fieldSaveHandler );
		}
		else if( initialValue instanceof Boolean )
		{
			ValidatableCheckBox checkBox = new ValidatableCheckBox( label );
			checkBox.setSelected( ( Boolean )initialValue );
			checkBox.setId( UIUtils.toCssId( label ) );
			vBox.getChildren().add( checkBox );
			fieldToFieldSaveHandler.put( checkBox, fieldSaveHandler );
		}
		else
		{
			throw new UnsupportedOperationException( "This operation is not yet available for class "
					+ initialValue.getClass().getName() );
		}
	}

	@SuppressWarnings( "unchecked" )
	void addActionButton( final ActionLayoutComponent action )
	{

		if( action.getLabel().compareTo( "Test Connection" ) == 0 )
		{
			try
			{
				Object currentStatus = action.get( "status" );

				if( !( currentStatus instanceof Callable<?> ) )
				{
					throw new IllegalArgumentException( "The status closure is currently not represented by a callable" );
				}

				final Text statusLabel = TextBuilder.create().text( "Untested..." ).id( UIUtils.toCssId( "status" ) )
						.build();

				final Button button = ButtonBuilder.create().text( action.getLabel() ).disable( !action.isEnabled() )
						.id( UIUtils.toCssId( action.getLabel() ) ).build();

				final Runnable statusCallback = new Runnable()
				{
					public void run()
					{
						if( action.get( "status" ) instanceof Callable<?> )
						{
							try
							{
								Object obj = ( ( Callable<Object> )action.get( "status" ) ).call();
								if( obj instanceof Boolean )
								{
									Boolean connected = ( Boolean )obj;
									if( connected )
									{
										statusLabel.setText( "Connected to monitor!" );
									}
									else
									{
										statusLabel.setText( "Unable to connect to monitor!" );
									}
								}
								else if( obj instanceof String )
								{
									String response = ( String )obj;
									if( response.contains( "Exception" ) )
									{
										statusLabel.setText( "Unable to connect to monitor!" );
									}
									else
									{
										statusLabel.setText( response );
									}
								}
							}
							catch( Exception e )
							{
								e.printStackTrace();
							}
							finally
							{
								restoreBackedUpSettings();
							}
						}
					}
				};

				button.setOnAction( new EventHandler<ActionEvent>()
				{
					@Override
					public void handle( ActionEvent arg0 )
					{
						backupCurrentSettings();

						button.fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, action.getAction() ) );
						button.fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, statusCallback ) );
					}
				} );
				vBox.getChildren().addAll( button, statusLabel );
			}
			catch( Exception e1 )
			{
				e1.printStackTrace();
			}
		}
		else
		{
			throw new UnsupportedOperationException( "This operation is not yet available for label " + action.getLabel() );
		}
	}

	private void backupCurrentSettings()
	{
		ObservableList<Tab> tabList = getTabPane().getTabs();

		for( Tab tab : tabList )
		{
			if( tab instanceof SettingsTab )
			{
				SettingsTab stab = ( ( SettingsTab )tab );
				stab.pushValueByField();
				stab.save();
			}
		}
	}

	private void restoreBackedUpSettings()
	{
		ObservableList<Tab> tabList = getTabPane().getTabs();

		for( Tab tab : tabList )
		{
			if( tab instanceof SettingsTab )
			{
				SettingsTab stab = ( ( SettingsTab )tab );
				stab.popValueByField();
			}
		}
	}

	Map<Field<?>, Object> settingsStore = new HashMap<>();

	private void pushValueByField()
	{
		for( Entry<Field<?>, Property<?>> entry : fieldToLoaduiProperty.entrySet() )
		{
			Field<?> field = entry.getKey();
			Property<?> prop = entry.getValue();
			settingsStore.put( field, prop.getValue() );
		}
	}

	private void popValueByField()
	{
		for( Field<?> field : settingsStore.keySet() )
		{
			Object item = settingsStore.get( field );
			fieldToLoaduiProperty.get( field ).setValue( item );
		}
		settingsStore.clear();
	}

	boolean validate()
	{
		boolean wasValid = true;
		Iterable<Field<?>> allFields = Iterables.concat( fieldToLoaduiProperty.keySet(), fieldToJavafxProperty.keySet(),
				fieldToFieldSaveHandler.keySet() );
		for( Validatable field : allFields )
		{
			wasValid = wasValid && field.isValid();
		}
		return wasValid;
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	void save()
	{
		for( Entry<Field<?>, Property<?>> entry : fieldToLoaduiProperty.entrySet() )
		{
			Field<?> field = entry.getKey();
			entry.getValue().setValue( field.getFieldValue() );
		}
		for( Entry<Field<?>, javafx.beans.property.Property<?>> entry : fieldToJavafxProperty.entrySet() )
		{
			Field<?> field = entry.getKey();
			javafx.beans.property.Property property = entry.getValue();
			property.setValue( field.getFieldValue() );
		}
		for( Entry<Field<?>, FieldSaveHandler<?>> entry : fieldToFieldSaveHandler.entrySet() )
		{
			Field<?> field = entry.getKey();
			FieldSaveHandler saveHandler = entry.getValue();
			saveHandler.save( field.getFieldValue() );
		}
	}

	public static class Builder
	{
		private final SettingsTab tab;

		public static Builder create( @Nonnull String label )
		{
			return new Builder( label );
		}

		private Builder( String label )
		{
			tab = new SettingsTab( label );
		}

		public Builder button( ActionLayoutComponent action )
		{

			tab.addActionButton( action );
			return this;
		}

		public <T> Builder field( @Nonnull String label, @Nonnull Property<T> loaduiProperty )
		{
			tab.addField( label, loaduiProperty );
			return this;
		}

		public <T> Builder field( @Nonnull String label, @Nonnull javafx.beans.property.Property<T> fxProperty )
		{
			tab.addField( label, fxProperty );
			return this;
		}

		public <T> Builder field( String label, T initialValue, FieldSaveHandler<T> fieldSaveHandler )
		{
			tab.addField( label, initialValue, fieldSaveHandler );
			return this;
		}

		public Builder id( String id )
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
}
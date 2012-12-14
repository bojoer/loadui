package com.eviware.loadui.ui.fx.control;

import java.util.Map.Entry;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
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
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;

public class SettingsTab extends Tab
{
	private final BiMap<Field<?>, Property<?>> fieldToLoaduiProperty = HashBiMap.create();
	private final BiMap<Field<?>, javafx.beans.property.Property<?>> fieldToJavafxProperty = HashBiMap.create();
	private final BiMap<Field<?>, FieldSaveHandler<?>> fieldToFieldSaveHandler = HashBiMap.create();
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
				textField.setText( Objects.firstNonNull( property.getValue(), "" ).toString() );
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
			entry.getValue().setValue( field.getValue() );
		}
		for( Entry<Field<?>, javafx.beans.property.Property<?>> entry : fieldToJavafxProperty.entrySet() )
		{
			Field<?> field = entry.getKey();
			javafx.beans.property.Property property = entry.getValue();
			property.setValue( field.getValue() );
		}
		for( Entry<Field<?>, FieldSaveHandler<?>> entry : fieldToFieldSaveHandler.entrySet() )
		{
			Field<?> field = entry.getKey();
			FieldSaveHandler saveHandler = entry.getValue();
			saveHandler.save( field.getValue() );
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
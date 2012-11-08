package com.eviware.loadui.ui.fx.control;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.ui.fx.control.fields.Field;
import com.eviware.loadui.ui.fx.control.fields.Validatable;
import com.eviware.loadui.ui.fx.control.fields.ValidatableCheckBox;
import com.eviware.loadui.ui.fx.control.fields.ValidatableLongField;
import com.eviware.loadui.ui.fx.control.fields.ValidatableStringField;
import com.eviware.loadui.ui.fx.control.fields.ValidatableTextField;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.google.common.base.Objects;

public class SettingsTab extends Tab
{
	private final Map<Field<?>, Property<?>> fieldToProperty = new HashMap<>();
	private final VBox vBox = new VBox( SettingsDialog.VERTICAL_SPACING );

	SettingsTab( String label )
	{
		super( label );
		setClosable( false );
		setContent( vBox );
	}

	void addField( String label, Property<?> property )
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
				textField.setText( Objects.firstNonNull( property.getValue(), "" ).toString() );
			}
			textField.setId( UIUtils.toCssId( label ) );
			vBox.getChildren().addAll( new Label( label + ":" ), textField );
			fieldToProperty.put( textField, property );
		}
	}

	boolean validate()
	{
		boolean wasValid = true;
		for( Entry<Field<?>, Property<?>> entry : fieldToProperty.entrySet() )
		{
			Validatable field = entry.getKey();
			wasValid = wasValid && field.isValid();
		}
		return wasValid;
	}

	void save()
	{
		for( Entry<Field<?>, Property<?>> entry : fieldToProperty.entrySet() )
		{
			Field<?> field = entry.getKey();
			entry.getValue().setValue( field.getValue() );
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

		public <T> Builder field( @Nonnull String label, @Nonnull Property<T> property )
		{
			tab.addField( label, property );
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
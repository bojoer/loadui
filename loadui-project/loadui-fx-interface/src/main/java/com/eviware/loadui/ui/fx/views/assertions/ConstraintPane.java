package com.eviware.loadui.ui.fx.views.assertions;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;

import com.eviware.loadui.ui.fx.control.SettingsDialog;
import com.eviware.loadui.ui.fx.control.fields.Validatable;
import com.eviware.loadui.ui.fx.control.fields.ValidatableLongField;

public class ConstraintPane extends VBox implements Validatable
{
	private final ValidatableLongField minField;
	private final ValidatableLongField maxField;
	private final BooleanProperty isValidProperty = new SimpleBooleanProperty( false );

	public ConstraintPane()
	{
		minField = ValidatableLongField.Builder.create().build();
		VBox minBox = VBoxBuilder.create().spacing( SettingsDialog.VERTICAL_SPACING )
				.children( new Label( "Min" ), minField ).build();

		maxField = ValidatableLongField.Builder.create().build();
		VBox maxBox = VBoxBuilder.create().spacing( SettingsDialog.VERTICAL_SPACING )
				.children( new Label( "Max" ), maxField ).build();

		Label constrainLabel = LabelBuilder.create().text( "Constraint" ).build();
		constrainLabel.getStyleClass().add( "strong" );

		HBox constraintFields = HBoxBuilder.create().spacing( 30.0 ).children( minBox, maxBox ).build();
		getChildren().setAll( constrainLabel, constraintFields );

		isValidProperty.bind( Bindings.and( minField.isValidProperty(), maxField.isValidProperty() ) );
	}

	@Override
	public ReadOnlyBooleanProperty isValidProperty()
	{
		return isValidProperty;
	}

	@Override
	public boolean isValid()
	{
		return isValidProperty.get();
	}
}

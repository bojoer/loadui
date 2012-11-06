package com.eviware.loadui.ui.fx.views.assertions;

import static com.eviware.loadui.ui.fx.control.SettingsDialog.VERTICAL_SPACING;
import static javafx.beans.binding.Bindings.and;
import static javafx.beans.binding.Bindings.when;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TitledPaneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.util.Pair;

import com.eviware.loadui.api.assertion.Constraint;
import com.eviware.loadui.ui.fx.control.fields.Validatable;
import com.eviware.loadui.ui.fx.control.fields.ValidatableLongField;
import com.eviware.loadui.util.assertion.RangeConstraint;

public class ConstraintPane extends VBox implements Validatable
{
	private final ValidatableLongField minField;
	private final ValidatableLongField maxField;
	private final BooleanProperty isValidProperty = new SimpleBooleanProperty( false );
	private final ValidatableLongField timesAllowed;
	private final ValidatableLongField timeWindow;

	public ConstraintPane()
	{
		minField = ValidatableLongField.Builder.create().build();
		VBox minBox = VBoxBuilder.create().spacing( VERTICAL_SPACING ).children( new Label( "Min" ), minField ).build();

		maxField = ValidatableLongField.Builder.create().build();
		VBox maxBox = VBoxBuilder.create().spacing( VERTICAL_SPACING ).children( new Label( "Max" ), maxField ).build();

		Label constrainLabel = LabelBuilder.create().text( "Constraint" ).build();
		constrainLabel.getStyleClass().add( "strong" );

		HBox constraintFields = HBoxBuilder.create().spacing( 26.0 ).children( minBox, maxBox ).build();

		timesAllowed = ValidatableLongField.Builder.create().text( "0" ).build();
		timeWindow = ValidatableLongField.Builder.create().build();
		timeWindow.disableProperty().bind(
				when( timesAllowed.textProperty().isEqualTo( "0" ) ).then( true ).otherwise( false ) );
		HBox tolerancePane = HBoxBuilder.create()
				.children( timesAllowed, new Label( "times, within" ), timeWindow, new Label( "seconds" ) ).build();
		TitledPane advancedPane = TitledPaneBuilder.create().text( "Advanced" ).expanded( false ).content( tolerancePane )
				.build();

		setSpacing( 16.0 );
		getChildren().setAll( constrainLabel, constraintFields, advancedPane );

		isValidProperty.bind( and( minField.isValidProperty(), maxField.isValidProperty() ) );
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

	public Constraint<Number> getConstraint()
	{
		return new RangeConstraint( minField.getValue(), maxField.getValue() );
	}

	public Pair<Integer, Integer> getTolerance()
	{
		return new Pair<Integer, Integer>( timesAllowed.getValue().intValue(), timeWindow.getValue().intValue() );
	}
}

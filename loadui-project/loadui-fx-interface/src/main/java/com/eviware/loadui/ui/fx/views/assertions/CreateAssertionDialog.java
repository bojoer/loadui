package com.eviware.loadui.ui.fx.views.assertions;

import static javafx.beans.binding.Bindings.and;
import static javafx.beans.binding.Bindings.not;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;

public class CreateAssertionDialog extends ConfirmationDialog
{
	protected static final Logger log = LoggerFactory.getLogger( CreateAssertionDialog.class );
	private final AssertableTree tree;

	public CreateAssertionDialog( Node owner, StatisticHolder holder )
	{
		super( owner, "Create assertion", "Create" );

		tree = AssertableTree.forHolder( holder );
		ConstraintPane constraintPane = new ConstraintPane();

		confirmDisableProperty().bind( not( and( tree.isValidProperty(), constraintPane.isValidProperty() ) ) );

		HBox hBox = HBoxBuilder.create().spacing( 30.0 ).children( tree, constraintPane ).build();
		getItems().add( hBox );
	}

	public AssertableWrapper<ListenableValue<Number>> getSelectedAssertable()
	{
		Labeled selected = tree.getSelectionModel().getSelectedItem().getValue();
		return ( AssertableWrapper<ListenableValue<Number>> )selected;
	}

}

package com.eviware.loadui.ui.fx.views.distribution;

import static com.eviware.loadui.ui.fx.util.NodeUtils.bindStyleClass;
import static javafx.beans.binding.Bindings.bindContent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.model.Assignment;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;

public class AssignmentView extends StackPane
{
	private final Assignment assignment;

	@FXML
	private MenuButton menuButton;

	public AssignmentView( Assignment assignment )
	{
		this.assignment = assignment;
		FXMLUtils.load( this );

		DragNode dragNode = DragNode.install( this, createLabel() );
		dragNode.setData( assignment );

		bindStyleClass( this, "dragging", dragNode.draggingProperty() );

		final Label baseLabel = createLabel();
		getChildren().add( 0, baseLabel );
	}

	private Label createLabel()
	{
		Label label = LabelBuilder.create().styleClass( "slim-icon" ).minWidth( 100 ).maxWidth( 100 ).build();
		label.textProperty().bind( Properties.forLabel( assignment.getScene() ) );
		bindContent( label.getStylesheets(), getStylesheets() );
		return label;
	}

	@FXML
	private void openScenario()
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, assignment.getScene() ) );
	}

	@FXML
	public void delete()
	{
		assignment.getScene().getProject().unassignScene( assignment.getScene(), assignment.getAgent() );

	}

}

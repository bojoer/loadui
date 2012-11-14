package com.eviware.loadui.ui.fx.views.distribution;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.model.Assignment;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;

public class AssignmentView extends StackPane
{
	@SuppressWarnings( "unused" )
	private final Assignment assignment;

	@FXML
	private MenuButton menuButton;
	@FXML
	private Label nameLabel;

	public AssignmentView( Assignment assignment )
	{
		this.assignment = assignment;
		FXMLUtils.load( this );

		SceneItem scenario = assignment.getScene();
		nameLabel.textProperty().bind( Properties.forLabel( scenario ) );
	}
}

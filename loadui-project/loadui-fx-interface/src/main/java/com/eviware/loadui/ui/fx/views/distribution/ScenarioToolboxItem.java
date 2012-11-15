package com.eviware.loadui.ui.fx.views.distribution;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;

public class ScenarioToolboxItem extends StackPane
{
	private final SceneItem scenario;

	public ScenarioToolboxItem( SceneItem scenario )
	{
		this.scenario = scenario;
		FXMLUtils.load( this );

		DragNode.install( this, createLabel() ).setData( scenario );

		final Label baseLabel = createLabel();
		getChildren().setAll( baseLabel );
	}

	private Label createLabel()
	{
		Label label = LabelBuilder.create().styleClass( "slim-icon" ).minWidth( 100 ).maxWidth( 100 ).build();
		label.textProperty().bind( Properties.forLabel( scenario ) );
		Bindings.bindContent( label.getStylesheets(), getStylesheets() );
		return label;
	}
}

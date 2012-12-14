package com.eviware.loadui.ui.fx.views.distribution;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.control.DragNode;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.UIUtils;

public class ScenarioToolboxItem extends Label
{
	private final SceneItem scenario;

	public ScenarioToolboxItem( SceneItem scenario )
	{
		getStyleClass().add( "icon" );
		this.scenario = scenario;
		FXMLUtils.load( this );

		DragNode.install( this, createIcon() ).setData( scenario );

		setGraphic( createIcon() );
		setText( scenario.getLabel() );

	}

	private Node createIcon()
	{

		ImageView imageView = new ImageView( UIUtils.getImageFor( SceneItem.class ) );
		imageView.getStyleClass().add( "image" );
		return imageView;
	}

	@Override
	public String toString()
	{
		return scenario.getLabel();
	}
}

package com.eviware.loadui.ui.fx.views.distribution;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.Scene;

import com.eviware.loadui.ui.fx.api.Inspector;
import com.eviware.loadui.ui.fx.api.perspective.PerspectiveEvent;
import com.eviware.loadui.ui.fx.util.InspectorHelpers;

public class DistributionInspector implements Inspector
{
	private static final String FILTER = PerspectiveEvent.getPath( PerspectiveEvent.PERSPECTIVE_PROJECT ) + ".*";

	// private final Property<WorkspaceItem> workspace = new SimpleObjectProperty<>( this, "workspace" );
	private final DistributionView panel = new DistributionView();

	@Override
	public void initialize( ReadOnlyProperty<Scene> sceneProperty )
	{
		panel.projectProperty().bind( InspectorHelpers.projectProperty( sceneProperty ) );
	}

	@Override
	public String getName()
	{
		return "Distribution";
	}

	@Override
	public String getPerspectiveRegex()
	{
		// TODO Auto-generated method stub
		return FILTER;
	}

	@Override
	public Node getPanel()
	{
		return panel;
	}

	@Override
	public void onShow()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onHide()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getHelpUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}

}

package com.eviware.loadui.ui.fx.views.syslog;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.RegionBuilder;

import com.eviware.loadui.ui.fx.api.Inspector;

public class SystemLogInspector implements Inspector
{
	@Override
	public void initialize( ReadOnlyProperty<Scene> sceneProperty )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getName()
	{
		return "System Log";
	}

	@Override
	public String getPerspectiveRegex()
	{
		return null;
	}

	@Override
	public Node getPanel()
	{
		return RegionBuilder.create().style( "-fx-background-color: yellow;" ).build();
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

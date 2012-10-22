package com.eviware.loadui.ui.fx.views.eventlog;

import javafx.scene.Node;

import com.eviware.loadui.ui.fx.api.Inspector;
import com.eviware.loadui.ui.fx.api.perspective.PerspectiveEvent;

public class EventLogInspector implements Inspector
{
	private static final String FILTER = PerspectiveEvent.getPath( PerspectiveEvent.PERSPECTIVE_PROJECT ) + ".*";

	private final EventLogView panel = new EventLogView();

	@Override
	public String getName()
	{
		return "Event Log";
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
		return null;
	}

	@Override
	public String getPerspectiveRegex()
	{
		return FILTER;
	}
}

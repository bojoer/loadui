package com.eviware.loadui.ui.fx.views.eventlog;

import com.eviware.loadui.api.ui.inspector.Inspector;

public class EventLogInspector implements Inspector
{
	private final EventLogView panel = new EventLogView();

	@Override
	public String getName()
	{
		return "Event Log";
	}

	@Override
	public Object getPanel()
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

}

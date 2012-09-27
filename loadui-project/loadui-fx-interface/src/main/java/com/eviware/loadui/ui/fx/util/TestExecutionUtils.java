package com.eviware.loadui.ui.fx.util;

import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.util.BeanInjector;

public class TestExecutionUtils
{
	private static TestRunner testRunner = BeanInjector.getBean( TestRunner.class );

	public static TestExecution startCanvas( CanvasItem canvas )
	{
		TestExecution currentExecution = null;
		return currentExecution;
	}
}

package com.eviware.loadui.ui.fx.util;

import com.eviware.loadui.ui.fx.api.NonSingletonFactory;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionChart;
import com.eviware.loadui.ui.fx.views.analysis.linechart.ScrollableLineChart;

public class DefaultNonSingletonFactory implements NonSingletonFactory
{
	
	private static final NonSingletonFactory instance = new DefaultNonSingletonFactory();
	
	public static final NonSingletonFactory get() {
		return instance;
	}
	
	private DefaultNonSingletonFactory() {
		// singleton
	}
	
	public ExecutionChart createExecutionChart() {
		return new ScrollableLineChart();
	}

}

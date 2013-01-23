package com.eviware.loadui.ui.fx.api;

import com.eviware.loadui.ui.fx.api.analysis.ExecutionChart;

/**
 * Factory which can be used to provide non-singleton implementations of API interfaces.
 * The Factory itself may be a OSGi service, but the instances it provides can come only from the
 * bundle where the factory is declared.
 * @author renato
 *
 */
public interface NonSingletonFactory
{
	/**
	 * Creates and returns an ExecutionChart
	 * @return ExecutionChart
	 */
	ExecutionChart createExecutionChart();
}

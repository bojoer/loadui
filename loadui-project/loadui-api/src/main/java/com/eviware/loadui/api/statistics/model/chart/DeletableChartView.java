package com.eviware.loadui.api.statistics.model.chart;

/**
 * A ChartView which can be deleted (eg. representing a Chart in a ChartGroup).
 * 
 * @author dain.nilsson
 */
public interface DeletableChartView extends ChartView
{
	/**
	 * Deletes the ChartView.
	 */
	public void delete();
}

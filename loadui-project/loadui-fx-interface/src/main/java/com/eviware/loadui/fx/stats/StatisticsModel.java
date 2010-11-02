package com.eviware.loadui.fx.stats;

import java.util.ArrayList;
import java.util.HashMap;

import com.eviware.loadui.api.counter.CounterHolder;
import com.jidesoft.chart.style.ChartStyle;

public class StatisticsModel
{

	private CounterHolder item;
	private HashMap<String, Statistics> stats = new HashMap<String, Statistics>();

	public StatisticsModel( CounterHolder project )
	{
		this.item = project;
	}

	public ArrayList<Statistics> getStatistics()
	{
		return new ArrayList<Statistics>(stats.values());
	}

	/**
	 * 
	 * @author robert
	 *
	 * Statistics holder
	 * Keeps counter name, chart style.
	 */
	public class Statistics {

		private String counterName;
		private String name;
		private ChartStyle style;

		public Statistics( String name, String counterName, ChartStyle chartStyle )
		{
			this.name = name;
			this.counterName = counterName;
			this.style = chartStyle;
		}

		public String getName()
		{
			return name;
		}

		public ChartStyle getChartStyle()
		{
			return style;
		}

		public long getValue( long currentTimeMillis )
		{
			return item.getCounter( counterName ).get();
		}
		
	}

	public void addStatistics( String name, String counterName, ChartStyle chartStyle )
	{
		stats.put( name, new Statistics(name, counterName, chartStyle));
	}
}

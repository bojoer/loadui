package com.eviware.loadui.fx.stats;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.jidesoft.chart.style.ChartStyle;

public class StatisticsModel
{
	static Random random = new Random();
	private ProjectItem item;
	private HashMap<String, StatisticsInner> stats = new HashMap<String, StatisticsInner>();

	public StatisticsModel( ProjectItem project )
	{
		this.item = project;
	}

	public ArrayList<StatisticsInner> getStatistics()
	{
		return new ArrayList<StatisticsInner>( stats.values() );
	}

	/**
	 * 
	 * @author robert
	 *
	 * Statistics holder
	 * Keeps counter name, chart style.
	 */
	public class StatisticsInner
	{

		private Statistic statistic;
		private String name;

		public StatisticsInner( String name, Statistic stat )
		{
			this.name = name;
			this.statistic = stat;
		}

		public String getName()
		{
			return name;
		}

		public Number getValue() 
		{
			return ( Number )statistic.getValue();
		}

	}

	public void addChartsForStats( String name, ChartStyle chartStyle )
	{
		
		if( item == null )
			return;
		for( ComponentItem comp : item.getComponents() ) {
//			System.out.println("stat names " + comp.getStatisticVariableNames());
//			for( String sname: comp.getStatisticVariableNames())
//			{
				StatisticVariable sv = comp.getStatisticVariable( name );
				//skip components without stats
				if( sv == null )
					continue;
//				for( String stat : sv.getStatisticNames() ) {
					stats.put( name+comp.getLabel()+"AVG", new StatisticsInner( name+comp.getLabel()+"AVG", sv.getStatistic( "AVERAGE", "local" ) ) );
					stats.put( name+comp.getLabel()+"STD_DEV", new StatisticsInner( name+comp.getLabel()+"STD_DEV", sv.getStatistic( "STD_DEV", "local" ) ) );
					stats.put( name+comp.getLabel()+"PERCENTILE", new StatisticsInner( name+comp.getLabel()+"PERCENTILE", sv.getStatistic( "PERCENTILE", "local" ) ) );
//				}
					
//			}
		}
	}
}

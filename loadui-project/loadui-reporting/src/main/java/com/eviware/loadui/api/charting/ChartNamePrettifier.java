/*
 * Copyright 2011 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.api.charting;

import com.eviware.loadui.api.statistics.Statistic;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.util.StringUtils;

public class ChartNamePrettifier
{
	public static String compactNameFor( Statistic<?> statistic )
	{
		return compactDataName( statistic.getName(), "" );
	}

	public static String nameFor( Statistic<?> statistic )
	{
		return statistic == null ? null : nameForStatistic( statistic.getName() );
	}

	public static String nameForSource( String source )
	{
		return StatisticVariable.MAIN_SOURCE.equals( source ) ? "Total" : source;
	}

	public static boolean compactNameIsAlone( String name )
	{
		return name.equals( "Running" ) || name.equals( "Queued" ) || name.equals( "Completed" )
				|| name.equals( "Discarded" ) || name.equals( "Failures" ) || name.equals( "Sent" )
				|| name.equals( "Assertion Failures" ) || name.equals( "Request Failures" ) || name.equals( "Requests" )
				|| name.equals( "Throughput" );
	}

	public static String nameForStatistic( String statisticName )
	{
		statisticName = statisticName.replaceAll( "_", " " );
		statisticName = StringUtils.capitalizeEachWord( statisticName );
		if( statisticName.startsWith( "Percentile" ) )
		{
			statisticName = statisticName.replaceFirst( "Percent", "%-" );
		}

		statisticName = statisticName.replaceAll( "^Tps$", "TPS" ).replaceAll( "^Bps$", "BPS" );

		return statisticName;
	}

	public static String compactDataName( String data, String metric )
	{
		if( metric.equalsIgnoreCase( "PER_SECOND" ) )
			return compactDataNameHelper( data ) + "/s";

		if( data.equalsIgnoreCase( "THROUGHPUT" ) )
			return metric;

		return compactDataNameHelper( data );
	}

	public static String compactDataNameHelper( String data )
	{
		if( data.equalsIgnoreCase( "Response Size" ) )
			return "RspSz";
		if( data.equalsIgnoreCase( "Assertion Failures" ) )
			return "AssertFails";
		if( data.equalsIgnoreCase( "Request Failures" ) )
			return "ReqstFails";
		if( data.equalsIgnoreCase( "Time Taken" ) )
			return "TimeT";
		return data;
	}

	public static String compactMetricName( String metric )
	{
		if( metric.equalsIgnoreCase( "AVERAGE" ) )
			return "Avg";
		if( metric.equalsIgnoreCase( "MIN" ) )
			return "Min";
		if( metric.equalsIgnoreCase( "MAX" ) )
			return "Max";
		if( metric.equalsIgnoreCase( "PER_SECOND" ) )
			return "/s";
		if( metric.equalsIgnoreCase( "TOTAL" ) )
			return "";
		if( metric.equalsIgnoreCase( "VALUE" ) )
			return "";
		if( metric.equalsIgnoreCase( "PERCENTILE_25TH" ) )
			return "25%";
		if( metric.equalsIgnoreCase( "PERCENTILE_75TH" ) )
			return "75%";
		if( metric.equalsIgnoreCase( "PERCENTILE_90TH" ) )
			return "90%";
		if( metric.equalsIgnoreCase( "STD_DEV" ) )
			return "SDv";
		if( metric.equalsIgnoreCase( "MEDIAN" ) )
			return "Mdn";
		if( metric.equalsIgnoreCase( "BPS" ) )
			return "";
		if( metric.equalsIgnoreCase( "TPS" ) )
			return "";
		return metric;
	}

	public static String compactDataAndMetricName( String data, String metric )
	{
		if( compactNameIsAlone( data ) )
		{
			return compactDataName( data, metric );
		}
		return compactDataName( data, metric ) + " " + ChartNamePrettifier.compactMetricName( metric );
	}
}

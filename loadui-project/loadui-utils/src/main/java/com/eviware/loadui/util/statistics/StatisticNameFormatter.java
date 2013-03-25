package com.eviware.loadui.util.statistics;

import com.eviware.loadui.util.StringUtils;

public class StatisticNameFormatter
{
	public static String format( String statistic )
	{
		switch( statistic )
		{
		case "PER_SECOND" :
		case "TOTAL" :
		case "VALUE" :
		case "MAX" :
		case "MIN" :
		case "AVERAGE" :
		case "MEDIAN" :
			return StringUtils.capitalizeEachWord( statistic.replace( "_", " " ) );
		case "STD_DEV" :
			return "Standard Deviation";
		case "PERCENTILE_90TH" :
			return "90th percentile";
		case "PERCENTILE_75TH" :
			return "75th percentile";
		case "PERCENTILE_25TH" :
			return "25th percentile";
		}
		return statistic;
	}
}

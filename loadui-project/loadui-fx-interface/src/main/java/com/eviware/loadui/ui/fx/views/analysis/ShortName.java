/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.views.analysis;

import javax.annotation.Nonnull;

public class ShortName
{
	/**
	 * Generates a short name for a statistic.
	 * 
	 * @param variable
	 *           is the name of the Statistic's StatisticVariable.
	 * @param metric
	 *           is the name of the Statistic's metric.
	 */
	public static String forStatistic( @Nonnull String variable, @Nonnull String metric )
	{
		StringFragment name = new StringFragment();

		if( variable.equals( "Throughput" ) )
			return metric;

		switch( metric )
		{
		case "PER_SECOND" :
			return variable + "/s";
		case "TOTAL" :
		case "VALUE" :
			return variable;
		case "MAX" :
			name.setPrefix( "Max" );
			break;
		case "MIN" :
			name.setPrefix( "Min" );
			break;
		case "AVERAGE" :
			name.setPrefix( "Avg" );
			break;
		case "STD_DEV" :
			name.setPrefix( "\u03C3" );
			break;
		case "PERCENTILE_90TH" :
			name.setPrefix( "90%" );
			break;
		case "PERCENTILE_75TH" :
			name.setPrefix( "75%" );
			break;
		case "MEDIAN" :
			name.setPrefix( "50%" );
			break;
		case "PERCENTILE_25TH" :
			name.setPrefix( "25%" );
			break;
		}

		switch( variable )
		{
		case "Time Taken" :
			return name.build( "Resp Tm" );
		case "Response Size" :
			return name.build( "Resp Sz" );
		}

		return metric;
	}

	private static class StringFragment
	{
		private String prefix = "";
		private String postfix = "";

		void setPrefix( String prefix )
		{
			this.prefix = prefix;
		}

		String build( String mainPart )
		{
			return prefix + " " + mainPart + " " + postfix;
		}
	}
}

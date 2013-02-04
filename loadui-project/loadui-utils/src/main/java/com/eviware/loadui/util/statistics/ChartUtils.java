package com.eviware.loadui.util.statistics;

import java.util.List;

public class ChartUtils
{
	public static String lineToColor( Object line, List<?> listOfLines )
	{
		int seriesOrder = listOfLines.indexOf( line );

		return lineToColor( seriesOrder );
	}

	public static String lineToColor( int number )
	{
		switch( number % 8 )
		{
		case 0 :
			return "#f9d900";
		case 1 :
			return "#a9e200";
		case 2 :
			return "#22bad9";
		case 3 :
			return "#0181e2";
		case 4 :
			return "#2f357f";
		case 5 :
			return "#860061";
		case 6 :
			return "#c62b00";
		case 7 :
			return "#ff5700";
		}
		throw new RuntimeException( "This is mathematically impossible!" );
	}
}

package com.eviware.loadui.util.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChartUtils
{
	private static final String[] colors = { "#f9d900", "#a9e200", "#22bad9", "#0181e2", "#2f357f", "#860061",
			"#c62b00", "#ff5700" };

	public final static Logger log = LoggerFactory.getLogger( ChartUtils.class );

	public static String getNewRandomColor( Collection<String> currentColorList )
	{
		String color;
		ArrayList<String> newColors = new ArrayList<>();

		newColors.addAll( Arrays.asList( colors ) );
		newColors.removeAll( currentColorList );

		Random rand = new Random();
		if( newColors.size() > 0 )
		{
			color = newColors.get( rand.nextInt( newColors.size() ) );
		}
		else
		{
			log.debug( "no new colors in list, randomising a new one ( will be duplicate of already used color)" );
			color = colors[( rand.nextInt( colors.length ) )];
		}

		return color;
	}

	public static String lineToColor( Object line, List<?> listOfLines )
	{
		int seriesOrder = listOfLines.indexOf( line );

		return lineToColor( seriesOrder );
	}

	public static String lineToColor( int number )
	{
		return colors[number % colors.length];

	}
}

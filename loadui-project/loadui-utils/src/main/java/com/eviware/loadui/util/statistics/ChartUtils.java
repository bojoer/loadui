package com.eviware.loadui.util.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public class ChartUtils
{
	private static final List<String> colors = ImmutableList.of( "#ff7a66", "#ff9533", "#66d466", "#00b2d2", "#ae7dd3",
			"#df51a5", "#c69c6d", "#c3e166", "#8383db", "#fffb00", "#ffffff", "#66afdb" );

	public final static Logger log = LoggerFactory.getLogger( ChartUtils.class );

	public static String getNewRandomColor( Collection<String> currentColorList )
	{
		String color;
		ArrayList<String> newColors = new ArrayList<>();

		newColors.addAll( colors );
		newColors.removeAll( currentColorList );

		Random rand = new Random();
		if( !newColors.isEmpty() )
		{
			color = newColors.get( rand.nextInt( newColors.size() ) );
		}
		else
		{
			log.debug( "no new colors in list, randomising a new one ( will be duplicate of already used color)" );
			color = colors.get( rand.nextInt( colors.size() ) );
		}

		return color;
	}
}

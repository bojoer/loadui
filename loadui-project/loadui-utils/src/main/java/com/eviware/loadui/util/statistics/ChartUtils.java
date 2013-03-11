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
	private static final List<String> colors = ImmutableList.of( "#f9d900", "#a9e200", "#22bad9", "#0181e2", "#2f357f",
			"#860061", "#c62b00", "#ff5700" );

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

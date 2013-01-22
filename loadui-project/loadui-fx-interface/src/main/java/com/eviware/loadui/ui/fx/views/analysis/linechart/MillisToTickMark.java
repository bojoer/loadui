package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.beans.property.SimpleObjectProperty;
import javafx.util.StringConverter;

import com.eviware.loadui.api.charting.line.ZoomLevel;

final class MillisToTickMark extends StringConverter<Number>
{
	private final SimpleObjectProperty<ZoomLevel> zoomLevelProperty;

	MillisToTickMark( SimpleObjectProperty<ZoomLevel> zoomLevelProperty )
	{
		this.zoomLevelProperty = zoomLevelProperty;
	}

	@Override
	public String toString( Number n )
	{
		long value = n.longValue();
		if( value == 0 )
			return "0";
		ZoomLevel parentZoomLevel = zoomLevelProperty.get().zoomOut();
		long parentIntervalMillis = parentZoomLevel.getInterval() * 1000;
		//		System.out.println( "" + value + " % " + parentIntervalMillis + " >= " + zoomLevelProperty.get().getInterval()
		//				* 1000 );
		if( value % parentIntervalMillis >= zoomLevelProperty.get().getInterval() * 1000 )
		{
			return Long.toString( value / ( ( 1000 * zoomLevelProperty.get().getInterval() ) )
					% zoomLevelProperty.get().zoomOut().getInterval() );
		}
		return Long.toString( value / parentIntervalMillis ) + parentZoomLevel.oneCharacterAbbreviation();
	}

	private static Number fromString( String s, ZoomLevel fromZoomLevel )
	{
		try
		{
			return Long.parseLong( s ) * fromZoomLevel.getInterval() * 1000;
		}
		catch( NumberFormatException e )
		{
			return Long.parseLong( dropLastChar( s ) ) * fromZoomLevel.zoomOut().getInterval() * 1000;
		}
	}

	private static String dropLastChar( String s )
	{
		return s.substring( 0, s.length() - 1 );
	}

	public String changeZoomLevel( String s, ZoomLevel fromZoomLevel )
	{
		return toString( fromString( s, fromZoomLevel ) );
	}

	@Override
	public Number fromString( String _ )
	{
		throw new UnsupportedOperationException();
	}
}
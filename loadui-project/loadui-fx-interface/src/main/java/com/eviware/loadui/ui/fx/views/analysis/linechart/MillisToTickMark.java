package com.eviware.loadui.ui.fx.views.analysis.linechart;

import javafx.beans.property.SimpleObjectProperty;
import javafx.util.StringConverter;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;

import com.eviware.loadui.api.charting.line.ZoomLevel;

final class MillisToTickMark extends StringConverter<Number>
{
	private final SimpleObjectProperty<ZoomLevel> zoomLevelProperty;
	private final PeriodFormatter timeFormatter;

	MillisToTickMark( SimpleObjectProperty<ZoomLevel> zoomLevelProperty, PeriodFormatter timeFormatter )
	{
		this.zoomLevelProperty = zoomLevelProperty;
		this.timeFormatter = timeFormatter;
	}

	@Override
	public String toString( Number n )
	{
		long value = n.longValue();

		if( value == 0 )
			return "0";

		ZoomLevel zoomLevel = zoomLevelProperty.get();
		ZoomLevel parentZoomLevel = zoomLevel.zoomOut();
		long parentInterval = parentZoomLevel.getInterval();
		//		System.out.println( "" + value / 1000 + " % " + parentInterval + " >= " + zoomLevel.getInterval() );

		if( value / 1000 % parentInterval >= zoomLevel.getInterval() )
		{
			return Long.toString( value / 1000 / zoomLevel.getInterval()
					% ( parentZoomLevel.getInterval() / zoomLevel.getInterval() ) );
		}
		return prettyPrintTime( n );
	}

	private String prettyPrintTime( Number n )
	{

		Period period = new Period( n.longValue() );
		return timeFormatter.print( period.normalizedStandard() );
	}

	private Number fromString( String s, ZoomLevel fromZoomLevel )
	{
		try
		{
			return Long.parseLong( s ) * fromZoomLevel.getInterval() * 1000;
		}
		catch( NumberFormatException e )
		{
			return timeFormatter.parsePeriod( s ).toStandardDuration().getMillis();
		}
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
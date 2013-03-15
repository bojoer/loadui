package com.eviware.loadui.ui.fx.views.analysis.linechart;

import static org.junit.Assert.*;
import javafx.beans.property.SimpleObjectProperty;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.charting.line.ZoomLevel;

public class MillisToTickmarkTest
{
	private final SimpleObjectProperty<ZoomLevel> zoomLevelProperty = new SimpleObjectProperty<>( ZoomLevel.SECONDS );
	private MillisToTickMark millisToTickMark;

	@Before
	public void setUp() throws Exception
	{
		zoomLevelProperty.set( ZoomLevel.SECONDS );
		millisToTickMark = new MillisToTickMark( zoomLevelProperty, ScrollableLineChart.timeFormatter );
	}

	@Test
	public final void testToString()
	{
		assertEquals( "19", millisToTickMark.toString( 19001 ) );
		assertEquals( "34", millisToTickMark.toString( 94669 ) );
		assertEquals( "5m", millisToTickMark.toString( 60 * 1000 * 65 ) );
		assertEquals( "47m", millisToTickMark.toString( new Double( "9.2820739E7" ) ) );

		zoomLevelProperty.set( ZoomLevel.MINUTES );
		assertEquals( "2h", millisToTickMark.toString( 60 * 1000 * 65 * 24 ) );

		assertEquals( "1", millisToTickMark.changeZoomLevel( "1m", ZoomLevel.SECONDS ) );
		assertEquals( "5", millisToTickMark.toString( 305000 ) );
		assertEquals( "2h", millisToTickMark.toString( 7200058 ) );
		assertEquals( "1", millisToTickMark.toString( 7260899 ) );

		zoomLevelProperty.set( ZoomLevel.SECONDS );
		assertEquals( "1h", millisToTickMark.toString( 3600000 ) );
		assertEquals( "1m", millisToTickMark.changeZoomLevel( "1", ZoomLevel.MINUTES ) );
		assertEquals( "1h", millisToTickMark.changeZoomLevel( "1h", ZoomLevel.MINUTES ) );
		assertEquals( "1d", millisToTickMark.changeZoomLevel( "1d", ZoomLevel.MINUTES ) );

		zoomLevelProperty.set( ZoomLevel.HOURS );
		assertEquals( "", millisToTickMark.changeZoomLevel( "2m", ZoomLevel.MINUTES ) );
		assertEquals( "1d", millisToTickMark.toString( ( 24 * 3600 + 5 * 60 ) * 1000 ) );

		zoomLevelProperty.set( ZoomLevel.WEEKS );
		assertEquals( "2", millisToTickMark.toString( 1209600000 ) );
	}

	@Test
	public final void testGeneratePositionString()
	{
		assertEquals( "", millisToTickMark.generatePositionString( 50 * 1000 ) );
		assertEquals( "1m", millisToTickMark.generatePositionString( 100 * 1000 ) );
		assertEquals( "4d 4h 59m", millisToTickMark.generatePositionString( ( 100 * 3600 + 60 * 59 ) * 1000 ) );
		assertEquals( "4d 5h", millisToTickMark.generatePositionString( ( 101 * 3600 ) * 1000 ) );
		assertEquals( "1h", millisToTickMark.generatePositionString( 3600 * 1000 ) );
		assertEquals( "1h 1m", millisToTickMark.generatePositionString( ( 3600 + 62 ) * 1000 ) );

		zoomLevelProperty.set( ZoomLevel.MINUTES );
		assertEquals( "4d 4h", millisToTickMark.generatePositionString( ( 100 * 3600 + 60 * 59 ) * 1000 ) );
		assertEquals( "1d", millisToTickMark.generatePositionString( 86460000 ) );

		zoomLevelProperty.set( ZoomLevel.HOURS );
		assertEquals( "4d", millisToTickMark.generatePositionString( ( 100 * 3600 + 60 * 59 ) * 1000 ) );

		zoomLevelProperty.set( ZoomLevel.DAYS );
		assertEquals( "", millisToTickMark.generatePositionString( ( 100 * 3600 + 60 * 59 ) * 1000 ) );

		zoomLevelProperty.set( ZoomLevel.WEEKS );
		assertEquals( "", millisToTickMark.generatePositionString( ( 100 * 3600 + 60 * 59 ) * 1000 ) );
	}
}

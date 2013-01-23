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
		millisToTickMark = new MillisToTickMark( zoomLevelProperty, ScrollableLineChart.timeFormatter );
	}

	@Test
	public final void testToStringT()
	{
		assertEquals( "19", millisToTickMark.toString( 19001 ) );
		assertEquals( "34", millisToTickMark.toString( 94669 ) );
		zoomLevelProperty.set( ZoomLevel.MINUTES );
		assertEquals( "1", millisToTickMark.changeZoomLevel( "1m", ZoomLevel.SECONDS ) );
		assertEquals( "5", millisToTickMark.toString( 305000 ) );
		assertEquals( "2h", millisToTickMark.toString( 7200058 ) );
		assertEquals( "1", millisToTickMark.toString( 7260899 ) );

		zoomLevelProperty.set( ZoomLevel.SECONDS );
		assertEquals( "1h", millisToTickMark.toString( 3600000 ) );
		assertEquals( "1m", millisToTickMark.changeZoomLevel( "1", ZoomLevel.MINUTES ) );
		assertEquals( "1h", millisToTickMark.changeZoomLevel( "1h", ZoomLevel.MINUTES ) );
		assertEquals( "1d", millisToTickMark.changeZoomLevel( "1d", ZoomLevel.MINUTES ) );

		zoomLevelProperty.set( ZoomLevel.WEEKS );
		assertEquals( "2", millisToTickMark.toString( 1209600000 ) );
	}
}

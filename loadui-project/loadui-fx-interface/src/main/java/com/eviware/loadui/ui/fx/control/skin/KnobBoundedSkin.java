package com.eviware.loadui.ui.fx.control.skin;

import com.eviware.loadui.ui.fx.control.Knob;

public class KnobBoundedSkin extends KnobSkin
{
	public KnobBoundedSkin( Knob knob )
	{
		super( knob );

		setStartAngle( 3 * Math.PI / 4 );
		setAngleSpan( 3 * Math.PI / 2 );

		getHandle().rotateProperty().bind( angleProperty().divide( Math.PI ).multiply( 180 ) );
	}
}

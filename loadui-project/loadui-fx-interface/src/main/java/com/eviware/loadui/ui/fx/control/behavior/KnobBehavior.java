package com.eviware.loadui.ui.fx.control.behavior;

import com.eviware.loadui.ui.fx.control.Knob;
import com.sun.javafx.scene.control.behavior.BehaviorBase;

public class KnobBehavior extends BehaviorBase<Knob>
{
	public KnobBehavior( Knob control )
	{
		super( control );
	}

	public void increment( int steps )
	{
		getControl().setValue( getControl().getValue() + steps * getControl().getStep() );
	}

	public void decrement( int steps )
	{
		getControl().setValue( getControl().getValue() - steps * getControl().getStep() );
	}
}

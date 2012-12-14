package com.eviware.loadui.ui.fx.views.assertions;

import com.eviware.loadui.api.serialization.ListenableValue;
import com.eviware.loadui.api.serialization.Resolver;
import com.eviware.loadui.api.traits.Labeled;

public interface AssertableWrapper<T extends ListenableValue<Number>> extends Labeled
{
	public Resolver<T> getResolver();

	public T getAssertable();
}

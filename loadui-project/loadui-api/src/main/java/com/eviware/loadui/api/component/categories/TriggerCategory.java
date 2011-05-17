package com.eviware.loadui.api.component.categories;

/**
 * This is here because some of the Groovy Components in loadUI 1.0 have an
 * import statement importing this class, even though it is never used. The real
 * TriggerCategory has been renamed to GeneratorCategory. The scripts have been
 * updated, but if you have a sample project which is from 1.0, it will fail to
 * load some of the components if this class doesn't exist. DO NOT USE THIS
 * CLASS!
 * 
 * @author dain.nilsson
 */
@Deprecated
public final class TriggerCategory
{
	@Deprecated
	public TriggerCategory()
	{
		throw new RuntimeException();
	}
}

package com.eviware.loadui.api.traits;

import edu.umd.cs.findbugs.annotations.OverrideMustInvoke;

public interface Initializable
{
	/**
	 * If the Initializable also implements EventFirer, it should fire a
	 * BaseEvent with the INITIALIZED constant as a key to inform listeners that
	 * it has been initialized.
	 */
	public static final String INITIALIZED = Initializable.class.getSimpleName() + "@initialized";

	/**
	 * Causes the Initializable to initialize its own state, as well as any child
	 * objects which need to be initialized. Child objects should be initialized
	 * fully here, calling both init() and postInit().
	 */
	@OverrideMustInvoke
	public void init();

	/**
	 * Called after init() has been completed (and all children are initialized
	 * fully), to do any last initialization before the Initializable can be
	 * considered fully initialized.
	 */
	@OverrideMustInvoke
	public void postInit();
}

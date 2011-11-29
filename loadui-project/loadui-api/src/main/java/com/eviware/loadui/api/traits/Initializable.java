/*
 * Copyright 2011 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
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

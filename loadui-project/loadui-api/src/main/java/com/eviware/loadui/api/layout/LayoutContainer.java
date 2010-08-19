/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.api.layout;

import java.util.List;

/**
 * A LayoutComponent for holding other LayoutComponents.
 * 
 * @author dain.nilsson
 */
public interface LayoutContainer extends LayoutComponent, List<LayoutComponent>
{
	public String getLayoutConstraints();

	public String getColumnConstraints();

	public String getRowConstraints();

	/**
	 * Once this method has been called, the LayoutContainer becomes immutable.
	 * Any call to a method that would modify the LayoutContainer after it has
	 * become frozen will result in an UnsupportedOperationException. Subsequent
	 * calls to this method will have no effect.
	 */
	public void freeze();

	/**
	 * Checks if the LayoutContainer has yet been frozen or not.
	 * 
	 * @return If the LayoutContainer has been frozen.
	 */
	public boolean isFrozen();
}

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
package com.eviware.loadui.api.layout;

/**
 * A LayoutComponent representing a separator. Likely to be drawn as a
 * horizontal or vertical line. Has a boolean that defines if it is horizontal
 * or vertical.
 * 
 * @author dain.nilsson
 */
public interface SeparatorLayoutComponent extends LayoutComponent
{
	/**
	 * If true, this Separator is vertical.
	 * 
	 * @return
	 */
	public boolean isVertical();
}

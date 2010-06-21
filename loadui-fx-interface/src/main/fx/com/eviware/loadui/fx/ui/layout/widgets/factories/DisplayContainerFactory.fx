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
/*
*DisplayContainerFactory.fx
*
*Created on apr 19, 2010, 12:38:02 em
*/

package com.eviware.loadui.fx.ui.layout.widgets.factories;

import com.eviware.loadui.fx.ui.layout.widgets.DisplayContainer;

import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.api.layout.WidgetCreationException;
import com.eviware.loadui.api.layout.WidgetFactory;

public class DisplayContainerFactory extends WidgetFactory {
	override function getId() {
		"display"
	}
	
	override function buildWidget( lc:LayoutComponent ) {
		if( lc instanceof LayoutContainer ) {
			return DisplayContainer { layoutComponent: lc }
		}
		
		throw new WidgetCreationException("The DisplayContainer widget requires a LayoutContainer!");
	}
}

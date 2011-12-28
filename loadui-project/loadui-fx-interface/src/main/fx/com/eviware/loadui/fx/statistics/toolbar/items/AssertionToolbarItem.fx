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
package com.eviware.loadui.fx.statistics.toolbar.items;

import com.eviware.loadui.fx.statistics.toolbar.StatisticsToolbarItem;
import com.eviware.loadui.fx.FxUtils.*;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.util.ModelUtils;

public class AssertionToolbarItem extends StatisticsToolbarItem {
	
	override var label = bind ModelUtils.getLabelHolder( assertionItem ).label;
	override var tooltip = bind "Adds {label} to a chart";
	
	public var assertionItem: AssertionItem on replace oldValue {
		if( not FX.isInitialized( icon ) ) {
			icon = FxUtils.getImageFor( assertionItem );
		}
	}
	
}
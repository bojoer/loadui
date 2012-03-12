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
package com.eviware.loadui.fx.assertions;

import com.eviware.loadui.fx.util.ModelUtils;
import com.eviware.loadui.fx.ui.toolbar.ToolbarItemNode;
import com.eviware.loadui.fx.FxUtils;

import com.eviware.loadui.api.statistics.StatisticHolder;

public class StatisticHolderAssertionToolbarItem extends ToolbarItemNode {
	def labelHolder = ModelUtils.getLabelHolder( null );
	
	override var label = bind labelHolder.label;
	override var tooltip = bind "Add an assertion to {labelHolder.label}";
	
   public var statisticHolder:StatisticHolder on replace oldValue {
   	labelHolder.labeled = statisticHolder;

      if( not FX.isInitialized( icon ) ) {
      	icon = FxUtils.getImageFor( statisticHolder );
      }
   }
}
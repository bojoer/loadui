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
package com.eviware.loadui.fx.statistics.toolbar;

import com.eviware.loadui.fx.ui.toolbar.Toolbar;

import com.eviware.loadui.fx.statistics.toolbar.items.*;
import com.eviware.loadui.fx.statistics.StatisticsWindow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.EventObject;

import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.util.BeanInjector;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;

import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.component.ComponentDescriptor;

import com.eviware.loadui.fx.FxUtils;

public class StatisticsToolbar extends Toolbar, EventHandler {
    
    var manager: StatisticsManager;
    
    var componentRegistry: ComponentRegistry;
    
    var statHolderMap: Map = new HashMap();
    
    init {
        manager = BeanInjector.getBean(StatisticsManager.class);
        manager.addEventListener( BaseEvent.class, this );
        componentRegistry = BeanInjector.getBean(ComponentRegistry.class);
    }
    
    postinit {
        addChartItems();
        addAnalysisItems();
        for(sh in manager.getStatisticHolders()){
            addStatisticHolder(sh);
        }
    }
    
    override var toolbarTitle = "Statictics";
    
    override var groupOrder = StatisticsGroupOrder{};
    
    override var itemOrder = StatisticsItemOrder{};
    
    override function handleEvent(e: EventObject) { 
		if( e instanceof CollectionEvent ) {
			def event: CollectionEvent = e as CollectionEvent;
			def element: Object = event.getElement();
			if(element instanceof StatisticHolder){
				def sh: StatisticHolder = element as StatisticHolder;
				if(event.getEvent() == CollectionEvent.Event.ADDED){
					FxUtils.runInFxThread( function():Void {
						addStatisticHolder(element as StatisticHolder);
					});
				}
				else if(event.getEvent() == CollectionEvent.Event.REMOVED){
					FxUtils.runInFxThread( function():Void {
						removeStatisticHolder(element as StatisticHolder);
					});
				}
			}
		}
	}
	
	function addStatisticHolder(sh: StatisticHolder){
		if(sh.getStatisticVariableNames().size() == 0){
		 	return;   
		}
		if(sh instanceof ComponentItem){
      	def cti: ComponentToolbarItem = ComponentToolbarItem {
				component: sh as ComponentItem
				descriptor: componentRegistry.findDescriptor((sh as ComponentItem).getType())
			} 
      	addItem(cti);
      	statHolderMap.put(sh, cti);
		}
	}
	
	function removeStatisticHolder(sh: StatisticHolder){
	    if(sh instanceof ComponentItem){
		    def cti: ComponentToolbarItem = statHolderMap.get(sh) as ComponentToolbarItem;
		    if(cti != null){
		    	removeItem(cti);
		    }
		    statHolderMap.remove(sh);
	    }
	}
	
	function addChartItems(){
	   def item: ChartToolbarItem = ChartToolbarItem {
	   	type: com.eviware.loadui.api.statistics.model.chart.LineChartView.class.getName()
			label: "Line Chart"
			tooltip: "Create new line chart"
			//icon: 
		} 
   	addItem(item);
	}

	function addAnalysisItems(){
	   def item: AnalysisToolbarItem = AnalysisToolbarItem {
			label: "Predefined A"
			tooltip: "Create new predefined chart A"
			//icon: 
		} 
   	addItem(item);
	}
	
}
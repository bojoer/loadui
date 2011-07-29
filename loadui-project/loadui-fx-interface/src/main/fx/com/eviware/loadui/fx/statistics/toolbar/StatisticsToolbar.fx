/* 
 * Copyright 2011 eviware software ab
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
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.StringUtils;

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
        for( sh in manager.getStatisticHolders() ) handleStatisticHolder( sh );
        componentRegistry = BeanInjector.getBean(ComponentRegistry.class);
    }
    
    postinit {
        //addChartItems();
        addAnalysisItems();
    }
    
    override var toolbarTitle = "Statistics";
    
    override var groupOrder = StatisticsGroupOrder{};
    
    override var itemOrder = StatisticsItemOrder{};
    
    override function handleEvent(e: EventObject) { 
		if( e instanceof CollectionEvent ) {
			def event: CollectionEvent = e as CollectionEvent;
			def source: Object = event.getSource();
			def element: Object = event.getElement();
			if(source instanceof StatisticsManager and element instanceof StatisticHolder){
				def sh: StatisticHolder = element as StatisticHolder;
				if(event.getEvent() == CollectionEvent.Event.ADDED){
					sh.addEventListener( BaseEvent.class, this );
					FxUtils.runInFxThread( function(): Void {
					   //component added to project so add it to 
					   //the toolbar only if it has variables
						handleStatisticHolder(sh);
					});
				}
				else if(event.getEvent() == CollectionEvent.Event.REMOVED){
					sh.removeEventListener( BaseEvent.class, this );
					FxUtils.runInFxThread( function(): Void {
					   //component removed from project so remove it from 
					   //the toolbar even if it has variables
						removeStatisticHolder(sh);
					});
				}
			}
		   else if(source instanceof StatisticHolder and element instanceof StatisticVariable){
			   FxUtils.runInFxThread( function(): Void {
					handleStatisticHolder(source as StatisticHolder);
				});
			}
		}
	}
	
	/** Removes holder from toolbar if it does not have variables and adds it if it does and it wasn't already added */
	function handleStatisticHolder(sh: StatisticHolder){
		if(sh.getStatisticVariableNames().size() == 0){
			// there are no more variables in this holder, remove it from the toolbar
			removeStatisticHolder(sh);
		}
		else if (not statHolderMap.containsKey(sh)) {
		   // there is more than one variable in a holder, so add it to toolbar if it wasn't already added
			if(sh instanceof ComponentItem){
	      	def cti: StatisticHolderToolbarItem = StatisticHolderToolbarItem {
					statisticHolder: sh
					category: "COMPONENTS"
				}
				addItem(cti);
	      	statHolderMap.put(sh, cti);
			} else if( sh instanceof CanvasItem ) {
	      	def cti: StatisticHolderToolbarItem = StatisticHolderToolbarItem {
					statisticHolder: sh
					category: "GLOBAL"
				}
				addItem(cti);
	      	statHolderMap.put(sh, cti);
			}
		}
	}
	
	/** Removes holder from toolbar no matter if it has variables or not */
	function removeStatisticHolder(sh: StatisticHolder){
		def cti: StatisticHolderToolbarItem = statHolderMap.get(sh) as StatisticHolderToolbarItem;
		if(cti != null){
			removeItem(cti);
		}
		statHolderMap.remove(sh);
	}
	
	/** Adds chart toolbar items to the toolbar */
	function addChartItems(){
	   def item: ChartToolbarItem = ChartToolbarItem {
			type: com.eviware.loadui.api.statistics.model.chart.LineChartView.class.getName()
			label: "Line Chart"
			tooltip: "Create new line chart"
			//icon: 
		} 
   	addItem(item);
	}

	/** Adds analysis toolbar items to the toolbar */
	function addAnalysisItems(){
   	for( item in AnalysisItems.ALL )
   		addItem(item);
	}
	
}
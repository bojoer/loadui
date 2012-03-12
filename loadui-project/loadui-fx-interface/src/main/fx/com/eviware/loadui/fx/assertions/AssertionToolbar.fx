/*
 * Copyright 2011 SmartBear Software
 */
package com.eviware.loadui.fx.assertions;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.toolbar.Toolbar;
import com.eviware.loadui.fx.util.ModelUtils;
import com.eviware.loadui.fx.ui.toolbar.GroupOrder;

import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.events.CollectionEvent;

import java.util.HashMap;
import java.util.Comparator;

public class AssertionToolbar extends Toolbar {
	override var toolbarTitle = "Assert";
	
	def statisticHolderMap = new HashMap();
	def variableListener = VariableListener {};
	
	var statisticHolders:ModelUtils.CollectionHolder;
	
	def onAdd = function( elem:Object ):Void {
		def statisticHolder = elem as StatisticHolder;
		def item = if( statisticHolder instanceof ComponentItem ) {
      	StatisticHolderAssertionToolbarItem {
				statisticHolder: statisticHolder
				category: "COMPONENTS"
			}
		} else if( statisticHolder instanceof CanvasItem ) {
      	StatisticHolderAssertionToolbarItem {
				statisticHolder: statisticHolder
				category: "GLOBAL"
			}
		} else {
			null
		}
		
		if( item == null ) {
			return;
		}
		
		statisticHolder.addEventListener( CollectionEvent.class, variableListener );
		statisticHolderMap.put( elem, item );
		handleStatisticHolder( statisticHolder );
	}
	
	def onRemove = function( elem:Object ):Void {
		(elem as StatisticHolder).removeEventListener( CollectionEvent.class, variableListener );
		removeItem( statisticHolderMap.get( elem ) as StatisticHolderAssertionToolbarItem );
	}
	
	function handleStatisticHolder( statisticHolder:StatisticHolder ) {
		def item = statisticHolderMap.get( statisticHolder ) as StatisticHolderAssertionToolbarItem;
		if( statisticHolder.getStatisticVariableNames().size() == 0 ) {
			removeItem( item );
		} else {
			addItem( item );
		}
	}
	
	public-init var statisticsManager:StatisticsManager on replace {
		statisticHolders = ModelUtils.CollectionHolder {
			owner: statisticsManager,
			key: StatisticsManager.STATISTIC_HOLDERS,
			items: statisticsManager.getStatisticHolders(),
			onAdd: bind onAdd,
			onRemove: bind onRemove
		}
		for( statisticHolder in statisticHolders.items ) {
			onAdd( statisticHolder );
		}
	}
	
	override var groupOrder = new CategoryOrder(); 
	//override var itemOrder = new ComponentOrder();
}

class VariableListener extends WeakEventHandler {
	override function handleEvent( e ) {
		def event = e as CollectionEvent;
		if( StatisticHolder.STATISTIC_VARIABLES.equals( event.getKey() ) ) {
		   FxUtils.runInFxThread( function():Void {
		   	handleStatisticHolder( event.getSource() as StatisticHolder );
			});
		}
	}
}

class CategoryOrder extends GroupOrder {
	override var groupOrder = [
		"GLOBAL", "COMPONENTS"
	];
}

//class ComponentOrder extends GroupOrder {
//	override var groupOrder = [
//		"GLOBAL", "COMPONENTS"
//	];
//}
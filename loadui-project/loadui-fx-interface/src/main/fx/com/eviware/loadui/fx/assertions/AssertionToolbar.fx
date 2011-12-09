/*
 * Copyright 2011 SmartBear Software
 */
package com.eviware.loadui.fx.assertions;

import com.eviware.loadui.fx.ui.toolbar.Toolbar;
import com.eviware.loadui.fx.util.ModelUtils;
import com.eviware.loadui.fx.ui.toolbar.GroupOrder;

import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.model.ComponentItem;

import java.util.HashMap;
import java.util.Comparator;

public class AssertionToolbar extends Toolbar {
	override var toolbarTitle = "Assert";
	
	def statisticHolderMap = new HashMap();
	
	var statisticHolders:ModelUtils.CollectionHolder;
	
	def onAdd = function( elem:Object ):Void {
		def statisticHolder = elem as StatisticHolder;
		def item = if( statisticHolder instanceof ComponentItem ) {
      	StatisticHolderAssertionToolbarItem {
				statisticHolder: statisticHolder
				category: "COMPONENTS"
			}
		} else {
      	StatisticHolderAssertionToolbarItem {
				statisticHolder: statisticHolder
				category: "GLOBAL"
			}
		}
		statisticHolderMap.put( elem, item );
		addItem( item );
	}
	
	def onRemove = function( elem:Object ):Void {
		removeItem( statisticHolderMap.get( elem ) as StatisticHolderAssertionToolbarItem );
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
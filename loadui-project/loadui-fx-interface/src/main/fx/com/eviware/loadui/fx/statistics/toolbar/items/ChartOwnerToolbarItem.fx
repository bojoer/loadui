/*
*ChartOwnerToolbarItem.fx
*
*Created on Jan 5, 2012, 13:11:02 PM
*/

package com.eviware.loadui.fx.statistics.toolbar.items;

import com.eviware.loadui.fx.FxUtils.*;

import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.statistics.model.Chart;

import com.eviware.loadui.fx.statistics.toolbar.StatisticsToolbarItem;
import com.eviware.loadui.fx.util.ModelUtils;

public class ChartOwnerToolbarItem extends StatisticsToolbarItem {
	var labelHolder:ModelUtils.LabelHolder;
	public var owner:Chart.Owner on replace {
		labelHolder = ModelUtils.getLabelHolder( owner );
	}
	
	override var label = bind labelHolder.label;
}
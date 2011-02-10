/*
*PageList.fx
*
*Created on feb 10, 2011, 18:00:43 em
*/

package com.eviware.loadui.fx.ui.pagelist;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.ui.pagination.Pagination;

public class PageList extends VBox, Pagination {
	public var label:String = "PageList";
	
	var itemWidth: Number;
	
	override var displayedItems on replace {
		itemWidth = 0;
		for( x in displayedItems )
			itemWidth = Math.max( itemWidth, x.layoutBounds.width );
	}
	
	init {
		content = [
			Region { styleClass: "page-list", width: bind width, height: bind height, managed: false },
			HBox { content: [ Label { text: bind label } ] },
			HBox { content: [
				Button {},
				HBox { content: bind displayedItems },
				Button {}
			] },
			Separator {},
			HBox {}
		];
	}
}

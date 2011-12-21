/*
*AssertionList.fx
*
*Created on Dec 16, 2011, 09:41:18 AM
*/

package com.eviware.loadui.fx.assertions;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.geometry.HPos;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.api.assertion.AssertionItem;

import com.eviware.loadui.fx.util.ModelUtils;

public class AssertionList extends Stack {
	
	def vbox = VBox {
		spacing: 11
		layoutInfo: LayoutInfo { vfill: false, vpos: VPos.TOP }
	}
	
	def placeholder = Stack {
		styleClass: "assertion-box"
		layoutInfo: LayoutInfo { height: 56 }
		content: [
			Region { styleClass: "assertion-box" },
			Label { text: "Drop items from the sidebar to create an assertion" }
		]
	}
	
	init {
		content = vbox;
	}
	
	public var items: AssertionItem[] on replace {
		vbox.content = if( sizeof items == 0 ) {
			placeholder
		} else {
			for( item in items ) AssertionBox { assertionItem: item }
		}
	}
}

class AssertionBox extends Stack {
	override var styleClass = "assertion-box";
	
	public-init var assertionItem:AssertionItem on replace {
		content = [
			Region { styleClass: "assertion-box" },
			HBox {
				spacing: 4
				padding: Insets { left: 12, top: 12, right: 12, bottom: 12 }
				content: [
					Region { style: "-fx-background-color: red;", layoutInfo: LayoutInfo { width: 19, height: 12, hfill: false, vfill: false } },
					VBox {
						layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS }
						content: [
							Label { styleClass: "title", text: bind ModelUtils.getLabelHolder( assertionItem ).label },
							Label { text: "Breadcrumbs > Go here > Once Finished : Some Range 1 - 40" }
						]
					}, VBox {
						nodeHPos: HPos.RIGHT
						content: [
							Label { text: "FAILURES" },
							Label { styleClass: "count", text: "12345" },
						]
					}
				] 
			}
		]
	}
}
/*
*AssertionList.fx
*
*Created on Dec 16, 2011, 09:41:18 AM
*/

package com.eviware.loadui.fx.assertions;

import javafx.scene.layout.VBox;
import javafx.scene.layout.Stack;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.control.Label;
import javafx.geometry.VPos;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.api.assertion.AssertionItem;

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
			Label { text: "Drop items fromt he sidebar to create an assertion" }
		]
	}
	
	init {
		content = vbox;
	}
	
	public var items: AssertionItem[] on replace {
		vbox.content = if( sizeof items == 0 ) {
			placeholder
		} else {
			for( item in items ) {
				AssertionBox { assertionItem: item }
			}
		}
	}
}

class AssertionBox extends Stack {
	public-init var assertionItem:AssertionItem on replace {
		content = [
			Region { styleClass: "assertion-box" }
		]
	}
}
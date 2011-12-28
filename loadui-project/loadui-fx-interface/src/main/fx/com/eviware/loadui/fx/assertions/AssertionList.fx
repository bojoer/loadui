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

import javafx.scene.input.MouseEvent;

import javafx.scene.shape.Polygon;
import javafx.scene.Group;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.testevents.TestEventManager.TestEventObserver;
import com.eviware.loadui.api.testevents.TestEvent.Entry;
import com.eviware.loadui.util.BeanInjector;

import com.eviware.loadui.fx.dialogs.RenameModelItemDialog;
import com.eviware.loadui.fx.dialogs.DeleteModelItemDialog;
import com.eviware.loadui.fx.util.ModelUtils;
import com.eviware.loadui.fx.FxUtils;

import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.MenuButton;
import com.javafx.preview.control.Menu;
import com.javafx.preview.control.PopupMenu;

import com.eviware.loadui.api.traits.Labeled;

import java.util.HashMap;

public class AssertionList extends Stack {
	
	def observer = AssertionObserver{}
	
	var manager: TestEventManager = bind BeanInjector.getBean( TestEventManager.class ) on replace oldValue {
	    if(oldValue != null){
	        oldValue.unregisterObserver(observer);
	    }
		manager.registerObserver(observer);
	}
		
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
	
	public var assertionBoxMap = new HashMap();
	
	public var items: AssertionItem[] on replace {
	    assertionBoxMap.clear();
		vbox.content = if( sizeof items == 0 ) {
			placeholder
		} else {
			for( item in items ){
			    def aBox = AssertionBox { assertionItem: item };
				assertionBoxMap.put(item.getLabel(), aBox);
				aBox;
			}
		}
	}
}

class AssertionBox extends Stack {
	override var styleClass = "assertion-box";
	
	var value: Number = 0;
	
	public function increment() {
	    value++;
	}
	
	var menu: PopupMenu = PopupMenu {
	    onShowing: function() {}
	    onShown: function() {}
	    items: [
			MenuItem {
				text: ##[RENAME]"Rename"
				action: function() { RenameModelItemDialog { labeled: assertionItem as Labeled.Mutable } }
			},
			MenuItem {
	            text: ##[DELETE]"Delete"
	            action: function() { DeleteModelItemDialog { modelItem: assertionItem } }
			}
        ]
	}
	
	var menuButton: Group = Group {
	    content: [
	    	Region { style: "-fx-background-color: transparent;", layoutInfo: LayoutInfo { width: 9, height: 9, hfill: false, vfill: false } },
	    	Polygon {
	    	    styleClass: "arrow"
	    	    points: [
	    	         0.0, 3.0,
	    	         5.0, 3.0,
	    	         2.5, 6.0
	    	    ]
	    	}
	    ]
	    onMouseClicked: function( e:MouseEvent ):Void { menu.show(menuButton, HPos.CENTER, VPos.BOTTOM, 0, 0); }
	}
	
	public-init var assertionItem:AssertionItem on replace oldValue {
		content = [
			Region { styleClass: "assertion-box" },
			HBox {
				spacing: 4
				padding: Insets { left: 15, top: 7, right: 12, bottom: 8 }
				content: [
					VBox {
					    spacing: 0
						layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS }
						content: [
							HBox {
							    nodeVPos: VPos.CENTER
							    spacing: 7
							    padding: Insets { left: 0, top: 0, right: 0, bottom: 0 }
							    layoutInfo: LayoutInfo { hfill: false, hgrow: Priority.NEVER }
							    content: [
							    	Region { style: "-fx-background-color: red;", layoutInfo: LayoutInfo { width: 19, height: 12, hfill: false, vfill: false } },
									Label { styleClass: "title", text: bind ModelUtils.getLabelHolder( assertionItem ).label }
									Region { styleClass: "separator", layoutInfo: LayoutInfo { width: 1, height: 14, hfill: false, vfill: false } },
									menuButton
							    ]
							}
							HBox {
							    nodeVPos: VPos.CENTER
							    spacing: 7
							    padding: Insets { left: 0, top: 0, right: 0, bottom: 0 }
							    layoutInfo: LayoutInfo { hfill: false, hgrow: Priority.NEVER }
							    content: [
							    	Region { style: "-fx-background-color: transparent;", layoutInfo: LayoutInfo { width: 19, height: 12, hfill: false, vfill: false } },
							    	Label { text: breadcrumb() }
							    ]
							}
						]
					}, VBox {
					    spacing: 0
						nodeHPos: HPos.RIGHT
						content: [
							Label { text: "FAILURES" },
							Label { styleClass: "count", text: bind String.format("%.0f", value) },
						]
					}
				] 
			},
			menu
		]
	}
	
	function breadcrumb(): String {
	    var parentLabel: String = "";
	    var parent = assertionItem.getParent();
	    if(parent != null){
	        if(parent instanceof Labeled){
	            parentLabel = (parent as Labeled).getLabel();
	        }
	    }
	    var variableLabel: String = "";
	    if(assertionItem.getValue() instanceof Labeled){
	        variableLabel = (assertionItem.getValue() as Labeled).getLabel();
	    }
	    "{parentLabel} > {variableLabel} : {assertionItem.getConstraint()} : Tolerance {assertionItem.getToleranceAllowedOccurrences()} times within {assertionItem.getTolerancePeriod()} seconds";
	}
}

class AssertionObserver extends TestEventObserver {
    override function onTestEvent( eventEntry: Entry ){
    	if(eventEntry.getTypeLabel().equals("AssertionFailureEvent")){
	    	FxUtils.runInFxThread( function():Void {
		    	def assertionBox: AssertionBox = assertionBoxMap.get(eventEntry.getSourceLabel()) as AssertionBox; 
		    	if(assertionBox != null){
		    	    assertionBox.increment();
		    	}
			});
    	}
    }
}
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
import javafx.scene.control.ScrollView;
import javafx.scene.control.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.MouseEvent;

import javafx.scene.shape.Polygon;
import javafx.scene.Group;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.testevents.TestEventManager.TestEventObserver;
import com.eviware.loadui.api.testevents.TestEvent.Entry;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.events.BaseEvent;
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
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.fx.ui.dnd.DroppableNode;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.assertions.StatisticHolderAssertionToolbarItem;

import com.eviware.loadui.fx.FxUtils.*;

import java.util.HashMap;

def assertionIcon = Image { url: "{__ROOT__}images/png/assertion_icon_neutral.png" };

public class AssertionList extends Stack {
	
	def menu = CustomPopupMenu{}
	
	def manager = bind BeanInjector.getBean( TestEventManager.class );

	def vbox = VBox {
		spacing: 11
		layoutInfo: LayoutInfo { vfill: false, vpos: VPos.TOP }
		padding: Insets { left: 0, top: 0, right: 17, bottom: 0 }
	}
	
	def placeholder = Stack {
		styleClass: "assertion-box"
		layoutInfo: LayoutInfo { height: 56 }
		content: [
			Region { styleClass: "assertion-box" },
			Label { text: "Drop items from the sidebar to create an assertion" }
		]
	}
	
	def scrollView: ScrollView = ScrollView {
		hbarPolicy: ScrollBarPolicy.NEVER
		vbarPolicy: ScrollBarPolicy.ALWAYS
		fitToWidth: true
		node: vbox
	}

	def droppable = DroppableNode {
		contentNode: Region { style: "-fx-background-color: transparent;", layoutInfo: LayoutInfo { width: bind scrollView.layoutBounds.width, height: bind scrollView.layoutBounds.height } }
		accept: function( d:Draggable ) {
			d.node instanceof StatisticHolderAssertionToolbarItem
		}
		onDrop: function( d:Draggable ) {
			if ( d.node instanceof StatisticHolderAssertionToolbarItem ) {
				AddAssertionDialog { statisticHolder: ( d.node as StatisticHolderAssertionToolbarItem ).statisticHolder };
			}
		}
	}
	
	init {
		content = [ scrollView, droppable ];
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
		insert menu into vbox.content;
	}
}

class AssertionBox extends Stack {
	override var styleClass = "assertion-box";
	
	def failureListener = FailureListener {};
	
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
		onMouseClicked: function( e:MouseEvent ):Void { menu.showFor(menuButton, assertionItem); }
	}
	
	public-init var assertionItem: AssertionItem on replace oldValue {
		def labelHolder = ModelUtils.getLabelHolder( assertionItem );
		assertionItem.addEventListener( BaseEvent.class, failureListener );
		content = [
			menu,
			Region { styleClass: "background" },
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
									ImageView { image: assertionIcon }
									Label { styleClass: "title", text: bind labelHolder.label }
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
									Label { text: bind labelHolder.description }
								]
							}
						]
					}, VBox {
						spacing: 0
						nodeHPos: HPos.RIGHT
						content: [
							Label { text: "FAILURES" },
							Label { styleClass: "count", text: bind "{failureListener.failures}" },
						]
					}
				] 
			}
		];
		layout();
	}
	
	function breadcrumb(): String {
		var parentLabel: String = "";
		var parent = assertionItem.getParent();
		if(parent != null){
			if(parent instanceof Labeled){
				parentLabel = ( parent as Labeled ).getLabel();
			}
		}
		var variableLabel: String = "";
		if(assertionItem.getValue() instanceof Labeled){
			variableLabel = ( assertionItem.getValue() as Labeled ).getLabel();
		}
		"{parentLabel} > {variableLabel} : {assertionItem.getConstraint()} : Tolerance {assertionItem.getToleranceAllowedOccurrences()} times within {assertionItem.getTolerancePeriod()} seconds";
	}
}

class FailureListener extends WeakEventHandler {
	var failures = 0;
	override function handleEvent( e ) {
		def event = e as BaseEvent;
		if( AssertionItem.FAILURE_COUNT.equals( event.getKey() ) ) {
			failures = ( event.getSource() as AssertionItem ).getFailureCount();
		}
	}
}

class CustomPopupMenu extends PopupMenu {

	public var assertionItem: AssertionItem;
	
	init{
		items = [
			/*MenuItem {
				text: ##[RENAME]"Rename"
				action: function() { RenameModelItemDialog { labeled: assertionItem as Labeled.Mutable } }
			},*/
			MenuItem {
				text: ##[DELETE]"Delete"
				action: function() { DeleteModelItemDialog { modelItem: assertionItem } }
			}
		]		
	}
	
	public function showFor( menuButton: Group, assertionItem: AssertionItem ) {
		this.assertionItem = assertionItem;
		show( menuButton, HPos.CENTER, VPos.BOTTOM, 0, 0 );
	}
}
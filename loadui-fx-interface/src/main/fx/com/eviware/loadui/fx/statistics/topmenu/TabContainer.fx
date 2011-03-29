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
package com.eviware.loadui.fx.statistics.topmenu;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.dialogs.*;
import com.eviware.loadui.fx.dialogs.*;
import com.eviware.loadui.fx.ui.dnd.SortableBox;
import com.eviware.loadui.fx.ui.form.fields.LabelField;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.Deletable;
import com.eviware.loadui.fx.statistics.StatisticsWindow;

import com.eviware.loadui.fx.FxUtils.__ROOT__;
import javafx.fxd.FXDNode;
import javafx.scene.effect.Glow;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.fx.ui.menu.TabcontainerButton;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.LayoutInfo;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.util.Sequences;

import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.PopupMenu;

import java.lang.NumberFormatException;
import java.lang.StringIndexOutOfBoundsException;
import java.util.EventObject;

/**
 * The statistic tabs/pages manager.
 * 
 * @author henrik.olsson
 */
public class TabContainer extends HBox {
	def DEFAULT_PAGE_NAME = "Untitled page";
	def statisticPagesListener = new StatisticPagesListener();

	public-read var currentPage:StatisticPage;
	public var onSelect:function(sp:StatisticPage):Void;
	public var statisticPages:StatisticPages on replace { sortableBox.content = generateTabs(); };

	var latestClickedTab:StatisticsTab;
	var previouslySelectedTab:StatisticsTab;
	var sortableBox:SortableBox;
	def tabGroup:ToggleTabGroup = new ToggleTabGroup();

	def renameAction = MenuItem {
		text: "Rename",
		action: function():Void {
			TabRenameDialog {
			   tabToRename: latestClickedTab as RadioButton,
			   tabButtons: for( t:Toggle in tabGroup.toggles ) t as RadioButton,
			   onOk: function(renamedTab:RadioButton, newName:String):Void {
			   	(renamedTab.value as StatisticPage).setTitle( newName );
			    }
			}.show();
		}
	}
	
	def addTabButton:TabcontainerButton = TabcontainerButton {
		shape: "M0,5 L3,5 3,8 5,8 5,5 8,5 8,3 5,3 5,0 3,0 3,3 0,3 Z"
		action: function():Void {
	    	var highestPageNumber:Integer = 0;
	   	for ( child in statisticPages.getChildren() )
	   	{
	   	   var title:String = child.getTitle();
	      	try
	      	{
   	      	if ( title.substring(0, DEFAULT_PAGE_NAME.length() + 1).equals("{DEFAULT_PAGE_NAME} ")	)
   	      	{
	   	      	   var pageNumber:Integer = Integer.valueOf( title.substring(DEFAULT_PAGE_NAME.length() + 1) );
	   	      	   if ( pageNumber > highestPageNumber )
	   	      	   {
	   	      	   	highestPageNumber = pageNumber
	   	      	   }
					}
				} catch (ex: NumberFormatException) {;}
				  catch (ex: StringIndexOutOfBoundsException) {;}
	   	}
	   	statisticPages.createPage("{DEFAULT_PAGE_NAME} {highestPageNumber + 1}");
		}
	}
	
//	def addTabButton:Group = Group {
//			   content: [
//			   	FXDNode {
//						url: bind openImg
//						visible: true
//						effect: bind if( addTabButton.hover ) Glow{ level: .2 } else null
//					}
//				]
//				onMousePressed: function(e:MouseEvent) {
//		   	    	var highestPageNumber:Integer = 0;
//		   	   	for ( child in statisticPages.getChildren() )
//		   	   	{
//		   	   	   var title:String = child.getTitle();
//		   	      	try
//		   	      	{
//			   	      	if ( title.substring(0, DEFAULT_PAGE_NAME.length() + 1).equals("{DEFAULT_PAGE_NAME} ")	)
//			   	      	{
//				   	      	   var pageNumber:Integer = Integer.valueOf( title.substring(DEFAULT_PAGE_NAME.length() + 1) );
//				   	      	   if ( pageNumber > highestPageNumber )
//				   	      	   {
//				   	      	   	highestPageNumber = pageNumber
//				   	      	   }
//								}
//							} catch (ex: NumberFormatException) {;}
//							  catch (ex: StringIndexOutOfBoundsException) {;}
//		   	   	}
//		   	   	statisticPages.createPage("{DEFAULT_PAGE_NAME} {highestPageNumber + 1}");
//		   		}
//			}

	def deleteAction = MenuItem {
		text: "Delete",
		action: function():Void {
			latestClickedTab.deleteObject();
		}
	}
	
	def contextMenu:PopupMenu = PopupMenu {
		items: [
			renameAction,
			deleteAction
		],
		onShowing: function():Void {
			if ( Sequences.indexOf( AppState.byScene( scene ).overlay.content, contextMenu ) == -1 )
			{
				insert contextMenu into AppState.byScene( scene ).overlay.content;				
			}
			var list:String[] = for (p:StatisticPage in statisticPages.getChildren()) {p.getTitle()};
		},
		onHiding: function():Void {
			delete contextMenu from AppState.byScene( scene ).overlay.content;
		}
	}
	
	override var padding = Insets {left: 16, right: 0};
	override var nodeVPos = VPos.CENTER;
	override var spacing = 9;
	
	def openImg: String = "{__ROOT__}images/execution-selector-open.fxz";
	
	init {
		sortableBox = SortableBox {
			content: generateTabs(),
			spacing: 9,
			padding: Insets {left: 0, right: 0},
			enforceBounds: false,
			styleClass: "statistics-tabs-sortableBox",
			onMoved: function( node:Node, fromIndex:Integer, toIndex:Integer ):Void {
				statisticPages.movePage( (node as StatisticsTab).value as StatisticPage, toIndex );
			}
		};
		content = [
			sortableBox,
			addTabButton,
	   	contextMenu
			];
	}
	
	function generateTabs():StatisticsTab[]
	{
		var tabs:StatisticsTab[];
 		if ( statisticPages != null )
	   {
			tabs = for ( child in statisticPages.getChildren() )
			{
				StatisticsTab {
			   	text: child.getTitle(),
			      value: child
			   }
			}
			if ( sizeof tabs > 0 )
			{
				tabs[0].selected = true; 
			}
			statisticPages.addEventListener( CollectionEvent.class, statisticPagesListener )
		}
		return tabs;
	}
};

class StatisticPagesListener extends EventHandler {
    override function handleEvent(e: EventObject) { 
		def event: CollectionEvent = e as CollectionEvent;
		if(event.getEvent() == CollectionEvent.Event.REMOVED){
			FxUtils.runInFxThread( function(): Void {
			   def sp:StatisticPage = event.getElement() as StatisticPage;
			   for(tab in sortableBox.content){
					if( (tab as RadioButton).value == sp) {
						if( (tab as RadioButton).selected ) (tab as RadioButton).toggleGroup = null;
						delete tab from sortableBox.content;
						break;
			   	}
				}
			previouslySelectedTab.selected = true;
			});
		} else if(event.getEvent() == CollectionEvent.Event.ADDED){
			FxUtils.runInFxThread( function(): Void {
			   def sp:StatisticPage = event.getElement() as StatisticPage;
		   	var tab:StatisticsTab = StatisticsTab {
				   	text: sp.getTitle(),
				      value: sp
	   		}
				insert tab into sortableBox.content;
				tab.selected = true;
			});
		}
	}
}

class ToggleTabGroup extends ToggleGroup {
	override var selectedToggle on replace oldVal {
		currentPage = selectedToggle.value as StatisticPage;
		onSelect( selectedToggle.value as StatisticPage );
		previouslySelectedTab = oldVal as StatisticsTab;
	}
}

class StatisticsTab extends RadioButton, Deletable {
	override var toggleGroup = tabGroup;
	override var blocksMouse = false;
	override var styleClass = "statistics-view-tab";
	override var confirmDialogScene = StatisticsWindow.getInstance().scene;
	override function doDelete():Void {
			(value as StatisticPage).delete();
		};
	override var onMouseClicked = function( e:MouseEvent ) {
		if( e.button == MouseButton.SECONDARY ) {
			latestClickedTab = this;
			contextMenu.show( this, e.screenX, e.screenY );
		}
	};
}

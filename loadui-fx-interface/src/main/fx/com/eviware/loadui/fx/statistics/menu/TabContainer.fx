/*
*TabContainer.fx
*
*Created on jan 12, 2011, 10:21:15 fm
*/

package com.eviware.loadui.fx.statistics.menu;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.dnd.SortableBox;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.VPos;

import java.lang.NumberFormatException;
import java.lang.StringIndexOutOfBoundsException;
import java.util.EventObject;

public class TabContainer extends HBox {
	def DEFAULT_PAGE_NAME = "Untitled page";
	def statisticPagesListener = new StatisticPagesListener();

	public var onSelect:function(sp:StatisticPage):Void;
	public var statisticPages:StatisticPages on replace { sortableBox.content = generateTabs(); };

	var sortableBox:SortableBox;
	var tabs:RadioButton[];
	def tabGroup:ToggleTabGroup = new ToggleTabGroup();
	
	override var padding = Insets {top: 10, bottom: 4, right: 25, left: 25};
	override var nodeVPos = VPos.CENTER;
	override var spacing = 6;
	
	init {
		sortableBox = SortableBox {
			content: generateTabs(),
			spacing: 36,
			padding: Insets {left: 10, top: 20},
			styleClass: "statistics-tabs-sortableBox"
		};
		content = [
			sortableBox,
			Button {
		   	text: "+",
		   	styleClass: "tab-plus",
		   	action: function() {
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
							} catch (e: NumberFormatException) {;}
							  catch (e: StringIndexOutOfBoundsException) {;}
		   	   	}
		   	   	statisticPages.createPage("{DEFAULT_PAGE_NAME} {highestPageNumber + 1}");
		   		}
		   	}
			];
	}
	
	function generateTabs():RadioButton[]
	{
		var tabs:RadioButton[];
 		if ( statisticPages != null )
	   {
			tabs = for ( child in statisticPages.getChildren() )
			{
				RadioButton {
			   	text: child.getTitle(),
			      toggleGroup: tabGroup,
			      value: child,
					blocksMouse: false,
			      styleClass: "statistics-view-tab",
			   }
			}
			if ( sizeof tabs == 1 )
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
					if( (tab as RadioButton).value == sp){
							delete tab from sortableBox.content;
							break;
				   	}
				}
			});
		} else if(event.getEvent() == CollectionEvent.Event.ADDED){
			FxUtils.runInFxThread( function(): Void {
			   def sp:StatisticPage = event.getElement() as StatisticPage;
		   	var tab:RadioButton = RadioButton {
				   	text: sp.getTitle(),
				      toggleGroup: tabGroup,
				      value: sp,
						blocksMouse: false,
						styleClass: "statistics-view-tab"
	   		}
				insert tab into sortableBox.content;
				tab.selected = true;
			});
		}
	}
}

class ToggleTabGroup extends ToggleGroup {
	
	override var selectedToggle on replace oldVal {
		   onSelect( selectedToggle.value as StatisticPage );
	}
}

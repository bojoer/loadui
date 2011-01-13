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
import javafx.scene.control.ToggleButton;
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
	public var statisticPages:StatisticPages on replace
	{
	   //TODO: (A) MERGE WITH B
	   if (statisticPages != null)
	   { 
		   
			sortableBox.content = for ( child in statisticPages.getChildren() )
			{
				StatisticsViewTab {
			   	text: child.getTitle(),
			      toggleGroup: tabGroup,
			      value: child
			   }
			}
			statisticPages.addEventListener( CollectionEvent.class, statisticPagesListener )
		}
	};

	var sortableBox:SortableBox;
	def tabGroup:ToggleTabGroup = new ToggleTabGroup();
	override var padding = Insets {top: 10, bottom: 4, right: 25, left: 25};
	override var nodeVPos = VPos.CENTER;
	override var spacing = 6;
	
	init {
	   var tabs;
	   
	   //TODO: (B) MERGE WITH A
 		if (statisticPages != null)
	   { 
			tabs = for ( child in statisticPages.getChildren() )
			{
				StatisticsViewTab {
			   	text: child.getTitle(),
			      toggleGroup: tabGroup,
			      value: child
			   }
			}
			statisticPages.addEventListener( CollectionEvent.class, statisticPagesListener )
		}
	    
		sortableBox = SortableBox {
			content: [tabs],
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
	
};

class StatisticPagesListener extends EventHandler {  
    override function handleEvent(e: EventObject) { 
		def event: CollectionEvent = e as CollectionEvent;
		if(event.getEvent() == CollectionEvent.Event.REMOVED){
			FxUtils.runInFxThread( function(): Void {
			   def sp:StatisticPage = event.getElement() as StatisticPage;
			   for(tab in sortableBox.content){
					if( (tab as StatisticsViewTab).value == sp){
							delete tab from sortableBox.content;
							break;
				   	}
				}
			});
		} else if(event.getEvent() == CollectionEvent.Event.ADDED){
			FxUtils.runInFxThread( function(): Void {
			   def sp:StatisticPage = event.getElement() as StatisticPage;
					insert 
						StatisticsViewTab {
					   	text: sp.getTitle(),
					      toggleGroup: tabGroup,
					      value: sp
		   		} into sortableBox.content;
			});
		}
	}
}

/* Makes sure that always one tab is selected, as opposed to the default ToggleBox behaviour */
class ToggleTabGroup extends ToggleGroup {
	
	var selectOne = false;
	var lastSelected:Toggle = null;
	
	override var selectedToggle on replace oldVal {
		if( selectedToggle == null ) {
			selectOne = true;
			FX.deferAction( requestSelect );
		} else {
		   lastSelected = selectedToggle;
		   onSelect( selectedToggle.value as StatisticPage );
			//selectedTab = selectedToggle.value as Tab;
		}
	}
	
	function requestSelect() {
		if( selectOne ) {
			selectOne = false;
			if( sizeof toggles > 0 ) {
				selectedToggle = lastSelected;
			} else {
				onSelect( null );
			}
		}
	}
}

/* Extend for the sole purpose of enabeling its own stylesheet */
class StatisticsViewTab extends ToggleButton {
    override var styleClass = "statistics-view-tab";
    override var blocksMouse = false;
    //override function action? no, lsiten to toggleGroup requests instead!
}

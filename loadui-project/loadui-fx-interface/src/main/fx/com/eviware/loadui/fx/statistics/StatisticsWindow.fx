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
package com.eviware.loadui.fx.statistics;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.util.Sequences;
import javafx.scene.image.Image;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Stack;
import javafx.scene.layout.VBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.Parent;
import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.statistics.topmenu.StatisticsMenu;
import com.eviware.loadui.fx.statistics.topmenu.ManageMenu;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.Overlay;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.toolbar.Toolbar;
import com.eviware.loadui.fx.statistics.toolbar.StatisticsToolbar;
import com.eviware.loadui.fx.statistics.chart.ChartPage;
import com.eviware.loadui.fx.statistics.manager.RecentResultsList;
import com.eviware.loadui.fx.statistics.manager.ArchivedResultsList;

import java.util.Map;
import java.util.HashMap;
import java.lang.Math;

public var instance:StatisticsWindow;

public function getInstance():StatisticsWindow {
  if (instance == null) {
    instance = StatisticsWindow {};
  }
  return instance;
}

public def STATISTICS_MANAGE = "statistics.manage";
public def STATISTICS_VIEW = "statistics.view";

public def VIEW_ATTRIBUTE = "gui.statistics.view";

public class StatisticsWindow {

	public var stage:Stage;
	public var project:ProjectItem on replace {
	    topMenu.project = project;
	}
	
	public var scene: Scene on replace {
		def appState = AppState {};
		
		appState.insertInto ( ManageMenu { width: bind scene.width }, STATISTICS_MANAGE );
		
		appState.insertInto ( RecentResultsList { layoutX: 50, layoutY: 100, width: bind Math.max( 315, scene.width - 100 ) }, STATISTICS_MANAGE );
		appState.insertInto ( ArchivedResultsList { layoutX: 50, layoutY: 347, width: bind Math.max( 315, scene.width - 100 ) }, STATISTICS_MANAGE );
		
		appState.insertInto( topMenu, STATISTICS_VIEW );
		appState.insertInto( stack, STATISTICS_VIEW );
		appState.insertInto( toolbar, STATISTICS_VIEW );
		
		appState.transitionTo( project.getAttribute( VIEW_ATTRIBUTE, STATISTICS_MANAGE ), AppState.FADE_WIPE );
		AppState.put( scene, appState, "STATISTICS" );
	}
	
	var closed:Boolean = true;
	def statisticsManager:StatisticsManager = BeanInjector.getBean( StatisticsManager.class );
	
	var pageMap: Map = new HashMap();
	
	function onTabRename(tab: ToggleButton): Void{
	    def page: StatisticPage = pageMap.get(tab) as StatisticPage;
	    if(page != null){
	        page.setTitle(tab.text);
	    }
	}
	
	def layoutRegion:Region = Region {
		managed: false,
		width: bind stack.width,
		height: bind stack.height,
		styleClass: "statistics-chartpage-container"
	}
	
	def stack:Stack = Stack {
  		layoutX: 135
  		layoutY: 145
  		width: bind if(scene.width >= 600) scene.width - 150 else 450
  		height: bind scene.height - 180
  		content: [ layoutRegion ]
  		//background: Color.web("#323232")
  	};
	
	def toolbar: StatisticsToolbar = StatisticsToolbar {
		layoutY: 140
		height: bind scene.height - 140
	}
	
	def topMenu:StatisticsMenu = StatisticsMenu {
		width: bind scene.width,
		project: project,
		onPageSelect: function( node ):Void {
			for( child in stack.content )
			{
				ReleasableUtils.release( child );
			}
			stack.content = [ layoutRegion, node ]
		}
	};
	
	public function show() {
    	if ( closed ) {
    		if ( scene == null ) {
	    		stage = Stage {
			    	title: "Statistics"
			    	icons: [
			    		Image { url:"{__ROOT__}images/png/icon_32x32.png" },
						Image { url:"{__ROOT__}images/png/icon_16x16.png" }
			    	]
			    	scene: scene = Scene {
						stylesheets: "file:style.css"
						width: 1024
						height: 768
						fill: Color.web("#373737")
					}
		    		onClose: function() {
		    		 	closed = true;
		    		 	close();
		//    				throw new com.eviware.loadui.util.hacks.PreventClosingStageException(); // this a hack to keep stage open
		  				}
		    		}
	    		} else {
		    		stage = Stage {
		    			height: 768
		    			width: 1024
				    	title: "Statistics"
				    	icons: [
				    		Image { url:"{__ROOT__}images/png/icon_32x32.png" },
							Image { url:"{__ROOT__}images/png/icon_16x16.png" }
				    	]
				    	scene: scene
			    		onClose: function() {
			    		 	closed = true;
			    		 	close();
			  				}
			    		}
	    		}
	    	}
    	closed = false;
    	stage.toFront();
	}
	
	public function close() {
		//tabs.clear();
		for( tb in pageMap.keySet() )
			((tb as ToggleButton).value as ChartPage).release();
		pageMap.clear();
		stage.close();
	}
			
}
	


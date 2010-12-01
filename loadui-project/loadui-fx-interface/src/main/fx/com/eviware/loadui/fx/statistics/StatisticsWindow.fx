/* 
 * Copyright 2010 eviware software ab
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
import javafx.scene.layout.VBox;

import com.eviware.loadui.fx.ui.tabs.TabPanel;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.util.BeanInjector;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.Overlay;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.toolbar.Toolbar;
import com.eviware.loadui.fx.statistics.toolbar.StatisticsToolbar;
import com.eviware.loadui.fx.statistics.chart.ChartPage;

public var instance:StatisticsWindow;

public function getInstance():StatisticsWindow {
  if (instance == null) {
    instance = StatisticsWindow {};
  }
  return instance;
}

public class StatisticsWindow {

	public var stage:Stage;
	public var project:ProjectItem;
	var closed:Boolean = true;
	var scene:Scene;
	def statisticsManager:StatisticsManager = BeanInjector.getBean( StatisticsManager.class );
	
	var tabs:TabPanel = TabPanel {
				        		x: 140
				        		y: 150
				        		width: bind scene.width - 150
				        		height: bind scene.height - 180
				        		background: Color.web("#323232")
				        	};
	
	init {
		tabs.addTab("Default", Text {content: "Tab default"});
		tabs.addTab("Default-1", Rectangle {
									height:100
									width: 200
									fill: Color.RED
									});
		tabs.addTab("Default-2", Rectangle {
									height:100
									width: 200
									fill: Color.GREEN
									});
		tabs.addTab("Default-3", Rectangle {
									height:100
									width: 200
									fill: Color.BLUE
									});
	}
	
	def toolbar: StatisticsToolbar = StatisticsToolbar {
		layoutY: 150
		height: bind scene.height - 100
	}
	
	public function show() {
		//Clean up trash
		for( sp in project.getStatisticPages().getChildren() ) (sp as StatisticPage).delete();
		
		def page = project.getStatisticPages().createPage( "General" );
		for( sh in statisticsManager.getStatisticHolders() )
			page.createChartGroup( "LINE", "{sh}" );
			
		tabs.addTab( "General", ChartPage { width: bind tabs.width - 60, height: bind tabs.height - 70, statisticPage: page } );
		
    	if ( closed ) {
    		var overlay = Group {};
    		stage = Stage {
    			height: 600
    			width: 600
		    	title: "Statistics"
		    	icons: [
		    		Image { url:"{__ROOT__}images/png/icon_32x32.png" },
					Image { url:"{__ROOT__}images/png/icon_16x16.png" }
		    	]
		    	scene: scene = Scene {
		    			stylesheets: "file:style.css"
				        content: [ Group {
				        	content: [
					        	Rectangle {
					        		x:1
					        		y:1
					        		width: bind scene.width -2
					        		height: 21
					        		fill: Color.web("#b1b1b1")
					        		stroke: Color.web("#000000")
					        		strokeWidth: 1
					        	}, Text {
					        		x: 20
					        		y: 15
					        		content: "Stats"
					        		font: Font { size: 10 }
					        	}, Rectangle {
					        		x: 1
					        		y: 22
					        		width: bind scene.width -2
					        		height: 90
					        		fill: Color.web("#989898")
					        		stroke: Color.web("#000000")
					        		strokeWidth: 1
					        	}, Text {
					        		x: 20
					        		y: 55
					        		content: "Stats: project {project.getLabel()}"
					        		font: Font { size: 30 }
					        		fill: Color.web("#ffffff")
					        	}, tabs, toolbar
					        ]
					     }, overlay ]
				        fill: Color.web("#373737")
	    			   }
	    		onClose: function() {
	    		 	closed = true;
	//    				throw new com.eviware.loadui.util.hacks.PreventClosingStageException(); // this a hack to keep stage open
	  				}
	    		}
	    		AppState.setOverlay( scene, Overlay { group: overlay } );
	    	}
    	closed = false;
	}
	
	public function close() {
		stage.close()
	}
		
}
	


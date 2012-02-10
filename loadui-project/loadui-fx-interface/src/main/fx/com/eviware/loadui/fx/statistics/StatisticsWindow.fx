/* 
 * Copyright 2011 SmartBear Software
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

import com.eviware.loadui.LoadUI;

import com.eviware.loadui.fx.statistics.chart.ChartDefaults;
import com.eviware.loadui.fx.statistics.topmenu.StatisticsMenu;
import com.eviware.loadui.fx.statistics.topmenu.ManageMenu;

import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.util.statistics.ExecutionListenerAdapter;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.ReleasableUtils;

import com.eviware.loadui.fx.WindowControllerImpl;
import javafx.scene.input.MouseEvent;

import com.eviware.loadui.fx.JavaFXActivator;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.Overlay;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.widgets.Trashcan;
import com.eviware.loadui.fx.statistics.toolbar.StatisticsToolbar;
import com.eviware.loadui.fx.statistics.chart.ChartPage;
import com.eviware.loadui.fx.statistics.manager.RecentResultsList;
import com.eviware.loadui.fx.statistics.manager.ArchivedResultsList;
import com.eviware.loadui.fx.ui.inspector.InspectorPanelControl;
import com.eviware.loadui.fx.ui.menu.StatisticsWindowButton;
import com.eviware.loadui.fx.ui.notification.NotificationArea;

import java.lang.Math;
import java.lang.System;
import java.io.File;

import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.statistics.StatisticsWindow" );

public-read var instance:StatisticsWindow;
public var currentExecution:Execution on replace oldExecution {
	if( currentExecution != null ) {
		if( execution == null or execution == lastExecution )
			execution = currentExecution;
		lastExecution = currentExecution;
	}
}
var lastExecution:Execution;
public var execution:Execution on replace {
	if( execution == comparedExecution )
		comparedExecution = null;
}
public var comparedExecution:Execution;

public-read var currentChartPage:ChartPage;

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
	
	var inspectors:InspectorPanelControl;
	
	def statisticPagesListener = new StatisticPagesListener();
	public-read def wc:WindowControllerImpl = WindowControllerImpl {
		windowTitleFilter: "Results"
	};
	public var stage:Stage;
	public var project:ProjectItem on replace {
		topMenu.project = project;
		execution = null;
		comparedExecution = null;
		scene = null;
		project.getStatisticPages().addEventListener( CollectionEvent.class, statisticPagesListener );
		checkForEmptyPages();
	}
	
	var appState:AppState;
	public var scene:Scene on replace {
		if( scene != null ) {
			topMenu = StatisticsMenu {
				width: bind scene.width,
				project: project,
				onPageSelect: function( page ):Void {
					for( child in stack.content ) {
						ReleasableUtils.release( child );
					}
					currentChartPage = page;
					stack.content = [ layoutRegion, page ]
				}
			};
			appState = AppState { windowName: "Statistics Workbench" };
			
			insert StatisticsWindowButton { layoutX: 10, layoutY: 2 } into appState.globalLayer.content;
			
			appState.insertInto ( RecentResultsList { layoutX: 50, layoutY: 100, layoutInfo: LayoutInfo { height: 222, width: bind Math.max( 487, scene.width - 100 ) } }, STATISTICS_MANAGE );
			appState.insertInto ( ArchivedResultsList { layoutX: 50, layoutY: 347, layoutInfo: LayoutInfo { height: 222, width: bind Math.max( 487, scene.width - 100 ) } }, STATISTICS_MANAGE );
			
			appState.insertInto( NotificationArea {
				id: "notification{STATISTICS_MANAGE}"
				layoutInfo: LayoutInfo { width: bind scene.width }
				layoutY: 50
			}, STATISTICS_MANAGE );
			
			appState.insertInto ( ManageMenu { width: bind scene.width }, STATISTICS_MANAGE );
			
			appState.insertInto( Trashcan { layoutX: bind scene.width - 110, layoutY: 90, layoutInfo: LayoutInfo { width: 100, height: 120 } }, STATISTICS_MANAGE );
			
			appState.insertInto( stack, STATISTICS_VIEW );
			appState.insertInto( toolbar, STATISTICS_VIEW );
			
			/*appState.insertInto( NotificationArea {
				id: "notification{STATISTICS_VIEW}"
				layoutInfo: LayoutInfo { width: bind scene.width }
				layoutY: 80
			}, STATISTICS_VIEW );*/
			appState.insertInto( topMenu, STATISTICS_VIEW );
			
			appState.insertInto( Trashcan { layoutX: bind scene.width - 110, layoutY: 90, layoutInfo: LayoutInfo { width: 100, height: 120 } }, STATISTICS_VIEW );
			
			appState.transitionTo( project.getAttribute( VIEW_ATTRIBUTE, STATISTICS_MANAGE ), AppState.FADE_WIPE );
			AppState.put( scene, appState, "STATISTICS" );
			
			// This region is just for fixing JavaFX's lack of working support for onMouseExited@Window and alwaysOnTop().
			insert Region{
				managed:false,
				width: bind scene.width - 5
				height: bind scene.height - 5
				onMouseExited: function(e:MouseEvent):Void {
					StatisticsWindow.instance.wc.setAlwaysOnTop( wc.isAlwaysOnTop );
				}
				onMouseEntered: function(e:MouseEvent):Void {
					StatisticsWindow.instance.wc.setAlwaysOnTop( false );
				}
			} into appState.globalLayer.content;
			
			//InspectorPanel
			log.debug( "Initializing StatInspectorPanel" );
			inspectors = InspectorPanelControl {
				id: "StatisticWorkbenchInspector"
				defaultInspector: "Monitors"
			}
			inspectors.collapse();
			insert inspectors into appState.globalLayer.content;
			log.debug( "Done initializing StatInspectorPanel: \{\}", inspectors );
		}
	}
	
	def state = bind appState.state on replace {
		project.setAttribute( VIEW_ATTRIBUTE, state );
	}
	
	var closed:Boolean = true;
	def statisticsManager:StatisticsManager = BeanInjector.getBean( StatisticsManager.class ) on replace {
		def executionManager = statisticsManager.getExecutionManager();
		executionManager.addExecutionListener( new CurrentExecutionListener() );
		execution = executionManager.getCurrentExecution();
	}
	
	def layoutRegion:Region = Region {
		managed: false,
		width: bind stack.width,
		height: bind stack.height,
		styleClass: "statistics-chartpage-container"
	}
	
	def toolbar: StatisticsToolbar = StatisticsToolbar {
		id: "StatisticsToolbar"
		layoutY: 140
		height: bind inspectors.topBar.layoutY + inspectors.topBar.translateY - 160
	}
	
	def stack:Stack = Stack {
		layoutX: bind if(toolbar.hidden) 63 else 135
		layoutY: 145
		width: bind if(toolbar.hidden) Math.max( 625, scene.width - 78 ) else Math.max( 625, scene.width - 150 )
		height: bind scene.height - 180
		content: layoutRegion
	};
	
	var topMenu:StatisticsMenu;
	
	public function show() {
		def name = System.getProperty(LoadUI.NAME);
		if ( closed ) {
			wc.listenForNewWindow();
			if ( scene == null ) {
				stage = Stage {
					//title: "{name} {LoadUI.VERSION} - Statistics Workbench"
					icons: [
						Image { url: new File( "res/icon_32x32.png" ).toURI().toString() },
						Image { url: new File( "res/icon_16x16.png" ).toURI().toString() },
					]
					scene: scene = Scene {
						stylesheets: bind JavaFXActivator.stylesheets
						width: 1085
						height: 720
						fill: Color.web("#373737")
					}
					onClose: function() {
						closed = true;
						close();
					}
				}
			} else {
				stage = Stage {
					width: 1280.0
					height: 768.0
					//title: "{name} {LoadUI.VERSION} - Statistics Workbench"
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
			wc.stage = stage;
			appState.updateTitleBar();
		}
		
		closed = false;
		wc.bringToFront();
	}
	
	public function close() {
		//tabs.clear();
		stage.close();
	}
	
	function checkForEmptyPages() {
		def statisticPages = project.getStatisticPages();
		if( statisticPages.getChildCount() == 0 )
			ChartDefaults.createStatisticsTab( statisticPages, "General", /*project*/ null );
	}
}
	
class CurrentExecutionListener extends ExecutionListenerAdapter {
	override function executionStarted( oldState ) {
		runInFxThread( function():Void {
			currentExecution = statisticsManager.getExecutionManager().getCurrentExecution();
		} );
	}
	
	override function executionStopped( oldState ) {
		runInFxThread( function():Void {
			currentExecution = null;
		} );
	}
}

class StatisticPagesListener extends WeakEventHandler {
    override function handleEvent( e ) { 
		def event: CollectionEvent = e as CollectionEvent;
		if( event.getEvent() == CollectionEvent.Event.REMOVED ) {
			runInFxThread( function():Void {
				checkForEmptyPages();
			} );
		}
	}
}
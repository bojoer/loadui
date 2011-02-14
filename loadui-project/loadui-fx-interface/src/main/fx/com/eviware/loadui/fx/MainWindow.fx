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
package com.eviware.loadui.fx;

import com.eviware.loadui.launcher.api.SplashController;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.inspector.InspectorPanelControl;
import com.eviware.loadui.fx.ui.menu.MainButton;
import com.eviware.loadui.fx.ui.menu.ProjectMenu;
import com.eviware.loadui.fx.ui.menu.SoapUIButton;
import com.eviware.loadui.fx.ui.menu.TestCaseMenu;
import com.eviware.loadui.fx.ui.menu.WorkspaceMenu;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.toolbar.Toolbar;
import com.eviware.loadui.fx.ui.toolbar.GroupOrder;
import com.eviware.loadui.fx.ui.toolbar.ItemOrder;
import com.eviware.loadui.fx.widgets.AgentList;
import com.eviware.loadui.fx.widgets.FeedDisplay;
import com.eviware.loadui.fx.widgets.ProjectList;
import com.eviware.loadui.fx.widgets.canvas.Canvas;
import com.eviware.loadui.fx.widgets.canvas.NavigationPanel;
import com.eviware.loadui.fx.widgets.canvas.ProjectCanvas;
import com.eviware.loadui.fx.widgets.toolbar.NoteToolbarItem;
import com.eviware.loadui.fx.widgets.toolbar.AgentToolbarItem;
import com.eviware.loadui.fx.widgets.toolbar.ProjectToolbarItem;
import com.eviware.loadui.fx.widgets.toolbar.TestCaseToolbarItem;
import com.eviware.loadui.fx.wizards.GettingStartedWizard;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.widgets.TutorialList;

import java.lang.Thread;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Math;
import org.slf4j.LoggerFactory;

import java.io.File;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.MainWindow" );

public-read var instance:MainWindow;

public def WORKSPACE_FRONT = "workspace.front";
public def PROJECT_FRONT = "project.front";
public def PROJECT_BACK = "project.back";
public def TESTCASE_FRONT = "testcase.front";

/**
 * Sets up the main window of loadUI. Initialized from Spring and may have dependencies
 * on several objects, both JavaFX based and other. For the Spring handled dependencies,
 * setters must be defined, and must explicitly return Void.
 * 
 * @author dain.nilsson
 */
public class MainWindow {
	var scene:Scene;
	public function setScene( scene:Scene ):Void { this.scene = scene }
	
	var workspaceProvider:WorkspaceProvider;
	public-read var workspace:WorkspaceItem;
	public function setWorkspaceProvider( workspaceProvider:WorkspaceProvider ):Void { this.workspaceProvider = workspaceProvider }
	
	var inspectors:InspectorPanelControl;
	public function getInspectorPanel() { inspectors }
	
	var projectList:ProjectList;
	
	var toolbar:Toolbar;
	public function getToolbar() { toolbar }
	
	var projectToolbar:Toolbar = Toolbar{
		layoutY: 110
		height: bind scene.height - inspectors.height - 100
	}
	
	public function getProjectToolbar() { 
		projectToolbar 
	}
	
	var testcaseToolbar:Toolbar = Toolbar{
		layoutY: 130
		height: bind scene.height - inspectors.height - 100
	}
	
	public function getTestcaseToolbar() { testcaseToolbar }
	
	public-read var projectCanvas:ProjectCanvas;
	public-read var testcaseCanvas:Canvas;
	public-read def canvas:Canvas = bind if(AppState.byScene( scene ).state == TESTCASE_FRONT ) testcaseCanvas else projectCanvas;
	public-read var navigator:NavigationPanel;
	
	/**
	 * Called once the bean is fully initialized (all the properties defined in the Spring
	 * configuration set through the setters). It sets up the main window.
	 */
	function initialize():Void {
	
		//change classloader of JavaFX thread to this thread's (Spring's) classloader
		//FxUtils.setJavaFXThreadClassLoader(Thread.currentThread().getContextClassLoader());
		
		//scene.stylesheets = "{__ROOT__}themes/default/style.css";
		//scene.stylesheets = "{FX.getProperty('javafx.user.home')}{File.separator}.loadui{File.separator}style.css";
	
		instance = this;
		def appState = AppState {};
		
		//Set the layer to place items being dragged into.
		
		//Load workspace
		workspace = if( workspaceProvider.isWorkspaceLoaded() )
			workspaceProvider.getWorkspace()
		else //TODO: Use a global configuration file to select workspace.
			workspaceProvider.loadDefaultWorkspace();
		
		//if workspace file does not exists it must be after clean install, so add a sample project
		if( not workspace.getWorkspaceFile().exists() ) {
		    def samplesDir = new File("samples");
            def sampleFile = new File(samplesDir,"getting-started-project.xml");
             if( sampleFile.exists() ) {
                 workspace.importProject(sampleFile, true);
			 }
		}
		
		//InspectorPanel
		log.debug( "Initializing InspectorPanel" );
		inspectors = InspectorPanelControl {
			height: bind inspectors.prefHeight
			width: bind scene.width
			layoutY: bind scene.height - inspectors.height
			layoutX: 0
			maxHeight: bind scene.height - 100 as Integer
		}
		inspectors.collapse();
		insert inspectors into appState.globalLayer.content;
		log.debug( "Done initializing InspectorPanel: \{\}", inspectors );
		
		appState.insertInto( projectList = ProjectList { workspace: workspace, layoutX: 137, layoutY: 90, layoutInfo: LayoutInfo { width: bind Math.max( scene.width - 529, 315 ), height: 222 } }, WORKSPACE_FRONT );
		appState.insertInto( AgentList { workspace: workspace, layoutX: 137, layoutY: 337, layoutInfo: LayoutInfo { width: bind Math.max( scene.width - 529, 315 ), height: 260 } }, WORKSPACE_FRONT );
		appState.insertInto( TutorialList { workspace: workspace, layoutX: 137, layoutY: 622, layoutInfo: LayoutInfo { width: bind Math.max( scene.width - 529, 315 ), height: 212 } }, WORKSPACE_FRONT );
		
		//Toolbar

		def toolbar:Toolbar = Toolbar {
			layoutY: 90
			height: bind scene.height - inspectors.height - 100
		}
		
		toolbar.addItem( ProjectToolbarItem { workspace: workspace } );
		toolbar.addItem( AgentToolbarItem {} );
		
		def feed:FeedDisplay = FeedDisplay {
			layoutX: bind scene.width - 356
			layoutY: 90
			//width: 300
			height: bind scene.height - 135
		}

		appState.insertInto( toolbar, WORKSPACE_FRONT );
		appState.insertInto( feed, WORKSPACE_FRONT );
		
		//Set up the Project view
		appState.insertInto( ImageView { image: Image { url: "{__ROOT__}images/grid.png" }, clip: Rectangle{ width: bind scene.width, height: bind scene.height } }, PROJECT_FRONT );
		appState.insertInto( projectCanvas = ProjectCanvas { width: bind scene.width, height: bind scene.height }, PROJECT_FRONT );
		appState.insertInto( navigator = NavigationPanel { canvas: projectCanvas, width: 240, height: 195, layoutX: bind scene.width - ( navigator.width + 20 ), layoutY: bind scene.height - ( inspectors.height + navigator.height ) }, PROJECT_FRONT );
		//def projectToolbar:Toolbar = Toolbar {
						//	layoutY: 90
						//	height: bind scene.height - inspectors.height - 100
						//};
		
		
						projectToolbar.addItem( TestCaseToolbarItem {} );
						projectToolbar.addItem( NoteToolbarItem {} );
						
		appState.insertInto( projectToolbar, PROJECT_FRONT );
				
		//Set up the back Project view
	//	appState.insertInto( ImageView { image: Image { url: "{__ROOT__}images/team.jpg"
	//	                                                width: 800
	//	                                                height: 600
	//	                                                preserveRatio: false } }, PROJECT_BACK );
		appState.insertInto( ImageView { image: Image { url: "{__ROOT__}images/grid.png" }, clip: Rectangle{ width: bind scene.width, height: bind scene.height } }, TESTCASE_FRONT );
	    appState.insertInto( testcaseCanvas = Canvas { width: bind scene.width, height: bind scene.height }, TESTCASE_FRONT );	
		appState.insertInto( navigator = NavigationPanel { canvas: testcaseCanvas, width: 240, height: 195, layoutX: bind scene.width - ( navigator.width + 20 ), layoutY: bind scene.height - ( inspectors.height + navigator.height ) }, TESTCASE_FRONT );
		testcaseToolbar.addItem( NoteToolbarItem {} );
		appState.insertInto( testcaseToolbar, TESTCASE_FRONT );
		
		insert MainButton { layoutX: 10, layoutY: 7 } into appState.globalLayer.content;
		insert SoapUIButton { layoutX: 2, layoutY: 2 } into appState.globalLayer.content;
		appState.insertInto( WorkspaceMenu { width: bind scene.width, workspace: workspace }, WORKSPACE_FRONT );
		appState.insertInto( ProjectMenu { width: bind scene.width, project: bind projectCanvas.canvasItem as ProjectItem }, PROJECT_FRONT );
		appState.insertInto( TestCaseMenu { width: bind scene.width, testCase: bind testcaseCanvas.canvasItem as SceneItem }, TESTCASE_FRONT );
		
		FX.deferAction( function():Void {
			scene.fill = Color.web("#333333");
			scene.stage.visible = true;
			appState.transitionTo( WORKSPACE_FRONT, AppState.FADE_WIPE );
			AppState.put( scene, appState, "MAIN" );
			SplashController.closeSplash();
			projectList.checkExistingProjects();
		});
		
		if( workspace.getAttribute( GettingStartedWizard.SHOW_GETTING_STARTED, "true" ) == "true" )
			GettingStartedWizard {
				x: scene.width/3
			    y: scene.height/4
			}.show();
	}
	
	/**
	 * Called when the JavaFX UI bundle is stopped. Generally this will be once the window 
	 * has been closed, JavaFX Nodes may already be destroyed.
	 */
	public function destroy():Void {
		log.info( "Shutting down..." );
		for( projectRef in workspace.getProjectRefs() )
			if( projectRef.isEnabled() )
				projectRef.setEnabled( false );
		workspace.save();
		workspace.release();
	}
}

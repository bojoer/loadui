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
package com.eviware.loadui.fx.widgets;

import com.eviware.loadui.api.ui.inspector.Inspector;
import javafx.scene.layout.Resizable;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Resizable;
import javafx.scene.CustomNode;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.Group;
import javafx.scene.CustomNode;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.control.Button;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.ui.pagelist.PagelistControl;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.DroppableNode;
import com.eviware.loadui.fx.dialogs.CreateNewRunnerDialog;
import com.eviware.loadui.fx.widgets.toolbar.RunnerToolbarItem;
import com.eviware.loadui.fx.widgets.TestCaseIconListener;
import com.eviware.loadui.fx.widgets.canvas.ProjectCanvas;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.Assignment;
import com.eviware.loadui.fx.ui.popup.SeparatorMenuItem;

import com.eviware.loadui.fx.ui.popup.ActionMenuItem;
import com.eviware.loadui.fx.ui.popup.PopupMenu;

import com.eviware.loadui.fx.runners.discovery.RunnerDiscoverer;
import com.eviware.loadui.fx.runners.discovery.RunnerDiscovererDialog;

import java.util.EventObject;
import java.util.Comparator;
import javafx.util.Sequences;
import java.lang.RuntimeException;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.RunnerItem;
import org.slf4j.LoggerFactory;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.RunnerInspector" );

/**
 * Static factory method easily invocable from Java Code.
 */
public function createInstance(name: String) {
	def runnerInspector = RunnerInspector {name: name};
	runnerInspector;
}

/**
 * A RunnerInspector.
 *
 * @author predrag
 */
public class RunnerInspector extends Inspector {
	
	public-init var name: String;
	
	override function onShow(): Void {
	}
	
	override function onHide(): Void {
	}
	
	override function getPanel(): Object {
		RunnerInspectorPanel {}
	}

	override function getName(): String {
		name
	}
	
	override function getHelpUrl(): String {
		"http://www.loadui.org/interface/project-view.html";
	}
	
	override function toString(): String {
		getName()
	}
	
}

public class RunnerInspectorPanel extends CustomNode, TestCaseIconListener, Resizable, EventHandler {

	//refernce to a workspace
	def workspace: WorkspaceItem = bind MainWindow.instance.workspace on replace oldVal {
		oldVal.removeEventListener( BaseEvent.class, this );
		workspace.addEventListener( BaseEvent.class, this );
	}
	
	def mainWindowInstance = bind MainWindow.instance;
	def testCaseCanvas = bind mainWindowInstance.testcaseCanvas;
	def currentTestCase = bind testCaseCanvas.canvasItem on replace {
		if(currentTestCase == null){
			//do nothing on close
			//deselectTestCaseIcons();
		}
		else{
			selectTestCaseIcon(currentTestCase as SceneItem, true);
		}
	}

	//determines if test cases will be executed localy or on runners
	var onRunners: Boolean = not workspace.isLocalMode();
	
	//pagelist that holds runners
	var pagelist: PagelistControl;
	
	//component paddings
	def paddingLeft: Number = 12;
	def paddingTop: Number = 0;
	def paddingBottom: Number = 16;
	def paddingRight: Number = 35;
	
	//width of panel that holds ghost runner
	def leftPanelWidth: Number = 210;
	//components in left panel are moved to right from the center by this offset
	def leftPanelContentOffset: Number = 40;
	
	//ghost runner instance
	var ghostRunner: RunnerInspectorNode;
	
	//fill of inactive panel (can be local or onRunners)
	def inactivePanelFill = LinearGradient {
		endX: 0
		stops: [
			Stop { offset: 0, color: Color.web("#5c5c5c") }
			Stop { offset: 0.5, color: Color.web("#6f6f6f") }
			Stop { offset: 1, color: Color.web("#666666") }
		]
	}
	
	//fill of active panel (can be local or onRunners)
	def activePanelFill = LinearGradient {
		endX: 0
		stops: [
			Stop { offset: 0, color: Color.web("#3b3b3b") }
			Stop { offset: 0.5, color: Color.web("#5b5b5b") }
			Stop { offset: 1, color: Color.web("#4d4d4d") }
		]
	}
	
	override function handleEvent( e:EventObject ) { 
		if(e.getSource() == MainWindow.instance.workspace){
			if( e instanceof PropertyEvent ) {
				def event = e as PropertyEvent;
				if( WorkspaceItem.LOCAL_MODE_PROPERTY == event.getProperty().getKey() ) {
					runInFxThread( function():Void {
						onRunners = not ( event.getProperty().getValue() as Boolean );
					} );
				}
			} else if( e instanceof CollectionEvent ) {
				def event = e as CollectionEvent;
				if( WorkspaceItem.PROJECTS == event.getKey() ) {
					if( event.getEvent() == CollectionEvent.Event.ADDED ) {
						runInFxThread( function():Void {
							(event.getElement() as ProjectItem).addEventListener( BaseEvent.class, this );
							populateTestCases();
						});
					} else {
						runInFxThread( function():Void {
							(event.getElement() as ProjectItem).removeEventListener( BaseEvent.class, this );
							clearTestCases();
						});
					}
				}
				else if( WorkspaceItem.RUNNERS == event.getKey() ) {
					if( event.getEvent() == CollectionEvent.Event.ADDED ) {
						runInFxThread( function():Void {
							addRunner(event.getElement() as RunnerItem);
						});
					} else {
						runInFxThread( function():Void {
							clearTestCases(event.getElement() as RunnerItem);
							removeRunner(event.getElement() as RunnerItem);
						});
					}
				}
			}
		}
		else if(e.getSource() == MainWindow.instance.projectCanvas.canvasItem){
			if( e instanceof CollectionEvent ) {
				def event = e as CollectionEvent;
				if( ProjectItem.SCENES == event.getKey() ) {
					if( event.getEvent() == CollectionEvent.Event.ADDED ) {
						runInFxThread( function():Void {
							ghostRunner.addTestCase(TestCaseIcon{
									stateListeners: [this]
									sceneItem: (event.getElement() as SceneItem)
								}
							);
						});
					} else {
						runInFxThread( function():Void {
							undeployTestCase(event.getElement() as SceneItem);
						});
					}
				}
				else if( ProjectItem.ASSIGNMENTS == event.getKey() ) {
					def a: Assignment = event.getElement() as Assignment;
					if( event.getEvent() == CollectionEvent.Event.ADDED ) {
						runInFxThread( function():Void {
							deployTestCase(a.getScene(), a.getRunner())
						});
					} else {
						runInFxThread( function():Void {
							undeployTestCase(a.getScene(), a.getRunner())
						});
					}
				}
			}
		}
	}

	override function getPrefWidth(width: Float) {
		paddingLeft + leftPanelWidth + pagelist.getPrefWidth(width) + paddingRight;
	}
	
	override function getPrefHeight(height: Float) {
		paddingTop + pagelist.getPrefHeight(height) + paddingBottom;
	}

	override function selectionChanged(tc: TestCaseIcon): Void {
		for(rin in pagelist.content){
			(rin as RunnerInspectorNode).selectTestCaseIcon(tc);
		}
		ghostRunner.selectTestCaseIcon(tc);
	}
	
	override function testCaseRemoved(tc: TestCaseIcon): Void {}
	
	var localButton: ToggleActionButton;
	var onRunnersButton: ToggleActionButton;
	
	override function create() {
		var text: Text;
		var buttonGroup: ToggleActionGroup = new ToggleActionGroup;
		
		def panelHeight = 325;
		
		def popup = PopupMenu {};
		popup.items = [
			ActionMenuItem {
				text: "Detect Agents"
				action: function() {
					RunnerDiscovererDialog{}.show();
				}
			}
			SeparatorMenuItem{}
			ActionMenuItem {
				text: "New Agent"
				action: function() {
					CreateNewRunnerDialog{ workspace: workspace };
				}
			}
		];
							
		var panel: Group = Group {
			layoutX: 0
			layoutY: 0
			content: [
				pagelist = PagelistControl {
					layoutInfo: LayoutInfo {
						hfill: true vfill: true
				    }
					layoutY: 0
					layoutX: leftPanelWidth
					height: bind panelHeight
					width: bind width - paddingLeft - paddingRight - leftPanelWidth
					itemSpacing: 18
					fill: bind if(onRunners) activePanelFill else inactivePanelFill
					fillOpacity: 1.0
					leftArrowActive: "{__ROOT__}images/leftarrow_active_nontransparent.fxz";
					leftArrowInactive: "{__ROOT__}images/leftarrow_inactive_nontransparent.fxz";
					rightArrowActive: "{__ROOT__}images/rightarrow_active_nontransparent.fxz";
					rightArrowInactive: "{__ROOT__}images/rightarrow_inactive_nontransparent.fxz";
					onMousePressed: function(e: MouseEvent){
						if(e.popupTrigger){
							popup.layoutX = e.sceneX;
							popup.layoutY = e.sceneY;
							popup.open();
						}
					}
					onMouseReleased: function(e: MouseEvent){
						if(e.popupTrigger){
							popup.layoutX = e.sceneX;
							popup.layoutY = e.sceneY;
							popup.open();
						}
					}
				}
				Rectangle {
					layoutY: 0
					layoutX: leftPanelWidth
				    width: 1  
				    height: bind panelHeight
				    fill: Color.web("#555555")
				}
				Rectangle { //panel that has the same color as right panel, it is 15px wide as much as arch of right panel 
					layoutX: leftPanelWidth - 15
				    width: 30  
				    height: bind panelHeight
				    fill: bind if(onRunners) activePanelFill else inactivePanelFill
				}
				Rectangle {
					layoutX: leftPanelWidth
					layoutY: 1
				    width: 1
				    height: bind panelHeight - 2
				    fill: Color.web("#797979")
				}
				Rectangle { //left panel, first part with rounded corners
					layoutX: 0
					layoutY: 0
				    width: 30  
				    height: bind panelHeight
				    arcWidth: 15  
				    arcHeight: 15
				    fill: bind if(onRunners) inactivePanelFill else activePanelFill
				}
				Rectangle { //left panel, main part
					layoutX: 15
					layoutY: 0
				    width: bind leftPanelWidth - 15  
				    height: bind panelHeight
				    fill: bind if(onRunners) inactivePanelFill else activePanelFill
				}
				Line { //bright line under the runner component on left panel 
					startX: leftPanelContentOffset - 10
					endX: bind leftPanelWidth - 20
					startY: bind panelHeight - 50
					endY: bind panelHeight - 50
					stroke: Color.web("#808080")
				}
				text = Text { //text for the local component
					layoutX: leftPanelContentOffset
					layoutY: bind panelHeight - 50 + text.layoutBounds.height
					fill: Color.web("#b2b2b2")
					content: "Local (Ghost) agent"
					font: Font.font("Arial", 10)
				}
				Text { //up text on the left panel
					layoutX: leftPanelContentOffset
					layoutY: 27
					fill: Color.web("#212121")
					content: "Test case distribution"
					font: Font.font("Arial", 10)
				}
				localButton = ToggleActionButton {
			        text: "Local"
			        toggleGroup: buttonGroup
			        layoutX: bind leftPanelWidth - 15 - localButton.layoutBounds.width
					layoutY: 15
					selected: not onRunners
			        action: function(): Void {
						workspace.setLocalMode( true );
					}
			    }
				onRunnersButton	= ToggleActionButton {
			        text: "On agents"
			        toggleGroup: buttonGroup
			        layoutX: bind leftPanelWidth + 15
					layoutY: 15
					selected: onRunners
			        action: function(): Void {
						workspace.setLocalMode( false );
					}
			    }
			    ghostRunner = RunnerInspectorNode { 
			    	layoutX: leftPanelContentOffset
					layoutY: bind panelHeight - 50 - ghostRunner.layoutBounds.height
			    	ghostRunner: true
			    }
			]
		}
		VBox {
			padding: Insets { top: paddingTop right: paddingRight bottom: paddingBottom left: paddingLeft}
			layoutInfo: LayoutInfo {
				height: bind if(height > panelHeight + paddingTop + paddingBottom) height else panelHeight + paddingTop + paddingBottom
			}
			spacing: 0
			nodeHPos: HPos.LEFT
			vpos: VPos.BOTTOM
			content: [ panel ]
		}
	}

	/** Adds runner node to list */
	function addRunner( runner: RunnerItem ):Void {
		pagelist.content = Sequences.sort( [ pagelist.content, RunnerInspectorNode { runner: runner } ], COMPARE_BY_TOSTRING ) as Node[];
	}
	
	/** Removes runner from the list */
	function removeRunner( runner:RunnerItem ):Void {
		for( node in pagelist.content[p|p instanceof RunnerNode] )
			if( (node as RunnerNode).runner == runner )
				delete node from pagelist.content;
		for( node in pagelist.content[p|p instanceof RunnerInspectorNode] )
			if( (node as RunnerInspectorNode).runner == runner )
				delete node from pagelist.content;
	}
	
	/** Assign testcase to a specified runner */
	function deployTestCase(scene: SceneItem, runner: RunnerItem): Void {
		for(rin in pagelist.content){
			if((rin as RunnerInspectorNode).runner == runner){
				(rin as RunnerInspectorNode).addTestCase(TestCaseIcon{
						stateListeners: [this]
						sceneItem: scene
					}
				);
				return;
			}
		}
	}
	
	/** Assign testcase to all runners */
	function deployTestCase(scene: SceneItem): Void {
		for(rin in pagelist.content){
			(rin as RunnerInspectorNode).addTestCase(TestCaseIcon{
					stateListeners: [this]
					sceneItem: scene
				}
			);
		}
	}
	
	/** Unassign testcas from specified runner */
	function undeployTestCase(scene: SceneItem, runner: RunnerItem): Void {
		for(rin in pagelist.content){
			if((rin as RunnerInspectorNode).runner == runner){
				(rin as RunnerInspectorNode).undeployTestCase(scene);
				return;
			}
		}
	}
	
	/** Unassign testcas from all runners it is assigned to */
	function undeployTestCase(sceneItem: SceneItem): Void{
		ghostRunner.undeployTestCase(sceneItem);
		for(rin in pagelist.content){
			(rin as RunnerInspectorNode).undeployTestCase(sceneItem);
		}
	}
	
	/** Re-populates test cases to ghost runner and all other runners they are assigned to */
	function populateTestCases(): Void{
		clearTestCases();
		var projectItem: ProjectItem = MainWindow.instance.projectCanvas.canvasItem as ProjectItem;
		if(projectItem != null){
			var scenesCount = projectItem.getScenes().size();
			for(s in projectItem.getScenes()){
				var tc: TestCaseIcon = TestCaseIcon {
						stateListeners: [this]
						sceneItem: s as SceneItem
				} 
				ghostRunner.addTestCase(tc);
				
				var rin: RunnerInspectorNode;
				var runners = projectItem.getRunnersAssignedTo(s as SceneItem);
				if(runners != null){
					for(r in runners){
						rin = findRunnerNode(r as RunnerItem);
						if(rin != null){
							rin.addTestCase(tc.copy());
						}
					}
				}
			}
		}
	}
	
	/** Find runner node by it's runner */
	function findRunnerNode(runner: RunnerItem): RunnerInspectorNode {
		for(rin in pagelist.content){
			if((rin as RunnerInspectorNode).runner == runner){
				return rin as RunnerInspectorNode;
			}
		}
		return null as RunnerInspectorNode;
	}
	
	/** Clear test cases from all runner nodes wiothout unassigning them */
	function clearTestCases(): Void{
		ghostRunner.clearTestCases(false);
		for(rin in pagelist.content){
			(rin as RunnerInspectorNode).clearTestCases(false);
		}
	}
	
	/** Clear test cases from the runner node specified by it's runner with unassigning them */
	function clearTestCases(runner: RunnerItem): Void{
		for(rin in pagelist.content){
			if((rin as RunnerInspectorNode).runner == runner){
				(rin as RunnerInspectorNode).clearTestCases(true);
				return;
			}
		}
	}
	
	/** Deselects selected testcases (if any) and selects or deselects one given by the argument, depending 
	 *  on value of the other argument. 
	 */
	function selectTestCaseIcon(sceneItem: SceneItem, selected: Boolean): Void {
		ghostRunner.selectTestCaseIcon(sceneItem, selected);
		for(rin in pagelist.content){
			(rin as RunnerInspectorNode).selectTestCaseIcon(sceneItem, selected);
		}
	}
	
	/** Deselects all testcase icons */
	function deselectTestCaseIcons(): Void {
		ghostRunner.deselectTestCaseIcons();
		for(rin in pagelist.content){
			(rin as RunnerInspectorNode).deselectTestCaseIcons();
		}
	}
	
	postinit {
		//add runners on workspace replace
		var tmp = MainWindow.instance.workspace on replace {
			if(tmp != null){
				for(runner in tmp.getRunners()){
					addRunner(runner);
				}
			}
		};

		populateTestCases();
	}
	
}

public class ToggleActionGroup {
	
	var buttonList: ToggleActionButton[] = [];
	
	public function add(button: ToggleActionButton){
		insert button into buttonList;
	}
	
	public function remove(button: ToggleActionButton){
		delete button from buttonList;
	}
	
	public function unselect(){
		for(button in buttonList){
			button.selected = false;
		}
	}
}

public class ToggleActionButton extends CustomNode {
    
    public var text: String;
    public var action: function();
    public var width: Integer = -1;
    
    public var toggleGroup: ToggleActionGroup on replace oldToggleGroup {
    	if(oldToggleGroup != null){
    		oldToggleGroup.remove(this);
    	}
    	if(toggleGroup != null){
    		toggleGroup.add(this);
    	}
    }
    
    public var selected: Boolean = false;
    
    var left: ImageView;
    var middle: ImageView;
    var right: ImageView;
    var content: Group;
    var label: Text;
    
    var contentNormal = Group {
        content: [
            left = ImageView {
                layoutX: 0
                layoutY: 0
                image: Image {
                    url: "{__ROOT__}images/png/inspector-runners-left-default.png"
                }
            },
            middle = ImageView {
                layoutY:0
                layoutX: left.layoutBounds.width
                scaleX: bind if(width > -1) width - 12 else label.layoutBounds.width + 14
                translateX: bind if(width > -1) (width - 12) / 2 else (label.layoutBounds.width + 14) / 2 - .5
                image: Image {
                    url: "{__ROOT__}images/png/inspector-runners-mid-default.png"
                }
            },
            right = ImageView {
                layoutY: 0
                layoutX: bind middle.boundsInParent.width + left.layoutBounds.width - 1
                image: Image {
                    url: "{__ROOT__}images/png/inspector-runners-right-default.png"
                }
            },
			label = Text {
		        layoutX: bind if(width > -1) (width - label.layoutBounds.width) / 2 + 1 else middle.boundsInParent.minX + 8
		        layoutY: 12
		        content: text
		        font: Font.font("Arial", 9)
		        fill: Color.web("#333333")
		    }
        ]
    }
                
    var contentActive = Group {
        content: [
            left = ImageView {
                layoutX: 0
                layoutY: 0
                image: Image {
                    url: "{__ROOT__}images/png/inspector-runners-left-active.png"
                }
            },
            middle = ImageView {
                layoutY:0
                layoutX: left.layoutBounds.width
                scaleX: bind if(width > -1) width - 12 else label.layoutBounds.width + 14
                translateX: bind if(width > -1) (width - 12) / 2 else (label.layoutBounds.width + 14) / 2 - .5
                image: Image {
                    url: "{__ROOT__}images/png/inspector-runners-mid-active.png"
                }
            },
            right = ImageView {
                layoutY:0
                layoutX: bind middle.boundsInParent.width + left.layoutBounds.width - 1
                image: Image {
                    url: "{__ROOT__}images/png/inspector-runners-right-active.png"
                }
            },
            label = Text {
		        layoutX: bind if(width > -1) (width - label.layoutBounds.width) / 2 + 1 else middle.boundsInParent.minX + 8
		        layoutY: 12
		        content: text
		        font: Font.font("Arial", 9)
		        fill: Color.web("#d9d9d9")
		    }
        ]
    }
            
    override public function create():Node {
        Group {
            content: bind if (selected) contentActive else contentNormal
        }
    }
    
    var oldSelected: Boolean = selected;
    
    override public var onMousePressed = function(e) {
    	oldSelected = selected;
        selected = true;
    }
    
    override public var onMouseReleased = function(e) {
        if( hover ) {
        	requestFocus();
            action();
            if(toggleGroup != null){
    			toggleGroup.unselect();
    		}
            selected = true;
        } 
        else {
            selected = oldSelected;
        }
    }
}


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
import javafx.scene.control.Separator;
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

import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.PopupMenu;

import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.ui.pagelist.PagelistControl;
import com.eviware.loadui.fx.ui.dnd.DraggableFrame;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.dnd.DroppableNode;
import com.eviware.loadui.fx.dialogs.CreateNewAgentDialog;
import com.eviware.loadui.fx.widgets.toolbar.AgentToolbarItem;
import com.eviware.loadui.fx.widgets.TestCaseIconListener;
import com.eviware.loadui.fx.widgets.canvas.ProjectCanvas;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.Assignment;

import com.eviware.loadui.fx.agents.discovery.AgentDiscoverer;
import com.eviware.loadui.fx.agents.discovery.AgentDiscovererDialog;

import java.util.EventObject;
import java.util.Comparator;
import javafx.util.Sequences;
import java.lang.RuntimeException;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.AgentItem;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.fx.ui.dnd.DroppableNode;
import com.eviware.loadui.fx.ui.dnd.Droppable;
import com.eviware.loadui.fx.ui.dnd.Draggable;
import com.eviware.loadui.fx.ui.node.BaseNode;

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.widgets.AgentInspector" );

/**
 * Static factory method easily invocable from Java Code.
 */
public function createInstance(name: String) {
	def AgentInspector = AgentInspector {name: name};
	AgentInspector;
}

/**
 * A AgentInspector.
 *
 * @author predrag
 */
public class AgentInspector extends Inspector {
	
	public-init var name: String;
	
	def panel = AgentInspectorPanel {};
	
	override function onShow(): Void {
	}
	
	override function onHide(): Void {
	}
	
	override function getPanel(): Object {
		panel
	}

	override function getName(): String {
		name
	}
	
	override function getHelpUrl(): String {
		"http://www.loadui.org/interface/workspace-view.html";
	}
	
	override function toString(): String {
		getName()
	}
	
}

public class AgentInspectorPanel extends BaseNode, TestCaseIconListener, Resizable, EventHandler, Droppable {

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

	//determines if test cases will be executed localy or on agents
	var onAgents: Boolean = not workspace.isLocalMode();
	
	//pagelist that holds agents
	var pagelist: PagelistControl;
	
	//component paddings
	def paddingLeft: Number = 12;
	def paddingTop: Number = 0;
	def paddingBottom: Number = 16;
	def paddingRight: Number = 35;
	
	//width of panel that holds ghost agent
	def leftPanelWidth: Number = 210;
	//components in left panel are moved to right from the center by this offset
	def leftPanelContentOffset: Number = 40;
	
	//ghost agent instance
	var ghostAgent: AgentInspectorNode;
	
	//fill of inactive panel (can be local or onAgents)
	def inactivePanelFill = LinearGradient {
		endX: 0
		stops: [
			Stop { offset: 0, color: Color.web("#5c5c5c") }
			Stop { offset: 0.5, color: Color.web("#6f6f6f") }
			Stop { offset: 1, color: Color.web("#666666") }
		]
	}
	
	//fill of active panel (can be local or onAgents)
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
						onAgents = not ( event.getProperty().getValue() as Boolean );
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
				else if( WorkspaceItem.AGENTS == event.getKey() ) {
					if( event.getEvent() == CollectionEvent.Event.ADDED ) {
						runInFxThread( function():Void {
							addAgent(event.getElement() as AgentItem);
						});
					} else {
						runInFxThread( function():Void {
							clearTestCases(event.getElement() as AgentItem);
							removeAgent(event.getElement() as AgentItem);
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
							ghostAgent.addTestCase(TestCaseIcon{
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
							deployTestCase(a.getScene(), a.getAgent())
						});
					} else {
						runInFxThread( function():Void {
							undeployTestCase(a.getScene(), a.getAgent())
						});
					}
				}
			} else if (e instanceof BaseEvent){
				/*
				* When project is imported and opened for first time, controller is not aware does it have
				* test cases or not. It becomes aware of them next time.
				* So, here check if ghost agent have them and add if not.
				*/
				def project:ProjectItem = MainWindow.instance.projectCanvas.canvasItem as ProjectItem;
				for( s in project.getScenes() ) {
					runInFxThread( function():Void {
							ghostAgent.addTestCase(TestCaseIcon{
									stateListeners: [this]
									sceneItem: s
								}
							);
						});				
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
			(rin as AgentInspectorNode).selectTestCaseIcon(tc);
		}
		ghostAgent.selectTestCaseIcon(tc);
	}
	
	override function testCaseRemoved(tc: TestCaseIcon): Void {}
	
	var localButton: ToggleActionButton;
	var onAgentsButton: ToggleActionButton;
	
	override var accept = function( d: Draggable ) {
		if(d.node instanceof TestCaseIcon){
			return true;
		}
		else{
			return false;
		}
	}
	
	override var onDrop = function( d: Draggable ) {
		(d.node as TestCaseIcon).remove();
	}
	
	override function create() {
		var text: Text;
		var buttonGroup: ToggleActionGroup = new ToggleActionGroup;
		
		def panelHeight = 325;
		
		def popup = PopupMenu {
			items: [
				MenuItem {
					text: "Detect Agents"
					action: function() {
						AgentDiscovererDialog{}.show();
					}
				}
				Separator{}
				MenuItem {
					text: "New Agent"
					action: function() {
						CreateNewAgentDialog{ workspace: workspace };
					}
				}
			]
		};
		
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
					fill: bind if(onAgents) activePanelFill else inactivePanelFill
					fillOpacity: 1.0
					leftArrowActive: "{__ROOT__}images/leftarrow_active_nontransparent.fxz";
					leftArrowInactive: "{__ROOT__}images/leftarrow_inactive_nontransparent.fxz";
					rightArrowActive: "{__ROOT__}images/rightarrow_active_nontransparent.fxz";
					rightArrowInactive: "{__ROOT__}images/rightarrow_inactive_nontransparent.fxz";
					onMousePressed: function(e: MouseEvent){
						if(e.popupTrigger){
							popup.show( pagelist, e.screenX, e.screenY );
						}
					}
					onMouseReleased: function(e: MouseEvent){
						if(e.popupTrigger){
							popup.show( pagelist, e.screenX, e.screenY );
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
				    fill: bind if(onAgents) activePanelFill else inactivePanelFill
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
				    fill: bind if(onAgents) inactivePanelFill else activePanelFill
				}
				Rectangle { //left panel, main part
					layoutX: 15
					layoutY: 0
				    width: bind leftPanelWidth - 15  
				    height: bind panelHeight
				    fill: bind if(onAgents) inactivePanelFill else activePanelFill
				}
				Line { //bright line under the agent component on left panel 
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
					selected: not onAgents
			        action: function(): Void {
						workspace.setLocalMode( true );
					}
			    }
				onAgentsButton	= ToggleActionButton {
			        text: "On agents"
			        toggleGroup: buttonGroup
			        layoutX: bind leftPanelWidth + 15
					layoutY: 15
					selected: onAgents
			        action: function(): Void {
						workspace.setLocalMode( false );
					}
			    }
			    ghostAgent = AgentInspectorNode { 
			    	layoutX: leftPanelContentOffset
					layoutY: bind panelHeight - 50 - ghostAgent.layoutBounds.height
			    	ghostAgent: true
			    }
			    popup
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

	/** Adds agent node to list */
	function addAgent( agent: AgentItem ):Void {
		pagelist.content = Sequences.sort( [ pagelist.content, AgentInspectorNode { agent: agent } ], COMPARE_BY_TOSTRING ) as Node[];
	}
	
	/** Removes agent from the list */
	function removeAgent( agent:AgentItem ):Void {
		for( node in pagelist.content[p|p instanceof AgentNode] )
			if( (node as AgentNode).agent == agent )
				delete node from pagelist.content;
		for( node in pagelist.content[p|p instanceof AgentInspectorNode] )
			if( (node as AgentInspectorNode).agent == agent )
				delete node from pagelist.content;
	}
	
	/** Assign testcase to a specified agent */
	function deployTestCase(scene: SceneItem, agent: AgentItem): Void {
		for(rin in pagelist.content){
			if((rin as AgentInspectorNode).agent == agent){
				(rin as AgentInspectorNode).addTestCase(TestCaseIcon{
						stateListeners: [this]
						sceneItem: scene
					}
				);
				return;
			}
		}
	}
	
	/** Assign testcase to all agents */
	function deployTestCase(scene: SceneItem): Void {
		for(rin in pagelist.content){
			(rin as AgentInspectorNode).addTestCase(TestCaseIcon{
					stateListeners: [this]
					sceneItem: scene
				}
			);
		}
	}
	
	/** Unassign testcas from specified agent */
	function undeployTestCase(scene: SceneItem, agent: AgentItem): Void {
		for(rin in pagelist.content){
			if((rin as AgentInspectorNode).agent == agent){
				(rin as AgentInspectorNode).undeployTestCase(scene);
				return;
			}
		}
	}
	
	/** Unassign testcas from all agents it is assigned to */
	function undeployTestCase(sceneItem: SceneItem): Void{
		ghostAgent.undeployTestCase(sceneItem);
		for(rin in pagelist.content){
			(rin as AgentInspectorNode).undeployTestCase(sceneItem);
		}
	}
	
	/** Re-populates test cases to ghost agent and all other agents they are assigned to */
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
				ghostAgent.addTestCase(tc);
				
				var rin: AgentInspectorNode;
				var agents = projectItem.getAgentsAssignedTo(s as SceneItem);
				if(agents != null){
					for(r in agents){
						rin = findAgentNode(r as AgentItem);
						if(rin != null){
							rin.addTestCase(tc.copy());
						}
					}
				}
			}
		}
	}
	
	/** Find agent node by it's agent */
	function findAgentNode(agent: AgentItem): AgentInspectorNode {
		for(rin in pagelist.content){
			if((rin as AgentInspectorNode).agent == agent){
				return rin as AgentInspectorNode;
			}
		}
		return null as AgentInspectorNode;
	}
	
	/** Clear test cases from all agent nodes wiothout unassigning them */
	function clearTestCases(): Void{
		ghostAgent.clearTestCases(false);
		for(rin in pagelist.content){
			(rin as AgentInspectorNode).clearTestCases(false);
		}
	}
	
	/** Clear test cases from the agent node specified by it's agent with unassigning them */
	function clearTestCases(agent: AgentItem): Void{
		for(rin in pagelist.content){
			if((rin as AgentInspectorNode).agent == agent){
				(rin as AgentInspectorNode).clearTestCases(true);
				return;
			}
		}
	}
	
	/** Deselects selected testcases (if any) and selects or deselects one given by the argument, depending 
	 *  on value of the other argument. 
	 */
	function selectTestCaseIcon(sceneItem: SceneItem, selected: Boolean): Void {
		ghostAgent.selectTestCaseIcon(sceneItem, selected);
		for(rin in pagelist.content){
			(rin as AgentInspectorNode).selectTestCaseIcon(sceneItem, selected);
		}
	}
	
	/** Deselects all testcase icons */
	function deselectTestCaseIcons(): Void {
		ghostAgent.deselectTestCaseIcons();
		for(rin in pagelist.content){
			(rin as AgentInspectorNode).deselectTestCaseIcons();
		}
	}
	
	postinit {
		//add agents on workspace replace
		var tmp = MainWindow.instance.workspace on replace {
			if(tmp != null){
				for(agent in tmp.getAgents()){
					addAgent(agent);
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
                    url: "{__ROOT__}images/png/inspector-agents-left-default.png"
                }
            },
            middle = ImageView {
                layoutY:0
                layoutX: left.layoutBounds.width
                scaleX: bind if(width > -1) width - 12 else label.layoutBounds.width + 14
                translateX: bind if(width > -1) (width - 12) / 2 else (label.layoutBounds.width + 14) / 2 - .5
                image: Image {
                    url: "{__ROOT__}images/png/inspector-agents-mid-default.png"
                }
            },
            right = ImageView {
                layoutY: 0
                layoutX: bind middle.boundsInParent.width + left.layoutBounds.width - 1
                image: Image {
                    url: "{__ROOT__}images/png/inspector-agents-right-default.png"
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
                    url: "{__ROOT__}images/png/inspector-agents-left-active.png"
                }
            },
            middle = ImageView {
                layoutY:0
                layoutX: left.layoutBounds.width
                scaleX: bind if(width > -1) width - 12 else label.layoutBounds.width + 14
                translateX: bind if(width > -1) (width - 12) / 2 else (label.layoutBounds.width + 14) / 2 - .5
                image: Image {
                    url: "{__ROOT__}images/png/inspector-agents-mid-active.png"
                }
            },
            right = ImageView {
                layoutY:0
                layoutX: bind middle.boundsInParent.width + left.layoutBounds.width - 1
                image: Image {
                    url: "{__ROOT__}images/png/inspector-agents-right-active.png"
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


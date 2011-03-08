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

import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Line;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextBox;
import javafx.scene.control.ToggleGroup;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.SVGPath;
import javafx.geometry.Insets;
import javafx.fxd.FXDNode;
import javafx.scene.effect.Glow;
import javafx.scene.Cursor;
import javafx.scene.text.Font;  

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;

import com.javafx.preview.control.MenuButton;
import com.javafx.preview.control.CustomMenuItem;
import com.javafx.preview.control.PopupMenu;

import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.util.BeanInjector;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.statistics.store.ExecutionListener;

import com.eviware.loadui.util.statistics.ExecutionListenerAdapter;

import com.eviware.loadui.fx.statistics.StatisticsWindow;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.FxUtils.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.Comparable;
import java.util.EventObject;

/**
 * A control for selecting executions to compare
 *
 * @author predrag.vucetic
 * @author henrik.olsson
 */
public class ExecutionSelector extends Group {
   
   var leftExecution: Execution = bind StatisticsWindow.execution on replace {
      FxUtils.runInFxThread( function():Void { setLabels(); } );
   }
   
   var rightExecution: Execution = bind StatisticsWindow.comparedExecution on replace {
      FxUtils.runInFxThread( function():Void { setLabels(); } );
   }
   
   def executionManagerStateListener = new ExecutionManagerStateListener();
   def executionManagerListener = ExecutionManagerListener{};
   def executionListener = ExecutionListener{};
   
   def executionManager = BeanInjector.getBean( ExecutionManager.class ) on replace oldExecutionManager {
      oldExecutionManager.removeExecutionListener( executionManagerStateListener );
      oldExecutionManager.removeEventListener( EventObject.class, executionManagerListener );
      
		executionManager.addExecutionListener( executionManagerStateListener );
		executionManager.addEventListener( EventObject.class, executionManagerListener );
	}
   
   def project = bind StatisticsWindow.instance.project on replace prevProject {
      def names = executionManager.getExecutionNames();
      if(prevProject != null){
			for( n in names ) {
			   if((n as String).startsWith(prevProject.getId())){
					executionManager.getExecution(n as String).removeEventListener( BaseEvent.class, executionListener );
				}
			}
		}
		if(project != null){
			for( n in names ) {
			   if((n as String).startsWith(project.getId())){
					executionManager.getExecution(n as String).addEventListener( BaseEvent.class, executionListener );
				}
			}
		} 
  		FxUtils.runInFxThread( function():Void { loadExecutions(); } );
   }
   
   def glow = Glow { level: .2 };
   
   var popupWidth: Number = 411;
   var popupHeight: Number = 442;
   
   def openButton: Group = Group {
	   content: [
	   	FXDNode {
				url: bind openImg
				visible: true
				effect: bind if( openButton.hover ) glow else null
			}
		]
		onMousePressed: function( e:MouseEvent ) { menu.show(openButton, HPos.CENTER, VPos.BOTTOM, -201 + openButton.layoutBounds.width / 2, 6); }
	}
   var leftLabel: Label = Label {}
   var rightLabel: Label = Label {}
   
   function setLabels(): Void {
      if(leftExecution == null or isCurrentExecution(leftExecution))
		{
			leftLabel.text = "Current run";
		}
		else
		{
      	leftLabel.text = leftExecution.getLabel();
      }
      if(rightExecution != null){
      	rightLabel.text = rightExecution.getLabel();
      }
      else{
         rightLabel.text = ""; 
      }
   }
   
   var menu: PopupMenu;
   var item: CustomMenuItem;
   
	override var styleClass = "execution-selector";
	
	def filterToggleGroup = ToggleGroup {};
	
	var filterAll: ToggleButton = ToggleButton {
	   styleClass: "execution-selector-menu-filter-button" 
		text: "All"
		toggleGroup: filterToggleGroup
		selected: true
   }
	var filterRecently: ToggleButton = ToggleButton {
		styleClass: "execution-selector-menu-filter-button"
		text: "Recent"
		toggleGroup: filterToggleGroup
   }
	var filterArchive: ToggleButton = ToggleButton {
	   styleClass: "execution-selector-menu-filter-button"
		text: "Archived"
		toggleGroup: filterToggleGroup
   }
	
	def selectedFilter = bind filterToggleGroup.selectedToggle on replace oldFilter {
		if( selectedFilter == null ) {
			FX.deferAction( function():Void { oldFilter.selected = true } );
		} else {
			FxUtils.runInFxThread( function():Void { loadExecutions(); } );
		}
	}
	
	var leftCarouselle: VCarouselle;
	var rightCarouselle: VCarouselle;
	
	var btnLoadData: Button;
	var btnClear: Button;
	
	var leftDisabled: RadioButton;
	var rightDisabled: RadioButton;
	var currentRunsRightRadioButton: RadioButton;
	
	var leftSelected = bind leftRadioToggles.selectedToggle on replace {
	   if( rightDisabled != null ){
	       rightDisabled.disable = false;
	   }
	   if( leftSelected != null ){
	       rightDisabled = leftToRightMapping.get( leftSelected ) as RadioButton;
		    rightDisabled.disable = true;
	   }
	   else{
	       rightDisabled = null;
	   }
	   rightRadioButtons[0].radioButton.disable = true;
   }
	
	var rightSelected = bind rightRadioToggles.selectedToggle on replace {
	   if( leftDisabled != null ){
	       leftDisabled.disable = false;
	   }
	   if( rightSelected != null ){
	       leftDisabled = rightToLeftMapping.get( rightSelected ) as RadioButton; 
		    leftDisabled.disable = true;
	   }
	   else{
	       leftDisabled = null;
	   }
	   rightRadioButtons[0].radioButton.disable = true;
   }
   
	var leftRadioButtons: CustomRadioButton[] = [];
	var rightRadioButtons: CustomRadioButton[] = [];
	
	var leftRadioToggles: ToggleGroup;
	var rightRadioToggles: ToggleGroup;
	
	var leftToRightMapping: HashMap;
	var rightToLeftMapping: HashMap;
	
	var leftExecutions: HashMap;
	var rightExecutions: HashMap;
	
	function loadExecutions(): Void {
	   if(selectedFilter == filterAll){
		    loadExecutions(true, true);
		}
		else if(selectedFilter == filterRecently){
		    loadExecutions(false, true);
		}
		else if(selectedFilter == filterArchive){
		    loadExecutions(true, false);
		}
		setLabels();
	}
	
	function reset(): Void {
		leftToRightMapping = new HashMap();
		rightToLeftMapping = new HashMap();
		leftExecutions = new HashMap();
		rightExecutions = new HashMap();
		leftRadioToggles = ToggleGroup {};
		rightRadioToggles = ToggleGroup {}
		delete leftRadioButtons;
		delete rightRadioButtons;
	}
	
	function loadExecutions(archive: Boolean, recently: Boolean): Void {
	   reset();
		if(project == null){
		    // project is null, return. radio buttons are removed in reset() method
		    return;
		}
		def holderList: ArrayList = new ArrayList();
		def names = executionManager.getExecutionNames();
		for( n in names ) {
		   if((n as String).startsWith(project.getId())){
			   def e = executionManager.getExecution(n as String);
			   if((archive and e.isArchived() or recently and (not e.isArchived())) or e == executionManager.getCurrentExecution()){
					holderList.add( ExecutionComparable { execution: e } );
				}
			}
		}
		if( executionManager.getCurrentExecution() == null ){
		    //there is no running execution so add radio for it without execution object
		    addCurrentRunDummyRadio();
		}
		Collections.sort( holderList );
		for( h in holderList ) {
			addRadioButton((h as ExecutionComparable).execution);
		}
		setRadioButtons();
	}
	
	function addCurrentRunDummyRadio(){
		var label: String = "Current run";
		def left = CustomRadioButton {text: label, radioGroup: leftRadioToggles};
		def right = CustomRadioButton {text: label, radioGroup: rightRadioToggles};
		insert left into leftRadioButtons;
		insert right into rightRadioButtons;
		leftToRightMapping.put(left.radioButton, right.radioButton);
		rightToLeftMapping.put(right.radioButton, left.radioButton);
	}
	
	function addRadioButton(e: Execution){
		var label:String;
		if( isCurrentExecution( e ) )
		{
			label = "Current run";
		}
		else
		{
			label = e.getLabel();
		}
		def left = CustomRadioButton {text: label, radioGroup: leftRadioToggles};
		def right = CustomRadioButton {text: label, radioGroup: rightRadioToggles};
		insert left into leftRadioButtons;
		insert right into rightRadioButtons;
		leftToRightMapping.put(left.radioButton, right.radioButton);
		rightToLeftMapping.put(right.radioButton, left.radioButton);
		leftExecutions.put(left.radioButton, e);
		rightExecutions.put(right.radioButton, e);
	}
	
	function isCurrentExecution(e: Execution): Boolean {
	    def curr: Execution = executionManager.getCurrentExecution();
	    e != null and curr != null and e.getId() == curr.getId();
	}
	
	function setRadioButtons(): Void {
	   unsetRadioButtons(true, true);
	   if(leftExecution != null){
			for(c in leftRadioButtons){
			    def e = leftExecutions.get(c.radioButton) as Execution; 
			    if(e != null and e.getId() == leftExecution.getId()){
			        c.radioButton.selected = true;
			        break;
			    }
			}
		}
		else if( executionManager.getCurrentExecution() == null ){
		    //left execution is null, so select dummy current execution radio button
		    leftRadioButtons[0].radioButton.selected = true;
		}
		if(rightExecution != null){
			for(c in rightRadioButtons){
			    def e = rightExecutions.get(c.radioButton) as Execution;
			    if(e != null and e.getId() == rightExecution.getId()){
			        c.radioButton.selected = true;
			        break;
			    }
			}
		}
		rightRadioButtons[0].radioButton.disable = true;
		if( leftRadioToggles.selectedToggle == null )
			leftRadioButtons[0].radioButton.selected = true;
	}
	
	function unsetRadioButtons(left: Boolean, right: Boolean): Void {
	   if(left){
			for(c in leftRadioButtons){
			    if(c.radioButton.selected){
			        c.radioButton.selected = false;
			        break;
			    }
			}
		}
		if(right){
			for(c in rightRadioButtons){
			    if(c.radioButton.selected){
			        c.radioButton.selected = false;
			        break;
			    }
			}
		}    
	}
	
	var resizeYStart: Number = 0;
	
//	var closeImg: String = "{__ROOT__}images/execution-selector-close.fxz";
	var openImg: String = "{__ROOT__}images/execution-selector-open.fxz";
	var resizeImg: String = "{__ROOT__}images/execution-selector-resize.fxz";
	var radioHoverImg: String = "{__ROOT__}images/execution-selector-radio-hover.fxz";
				    
	init {
//	   def closeAction: Group = Group {
//		   content: [
//		   	Rectangle {
//				    width: 10  
//				    height: 10
//				    fill: Color.TRANSPARENT
//				}
//		   	FXDNode {
//					url: bind closeImg
//					visible: true
//					effect: bind if( closeAction.hover ) glow else null
//				}
//			]
//			onMousePressed: function( e:MouseEvent ) { menu.hide(); }
//		}
		
	   var popupContent: VBox = VBox {
	       spacing: 12
	       padding: Insets { top: 11 right: 18 bottom: 0 left: 18}
	       nodeHPos: HPos.CENTER
	       content: [
		      HBox {
					spacing: 5
					nodeVPos: VPos.TOP
					content: [
						HBox{
						    nodeVPos: VPos.CENTER
						    spacing: 5
						    layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS, vfill: false, vgrow: Priority.NEVER }
						    content: [
								Label { text: "Compare runs:      ", font: Font { name:"Arial", size:12 }},
								filterAll,
								filterRecently,
								filterArchive
						    ]
						},
//						closeAction
					]
				}, Line {
				   styleClass: "execution-selector-line"
					endX: bind popupWidth - 36
				}, HBox {
					spacing: 21
					nodeVPos: VPos.CENTER
					layoutInfo: LayoutInfo { vfill: true, vgrow: Priority.ALWAYS }
					content: [
						leftCarouselle = VCarouselle {
							layoutInfo: LayoutInfo { width: 162, vfill: true, vgrow: Priority.NEVER }
							itemSpacing: 6
							arrowVSpace: 36
							items: bind leftRadioButtons
						},
						Line {
						   styleClass: "execution-selector-line"
						   startY: 18
							endY: bind leftCarouselle.layoutBounds.height - 18
						},
						rightCarouselle = VCarouselle {
							layoutInfo: LayoutInfo { width: 162, vfill: true, vgrow: Priority.NEVER }
							itemSpacing: 6
							arrowVSpace: 36
							items: bind rightRadioButtons
						}
					]
				}, Line {
				   styleClass: "execution-selector-line"
					endX: bind popupWidth - 36
				}, Group{ 
					content:	[
						Rectangle{
						    width: bind popupWidth - 36
						    height: 5
						    fill: Color.TRANSPARENT
						},
						btnLoadData = Button {
							layoutX: bind (popupWidth - 36 - btnLoadData.layoutBounds.width) / 2
							text: "Load data"
							action: function():Void {
							   if(leftSelected != null){
							       StatisticsWindow.execution = leftExecutions.get(leftSelected) as Execution;
							   }
							   else{
							       StatisticsWindow.execution = null;
							   }
							   if(rightSelected != null){
							       StatisticsWindow.comparedExecution = rightExecutions.get(rightSelected) as Execution;
							   }
							   else{
							       StatisticsWindow.comparedExecution = null;
							   }
								menu.hide();
							}
						},
						btnClear = Button {
							layoutX: bind popupWidth - 36 - btnClear.layoutBounds.width
							text: "Clear"
							action: function():Void {
								unsetRadioButtons(false, true);
							}
						}
					]
				}
	       ]
	   } 
	   
	   def resizeAction: Group = Group {
	      cursor: Cursor.V_RESIZE
		   content: [
		     	Rectangle {
				    width: 10  
				    height: 10
				    fill: Color.TRANSPARENT
				}
		   	FXDNode {
		   	   layoutX: -3
	      		layoutY: -3 
					url: bind resizeImg
					visible: true
				}
			]
			onMousePressed: function( e: MouseEvent ) {
		   	if( e.primaryButtonDown ) {
		   	    resizeYStart = e.screenY;
		   	} 
		   }
		   onMouseDragged: function( e: MouseEvent ) {
		   	if( e.primaryButtonDown ) {
		   	    def delta = e.screenY - resizeYStart;
		   	    if(popupHeight + delta >= 250){
			   	    popupHeight += delta;
			   	    resizeYStart = e.screenY;
		   	    }
		   	} 
		   }
		}
					
	   item = CustomMenuItem {
			styleClass: "execution-selector-menu-item"
			hideOnClick: false
			layoutInfo: LayoutInfo { height: bind popupHeight, width: popupWidth }
			node: VBox {
				snapToPixel: true
				nodeHPos: HPos.RIGHT
				spacing: 0
				content: [
					popupContent,
					resizeAction
				]
			} 
		}
		
		menu = PopupMenu {
		    styleClass: "execution-selector-menu"
		    items: [item]
		    layoutInfo: LayoutInfo{ height: bind popupHeight, width: bind popupWidth }
		    onShowing: function(){FX.deferAction( function(){ setRadioButtons(); } )}
		}
	   
	   content = [
	   	HBox {
	   	    nodeHPos: HPos.CENTER
	   	    spacing: 9
	   	    content: [leftLabel, openButton, rightLabel]
	   	},
	   	menu 
		]		   		
	}
}

class CustomRadioButton extends Group {
    
    public var text: String;
    
    public-init var radioGroup: ToggleGroup;
    
    public-read var radioButton: RadioButton;
    
    var width: Number = 162;
    var height: Number = 21;
    
    var lPad: Number = 9;
    var rPad: Number = 3;
    
    init{
        content = [
        		Group{
        		    content:[
        		    	Rectangle {
						    width: width
						    height: height
						    arcWidth: 5  
						    arcHeight: 5
						    fill: Color.TRANSPARENT
						    stroke: Color.web("#202020")
						},
						Rectangle {
						    layoutX: 1
						    layoutY: 1
						    width: width - 2
						    height: height - 2
						    arcWidth: 4  
						    arcHeight: 4
						    fill: Color.TRANSPARENT
						    stroke: Color.web("#2c2c2c")
						},
						Rectangle {
						    layoutX: 2
						    layoutY: 2
						    width: width - 4
						    height: height - 4
						    arcWidth: 3  
						    arcHeight: 3
						    fill: Color.TRANSPARENT
						    stroke: Color.web("#2e2e2e")
						}
        		    ]
        		    visible: bind radioButton.hover
        		},
        		Rectangle {
				    width: width
				    height: height
				    arcWidth: 5  
				    arcHeight: 5
				    fill: bind if( radioButton.selected ) Color.web("#1a1a1a") else Color.TRANSPARENT
				    stroke: bind if( radioButton.selected ) Color.web("#1a1a1a") else Color.TRANSPARENT
				},
        		radioButton = RadioButton {
        		    layoutX: lPad
        		    layoutY: bind (height - radioButton.layoutBounds.height)/2
        		    layoutInfo: LayoutInfo {
        		        width: width - lPad - rPad
        		    },
        		    text: bind " {text}", 
        		    toggleGroup: radioGroup
        		}
        ]
    }
}

class ExecutionComparable extends Comparable {
	
	public var execution: Execution;
	
	override function compareTo( o: Object ): Integer {
		if( (o as ExecutionComparable).execution == null )
			return 1;
		((o as ExecutionComparable).execution.getStartTime() - execution.getStartTime()) as Integer;
	}
}

class ExecutionManagerStateListener extends ExecutionListenerAdapter {
   override function executionStarted( state: ExecutionManager.State ) {
		FxUtils.runInFxThread( function():Void { loadExecutions(); } );
   }
}

class ExecutionManagerListener extends EventHandler {
	override function handleEvent( e:EventObject ):Void {
		if( e instanceof CollectionEvent ) {
			def event = e as CollectionEvent;
			if( CollectionEvent.Event.REMOVED == event.getEvent() ) {
			   FxUtils.runInFxThread( function():Void {
				   def execution: Execution = event.getElement() as Execution;
				   if(execution == leftExecution){
				   	StatisticsWindow.execution = null;
				   }
				   else if(execution == rightExecution){
				      StatisticsWindow.comparedExecution = null; 
				   }
					loadExecutions();
				} );
			} 
		}
	}
}

class ExecutionListener extends EventHandler {
	override function handleEvent( e:EventObject ):Void {
		if( e instanceof BaseEvent ) {
			def event = e as BaseEvent;
			if( event.getKey().equals(Execution.ARCHIVED)) {
				FxUtils.runInFxThread( function():Void { loadExecutions(); } );
			} 
		}
	}
}
/*
 * Copyright 2011 SmartBear Software
 */
package com.eviware.loadui.fx.assertions;

import javafx.scene.Group;
import javafx.scene.shape.Polyline;
import javafx.scene.layout.Stack;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollView;
import javafx.scene.control.ScrollBarPolicy;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.util.Math;
import javafx.scene.shape.Line;

import com.javafx.preview.layout.Grid;
import com.javafx.preview.layout.GridRow;
import com.javafx.preview.layout.GridLayoutInfo;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.assertion.AssertionAddon;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.SettingsLayoutContainerForm;
import com.eviware.loadui.fx.ui.form.fields.TextField;
import com.eviware.loadui.fx.ui.treeselector.CascadingTreeSelector;
import com.eviware.loadui.fx.assertions.AssertionTreeModel;
import com.eviware.loadui.fx.assertions.AssertionTreeSelectedItemHolder;
import com.eviware.loadui.api.statistics.StatisticHolder;

import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.assertion.AssertionAddon;
import com.eviware.loadui.util.serialization.*;
import com.eviware.loadui.util.assertion.RangeConstraint;
import com.eviware.loadui.api.serialization.Resolver;

import java.lang.Runnable;

public class AddAssertionDialog {

	var statisticHolderLabel: Label;
	var statisticVariableLabel: Label;
	var statisticLabel: Label;
		
	public-init var statisticHolder: StatisticHolder;

	var selected: AssertionTreeSelectedItemHolder;
	
	var warningMessage: String = "";
	
	def warningDialog: Dialog = Dialog {
		scene: AppState.byName("MAIN").scene
		title: "Validation Error"
		content: [ Label { text: bind warningMessage } ]
		showPostInit: false
		noCancel: true
		onOk: function():Void { warningDialog.close(); }
	}
	
	def minTextBox = TextBox {
		selectOnFocus: true
		text: "0"
		layoutInfo: LayoutInfo { width: 150, hfill: false }
	}
	
	def maxTextBox = TextBox {
		selectOnFocus: true
		text: "10"
		layoutInfo: LayoutInfo { width: 150, hfill: false }
	}

	def timesTextBox = TextBox {
		selectOnFocus: true
		text: "0"
		layoutInfo: LayoutInfo { width: 50, hfill: false }
	}
	
	def withinTextBox = TextBox {
		selectOnFocus: true
		text: "1"
		layoutInfo: LayoutInfo { width: 50, hfill: false }
	}

	def assertionNameTextBox = TextBox {
		selectOnFocus: true
		text: ""
		layoutInfo: LayoutInfo { width: 300, hfill: false }
	}
	
	var min;
    var max;
    var times;
    var within;
		        
	function validate(): Boolean {
	    try{
	        if(selected == null){
	            warningMessage = "Statistic to assert is not selected.";
	            warningDialog.show();
	            return false;
	        }
	        
	        assertionNameTextBox.text = assertionNameTextBox.text.trim();
	        if(assertionNameTextBox.text.length() == 0){
	            warningMessage = "Assertion name is not specified.";
	            warningDialog.show();
	            return false;
	        }
	        
	        min = Integer.valueOf(minTextBox.text);
	        max = Integer.valueOf(maxTextBox.text);
	        times = Integer.valueOf(timesTextBox.text);
	        within = Integer.valueOf(withinTextBox.text);
	        
	        if(min < 0){
	            warningMessage = "Min value should be greater than or equal to zero.";
	            warningDialog.show();
	            return false;
	        }
	        
	        if(max < min){
	            warningMessage = "Max value should be greater than or equal to Min.";
	            warningDialog.show();
	            return false;
	        }

			if(times < 0 or within < 0){
	            warningMessage = "Both Tolerance values should be greater than or equal to zero.";
	            warningDialog.show();
	            return false;
	        }
	        	        	        
	        return true;
	    }
	    catch(e: java.lang.Exception){
	        warningMessage = "Constraint and Tolerance values should be numbers.";
	        warningDialog.show();
	        return false;
	    }
	}
	
	function createAssertion(): Void {
	    def project = MainWindow.instance.projectCanvas.canvasItem as ProjectItem; 
	    def assertionAddon: AssertionAddon = project.getAddon( AssertionAddon.class ) as AssertionAddon;
	    def sVariable = statisticHolder.getStatisticVariable( selected.getStatisticVariableName() );
	    def resolver: Resolver = new StatisticVariableResolver( sVariable );
	    
	    def assertion = assertionAddon.createAssertion( statisticHolder as Addressable, resolver ) as AssertionItem.Mutable;
        assertion.setConstraint(new RangeConstraint( min, max ));
        assertion.setTolerance(within,times);
        assertion.setLabel(assertionNameTextBox.text);
	}
	
	postinit {
	    
	    var grid: Grid;
	    var hbox: HBox;
	    var treeSelector: CascadingTreeSelector;
	    var titleRegion: Region;
	    
		def dialog: Dialog = Dialog {
			scene: AppState.byName("MAIN").scene
			title: "MAKE ASSERTION"
			content: [
				VBox {
				    spacing: 15
				    styleClass: "add-assertion-dialog"
				    content: [
						HBox{
						    nodeVPos: VPos.CENTER
				    	    spacing: 6
				    	    layoutInfo: LayoutInfo { vfill: false, vgrow: Priority.NEVER }
				    	    content: [
				    	    	Label { text: "Assertion name" },
				    	    	assertionNameTextBox
				    	    ]
				    	}
				    	Group {
				    	   content: [ 
				    			Group{
				    				content: [
				    					titleRegion = Region { styleClass: "title-region", layoutInfo: LayoutInfo { width: bind hbox.layoutBounds.width, height: 24, hfill: false, vfill: false } }
				    					Region { 
				    						styleClass: "border"
				    						layoutX: 0
				    						layoutY: bind titleRegion.layoutBounds.height - 1
				    						layoutInfo: LayoutInfo { 
				    							width: bind hbox.layoutBounds.width, 
				    							height: bind hbox.layoutBounds.height + 4, 
				    							hfill: false, 
				    							vfill: false 
				    						} 
				    					}
				    					Line { 
				    						styleClass: "line"
				    						startY: bind titleRegion.layoutBounds.height
				    						endY: bind hbox.layoutBounds.height + hbox.layoutY
				    						startX: bind treeSelector.layoutBounds.width / 3
				    						endX: bind treeSelector.layoutBounds.width / 3 
				    					}
				    					Polyline {
				    					    layoutX: bind treeSelector.layoutBounds.width / 3 - 10
				    					    layoutY: bind titleRegion.layoutBounds.height / 2
    							    	    styleClass: "arrow"
    							    	    points: [
    							    	         0.0, -2.0,
     							    	         2.0, 0.0,
     							    	         0.0, 2.0
    							    	    ]
    							    	}	
				    					Line { 
				    						styleClass: "line"
				    						startY: bind titleRegion.layoutBounds.height
				    						endY: bind hbox.layoutBounds.height + hbox.layoutY
				    						startX: bind  2 * treeSelector.layoutBounds.width / 3 
				    						endX: bind  2 * treeSelector.layoutBounds.width / 3  
				    					}
				    					Polyline {
				    					    layoutX: bind 2 * treeSelector.layoutBounds.width / 3 - 10
				    					    layoutY: bind titleRegion.layoutBounds.height / 2
    							    	    styleClass: "arrow"
    							    	    points: [
    							    	         0.0, -2.0,
	 							    	         2.0, 0.0,
	 							    	         0.0, 2.0
    							    	    ]
    							    	}	
				    					Line { 
				    						styleClass: "line"
				    						startY: bind titleRegion.layoutBounds.height
				    						endY: bind hbox.layoutBounds.height + hbox.layoutY
				    						startX: bind  treeSelector.layoutBounds.width 
				    						endX: bind  treeSelector.layoutBounds.width  
				    					}
				    					Polyline {
				    					    layoutX: bind treeSelector.layoutBounds.width - 10
				    					    layoutY: bind titleRegion.layoutBounds.height / 2
    							    	    styleClass: "arrow"
    							    	    points: [
    							    	         0.0, -2.0,
    							    	         2.0, 0.0,
    							    	         0.0, 2.0
    							    	    ]
    							    	}
    							    	statisticHolderLabel = Label { 
    							    	    styleClass: "title"
   							    	        layoutX: 12
   				    					    layoutY: 6
    							    	}	
    							    	statisticVariableLabel = Label { 
    							    	    styleClass: "title"
   							    	        layoutX: bind treeSelector.layoutBounds.width / 3 + 12
   				    					    layoutY: 6
    							    	}	
    							    	statisticLabel = Label { 
    							    	    styleClass: "title"
   							    	        layoutX: bind 2 * treeSelector.layoutBounds.width / 3 + 12
   				    					    layoutY: 6
    							    	}	    							    	
    							    	Label { 
    							    	    styleClass: "title"
   							    	        layoutX: bind treeSelector.layoutBounds.width + 12
   				    					    layoutY: 6
    							    		text: "Values" 
    							    	}				
				    				]
				    			}
						    	hbox = HBox {
		    					    layoutInfo: LayoutInfo { width: 1000, height: 220 } 
		    					    layoutY: bind titleRegion.layoutBounds.height + 1  
		    					    content: [
		    					    	treeSelector = CascadingTreeSelector {
		    					    	    externalLabels: [statisticHolderLabel, statisticVariableLabel, statisticLabel]
		    					    	    treeLevelNodeLayoutInfo: LayoutInfo { margin: Insets { left: 4, top: 10, bottom: 10, right: 0 } };
		    	    						columnCount: 3
		    	    						treeModel: new AssertionTreeModel( statisticHolder )
		    	    						allowMultiple: false
		    	    						onSelect: function(obj):Void { selected = obj as AssertionTreeSelectedItemHolder; }
		    	    						onDeselect: function(obj):Void { selected = null; }
		    	    					}
		    	    					VBox {
		    	    					    layoutInfo: LayoutInfo { hfill: false, hgrow: Priority.NEVER, vfill: false, vgrow: Priority.NEVER }
		    	    					    padding: Insets { left: 12, top: 12, right: 12, bottom: 12 }
		    	    					    spacing: 3
		    	    					    vpos: VPos.TOP
		    	    					    content: [
		    	    					    	Label { text: "Constraint", styleClass: "bold-text"}
		    	    					    	grid = Grid {
		    	    					    	    layoutInfo: LayoutInfo { hfill: false, hgrow: Priority.NEVER }
		    	    					    	    hgap: 20
		    	    					    		rows: [
		    	    					    	         GridRow { cells: [Label { text: "Min" }, Label { text: "Max" }] }
		    	    					    	         GridRow { cells: [minTextBox, maxTextBox] }
		    	    					    		]
		    	    					    	}
		    	    					    	Region { style: "-fx-background-color: transparent;", layoutInfo: LayoutInfo { width: 1, height: 10 } }
		    	    					    	Line {
													styleClass: "line"
						    						endX: bind grid.layoutBounds.width
						    					}
		    	    					    	Region { style: "-fx-background-color: transparent;", layoutInfo: LayoutInfo { width: 1, height: 10 } }				    					
		    	    					    	Label { text: "Tolerance", styleClass: "bold-text" }
		    	    					    	HBox{
		    	    					    	    nodeVPos: VPos.CENTER
		    	    					    	    spacing: 6
		    	    					    	    layoutInfo: LayoutInfo { hfill: false, hgrow: Priority.NEVER }
		    	    					    	    content: [
		    	    					    	    	Label { text: "Allow the constraint to be violated" },
		    	    					    	    	timesTextBox,
		    	    					    	    	Label { text: "times," }
		    	    					    	    ]
		    	    					    	}
		    	    					    	HBox{
		    	    					    	    nodeVPos: VPos.CENTER
		    	    					    	    spacing: 6
		    	    					    	    layoutInfo: LayoutInfo { hfill: false, hgrow: Priority.NEVER }
		    	    					    	    content: [
		    	    					    	    	Label { text: "within" },
		    	    					    	    	withinTextBox,
		    	    					    	    	Label { text: "seconds." }
		    	    					    	    ]
		    	    					    	}   
		    	    					    ]
		    	    					}
		    					    ]
						    	}
				    		]
				    	}
				    ]
				}
			]
			okText: "Add"
			onOk: function() {
				if(validate()){
				    dialog.close();
					createAssertion();
				}
			}
		}
	}
}
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
 
/*
*ProjectMenu.fx
*
*Created on jun 1, 2010, 11:52:51 fm
*/

package com.eviware.loadui.fx.ui.menu;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.geometry.VPos;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;

import com.javafx.preview.control.MenuItem;
import com.javafx.preview.control.MenuButton;

import com.sun.javafx.scene.layout.Region;

import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.dialogs.*;
import com.eviware.loadui.fx.dialogs.*;
import com.eviware.loadui.fx.ui.menu.button.*;
import com.eviware.loadui.fx.widgets.TrashHole;
import com.eviware.loadui.fx.FxUtils.*;
import com.eviware.loadui.fx.ui.resources.Paints;
import com.eviware.loadui.fx.ui.resources.MenuArrow;
import com.eviware.loadui.fx.widgets.RunController;
import com.eviware.loadui.fx.widgets.DistributionModeSelector;
import com.eviware.loadui.fx.summary.SummaryReport;
import com.eviware.loadui.fx.widgets.canvas.Canvas;

import com.eviware.loadui.api.model.ModelItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;

import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.fx.wizards.NewProjectWizard;
import com.eviware.loadui.fx.statistics.StatisticsWindow;

import javax.swing.JFileChooser;
import java.io.File;
import java.lang.Exception;
import java.io.IOException;

import java.util.EventObject;

public class ProjectMenu extends HBox {
	def listener = new SummaryListener();
	var statMonitor = StatisticsWindow.getInstance();
	
	public var project: ProjectItem on replace oldProject = newProject {
		//workspaceLabel = project.getWorkspace().getLabel();
		projectLabel = project.getLabel();
		statMonitor.project = project;
		summaryEnabled = false;
		if( oldProject != null )
			oldProject.removeEventListener( BaseEvent.class, listener );
		if( newProject != null )
			newProject.addEventListener( BaseEvent.class, listener );
	}

	
	var workspaceLabel:String = "Workspace";
	var projectLabel:String;
	
	override var spacing = 3;
	override var nodeVPos = VPos.CENTER;
	
	public var projectMenuFill: Paint = Color.TRANSPARENT;
	public var projectMenuClosedTextFill: Paint = Color.web("#666666");
	public var projectMenuOpenedTextFill: Paint = Color.web("#4D4D4D");
	public var projectMenuClosedArrowFill: Paint = Color.web("#666666");
	public var projectMenuOpenedArrowFill: Paint = Color.web("#4D4D4D");
	public var projectMenuClosedFont: Font = Font{name:"Arial", size:10};
	public var projectMenuOpenedFont: Font = Font{name:"Arial", size:18};
	public var workspaceMenuClosedTextFill: Paint = Color.web("#666666");
	public var workspaceMenuClosedArrowFill: Paint = Color.web("#666666");
	public var workspaceMenuClosedFont: Font = Font{name:"Arial", size:10};
	
	override var layoutInfo = LayoutInfo {
		hgrow: Priority.ALWAYS
		vgrow: Priority.NEVER
		hfill: true
		vfill: false
		height: 70
	}
	
	var summaryEnabled = false;
	
	def showNotes = bind Canvas.showNotes on replace {
		showNotesButton.selected = showNotes;
	}
	var showNotesButton:MenubarToggleButton;
	def showNotesButtonState = bind showNotesButton.selected on replace {
		if( showNotesButton.armed )
		Canvas.showNotes = showNotesButtonState;
	}
	
	// used for file chooser in 'save as' action to set the previously selected value
	// when file chooser is reopened
	var lastFileChooserLocation: File;
	
	init {
		var menuContent:Node;
		var menuButton:MenuButton;
		
		content = [
			ImageView {
				image: Image {
					url: "{__ROOT__}images/png/toolbar-background.png"
					width: width
				}
				fitWidth: bind width
				managed: false
				layoutY: -20
			}, Rectangle {
				width: 78
				height: 73
				fill: Color.rgb( 0, 0, 0, 0.1 )
				managed: false
			}, Rectangle {
				width: bind width
				height: 20
				fill: Color.TRANSPARENT
				managed: false
				onMousePressed: function ( e:MouseEvent ) { 
					// This is disabled until issue LUCO-620 is fixed.
				   // AppState.byName("MAIN").displayWorkspace(); 
				}
			}, Label {
				layoutInfo: LayoutInfo {
					width: 95
				}
			}, VBox {
				content: [
					HBox {
						nodeVPos: VPos.CENTER
						content: [
							Label {
								text: bind workspaceLabel
								textFill: bind workspaceMenuClosedTextFill
								font: bind workspaceMenuClosedFont
								layoutInfo: LayoutInfo {
									height: 20
									width: bind menuButton.width
									margin: Insets { left: 3 }
								}
							}, DistributionModeSelector {
								layoutInfo: LayoutInfo { height: 16, hfill: false, vfill: false }
							}
						]
					},
					HBox {
						layoutInfo: LayoutInfo {
							hgrow: Priority.ALWAYS
							vgrow: Priority.NEVER
							hfill: true
							vfill: false
							height: 50
						}
						spacing: 3
						nodeVPos: VPos.CENTER
						content: [
							menuButton = MenuButton {
								styleClass: bind if( menuButton.showing ) "menu-button-showing" else "menu-button"
								layoutInfo: LayoutInfo { hshrink: Priority.SOMETIMES, minWidth: 100 }
								text: bind projectLabel
								font: bind projectMenuOpenedFont
								items: [
									MenuItem {
				                    text: "New TestCase"
				                    action: function() { 
				                    	if ( MainWindow.instance.projectCanvas.canvasItem instanceof ProjectItem ) {
				                            CreateNewTestCaseDialog { project: MainWindow.instance.projectCanvas.canvasItem as ProjectItem };
				                        }
				                    }
				                }
				               MenuItem {
				                	text: "New Project Wizard"
				                	action: function() { 
				                	    if ( MainWindow.instance.projectCanvas.canvasItem instanceof ProjectItem ) {
				                			var newWizard:NewProjectWizard = NewProjectWizard{workspace: (MainWindow.instance.projectCanvas.canvasItem as ProjectItem).getWorkspace()};
				                			newWizard.show();
				                	    }
				                	}
				                }
				                Separator{}
				                MenuItem {
				                    text: "Rename"
				                    action: function() { 
				                    	RenameModelItemDialog { 
				                    		labeled: project
				                    	}

				                    	projectLabel = project.getLabel();
				                    }
				                }
				                Separator{}
				                MenuItem {
				                    text: "Settings"
				                    action: function() {ProjectSettingsDialog{}.show(MainWindow.instance.projectCanvas.canvasItem as ProjectItem); }
				                }
				                Separator{}
				                MenuItem {
				                    text: "Save"
				                    action: function() {
				                    	MainWindow.instance.projectCanvas.generateMiniatures(); 
										project.save();
				                    }
				                }
				                MenuItem {
				                    text: "Save As"
				                    action: function() {
				                        def chooser = new JFileChooser(lastFileChooserLocation);
				                        chooser.addChoosableFileFilter(new XMLFileFilter());
				                        chooser.setAcceptAllFileFilterUsed(false);
				                        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(null)) {
				                            var destination = chooser.getSelectedFile();
				                            if( not destination.getName().endsWith(".xml") ) {
				                                def newname = "{destination.getAbsolutePath()}.xml";
				                                destination = new File(newname); 
				                            }
				                            lastFileChooserLocation = destination;
							                   if(destination != null and destination.exists()){
							                     // confirmation dialog when selected file already exists
														def confirmDialog: Dialog = Dialog {
															title: "Confirm Save As"
															content: [
																Text { content: "File exists, overwrite with current project?" },
															]
															okText: "Yes"
															cancelText: "No"
															onOk: function() {
																saveAsProject(destination);
																confirmDialog.close();
															}
															onCancel: function() {confirmDialog.close();}
														}
									             }
							                   else{
							                      saveAsProject(destination);
							                   }
				                        }
				                    }
				                }
				                MenuItem {
				                	text: "Save and Close"
				                	action: function() {
				                		statMonitor.close();
				                		MainWindow.instance.projectCanvas.generateMiniatures(); 
				                		project.save();
				                		AppState.byName("MAIN").displayWorkspace();
				                	}
				                }
				                MenuItem {
				                    text: "Close"
				                    action: function() { 
				                    	statMonitor.close();
				                        AppState.byName("MAIN").displayWorkspace();
				                    }
				                }
								]
							}, RunController {
								canvas: bind project
							}, Label {
								layoutInfo: LayoutInfo {
									hgrow: Priority.ALWAYS
									hfill: true 
								}
							}, SeparatorButton {
								height: bind height;
							}, TrashHole {
							}, SeparatorButton {
								height: bind height;
							}, MenubarButton {
									shape: "M 1.00,14.40 L 3.83,14.40 3.83,9.02 1.00, 9.02 M 11.85,14.40 L 14.68,14.40 14.68,4.92 11.85, 4.92 M 6.43,14.40 L 9.26,14.40 9.26,1.00 6.43, 1.00 Z"
									tooltip: Tooltip { text: ##[STAT_MONITOR]"Statistics Window" }
									action: function():Void { 
										statMonitor.show();
									 }
				         	}, showNotesButton = MenubarToggleButton {
								shape: "M 0,0 L 0,8 5,8 11,12 9,8 13,8 13,0 Z"
								tooltip: Tooltip { text: ##[TOGGLE_NOTES]"Toggle note visibility" }
								selected: showNotes
							}, MenubarButton {
								shape: "M0,0 L0,12 10,12, 10,0 0,0 M4,13 L4,16 14,16 14,4 11,4 11,13 4,13"
								tooltip: Tooltip { text: ##[SUMMARY]"Summary Report" }
								action: function() {
									if(summaryEnabled) {
										def summary = SummaryReport{ select: project.getLabel(), summary:project.getSummary() };
										summary.show();
									} else {
										println("No summary exists!");
									}
								}
								disable: bind not summaryEnabled
							}, MenubarButton {
								shape: "M14.00,12.06 L7.50,5.59 C7.74,5.08 7.88,4.53 7.88,3.93 C7.88,1.76 6.12,0.00 3.94,0.00 C3.36,0.00 2.80,0.14 2.31,0.36 L4.83,2.88 L2.89,4.82 L0.36,2.30 C0.13,2.80 -0.00,3.35 -0.00,3.93 C-0.00,6.10 1.76,7.86 3.94,7.86 C4.52,7.86 5.06,7.73 5.55,7.51 L12.06,14.00 Z"
								tooltip: Tooltip { text: ##[SETTINGS]"Settings" }
								action: function():Void { new ProjectSettingsDialog().show(project) }
							}, MenubarButton {
								shape: "M2.46,10.21 C2.46,9.69 2.49,9.23 2.54,8.83 C2.59,8.43 2.69,8.07 2.82,7.75 C2.95,7.43 3.12,7.15 3.34,6.89 C3.55,6.63 3.82,6.39 4.15,6.15 C4.44,5.93 4.70,5.73 4.92,5.55 C5.14,5.36 5.32,5.18 5.47,4.99 C5.62,4.81 5.73,4.62 5.80,4.43 C5.87,4.25 5.91,4.03 5.91,3.80 C5.91,3.57 5.86,3.37 5.77,3.18 C5.67,2.99 5.54,2.82 5.36,2.68 C5.19,2.55 4.98,2.44 4.73,2.36 C4.48,2.29 4.21,2.25 3.90,2.25 C3.57,2.25 3.26,2.28 2.96,2.32 C2.67,2.37 2.39,2.44 2.12,2.52 C1.84,2.60 1.58,2.69 1.33,2.80 C1.08,2.90 0.83,3.01 0.58,3.13 L-0.00,1.18 C0.22,1.05 0.49,0.91 0.79,0.77 C1.10,0.63 1.45,0.50 1.83,0.39 C2.22,0.28 2.64,0.19 3.09,0.11 C3.55,0.04 4.04,0.00 4.56,0.00 C5.21,0.00 5.80,0.08 6.33,0.25 C6.86,0.42 7.32,0.65 7.70,0.96 C8.08,1.27 8.38,1.64 8.59,2.08 C8.80,2.52 8.90,3.02 8.90,3.57 C8.90,4.07 8.82,4.52 8.66,4.91 C8.50,5.30 8.29,5.66 8.03,5.99 C7.77,6.31 7.49,6.61 7.17,6.88 C6.85,7.15 6.53,7.41 6.21,7.67 C6.04,7.83 5.90,7.98 5.77,8.13 C5.64,8.28 5.53,8.46 5.45,8.65 C5.37,8.84 5.30,9.06 5.26,9.31 C5.22,9.56 5.20,9.86 5.20,10.21 Z M2.48,11.85 L5.25,11.85 L5.25,14.00 L2.48,14.00 Z"
								tooltip: Tooltip { text: ##[HELP]"Help Page" }
								action: function():Void { openURL("http://www.loadui.org/interface/project-view.html") }
							}, MenubarButton {
								shape: "M14.00,2.00 L12.00,0.00 7.00,5.00 2.00,0.00 0.00,2.00 5.00,7.00 0.00,12.00 2.00,14.00 7.00,9.00 12.00,14.00 14.00,12.00 9.00,7.00 Z"
								tooltip: Tooltip { text: ##[CLOSE_PROJECT]"Close Project" }
								action: function():Void {
									statMonitor.close();
									AppState.byName("MAIN").displayWorkspace() }
				         	}, Label {
								layoutInfo: LayoutInfo {
									width: 10
								}
							}
						]
					}
				]
			}
		];
	}
	
	function saveAsProject(destination: File){
	    try {
	      MainWindow.instance.projectCanvas.generateMiniatures();
	      project.saveAs(destination);
			project.save();					
	    } catch(e:IOException) {
	        def warning:Dialog = Dialog {
	            title: "Warning!"
	            content: Text {
	                content: "Failed to Import Project, see log for more details!"
	            }
	            okText: "Ok"
	            onOk: function() {
	                warning.close();
	            }
	            noCancel: true
	         };
	     }
	}
}

class SummaryListener extends EventHandler {
	override function handleEvent( e:EventObject ) { 
    	var event = e as BaseEvent;
    	if(event.getKey().equals(CanvasItem.START_ACTION)){
    		runInFxThread( function(): Void { summaryEnabled = false } );
    	} 
    	else if(event.getKey().equals(CanvasItem.SUMMARY)){
    		runInFxThread( function(): Void { summaryEnabled = project.getSummary() != null } );
    	}
    	else if(event.getKey().equals(ModelItem.LABEL)){
			runInFxThread( function(): Void {
		 		if(event.getSource() instanceof ProjectItem){
		 			projectLabel = project.getLabel();
		 		}
			});
		}
		else if (event.getKey().equals(CounterHolder.COUNTER_RESET_ACTION)){
			runInFxThread( function(): Void { summaryEnabled = false } );
		} 
	}
}

class  XMLFileFilter extends javax.swing.filechooser.FileFilter {
        override public function accept(f:File):Boolean {
            f.getName().toLowerCase().endsWith(".xml") or f.isDirectory()
        }
        
        override public function getDescription():String {
            ".xml files";
        }
    }
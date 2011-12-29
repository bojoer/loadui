/*
*SoapUIProjectSelector.fx
*
*Created on dec 22, 2011, 15:16:59 em
*/

package com.eviware.loadui.fx.ui.layout.widgets;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.layout.LayoutComponentNode;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.form.fields.FileInputField;
import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.EventHandler;

import com.sun.javafx.scene.layout.Region;

import com.javafx.preview.control.PopupMenu;

import javafx.scene.text.Text;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.dialogs.*;
import com.eviware.loadui.fx.MainWindow;
import com.eviware.loadui.api.ui.WindowController;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.fx.statistics.StatisticsWindow;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;

import com.javafx.preview.control.CustomMenuItem;
import com.javafx.preview.control.MenuButton;
import com.javafx.preview.layout.Grid;
import com.javafx.preview.layout.Grid.row;

import java.util.EventObject;
import java.io.File;

import javafx.scene.control.Button;
import javafx.geometry.HPos;
import javafx.geometry.VPos;

import javafx.scene.input.MouseEvent;

public class SoapUIProjectSelector extends LayoutComponentNode, EventHandler {

	var menuButton:MenuButton;
	var item:PopupMenu;
	var projectLabel:Label;
	var testSuiteLabel:Label;
	var testCaseLabel:Label;
	var testSuiteSelector:SelectWidget;
	var testCaseSelector:SelectWidget;
	public-init var project:PropertyLayoutComponent;
	public-init var testSuite:PropertyLayoutComponent;
	public-init var testCase:PropertyLayoutComponent;
	
	var hbox:HBox;
	
	var button:Button;
	var projectSelectionDialog:ProjectSelectionDialog = ProjectSelectionDialog{};
	
	postinit {
		updateLabels();
		testCase.getProperty().getOwner().addEventListener( PropertyEvent.class, this );
	}
	
//	init {
//		item = PopupMenu {
//			styleClass: "execution-selector-menu-item"
//			layoutInfo: LayoutInfo { height: 160, width: 325 }
//			
//
//			items: VBox {
//				snapToPixel: true
//
////				onMouseExited: function(e:MouseEvent):Void {
////					if( e.x > boundsInLocal.maxX or e.x < boundsInLocal.minX or e.y < boundsInLocal.minY or e.y > boundsInLocal.maxY )
////						item.hide()
////					else
////						println("DO_NOT_EXIT!!!");
////				}
//
//				content: [
//					FormFieldWidget {
//						plc: project
//						field: FileInputField {
//							label: project.getLabel()
//							value: project.getProperty().getValue()
//							selectMode : FileInputField.FILES_ONLY
//						}
//					}
//					testSuiteSelector = SelectWidget {
//						plc: testSuite
//					}
//					testCaseSelector = SelectWidget {
//						plc: testCase
//					}
////					Region{ managed:false, width: 325, height: 160, style:"-fx-background-color: #ff0000;" }
//				]
//			} 
//		};
//	}

	init {
		hbox = HBox {
			spacing: 28
			content: [
				VBox {
					content: [
//						menuButton = MenuButton {
//							styleClass: bind if( menuButton.showing ) "menu-button-showing" else "menu-button"
//							text: "soapUI Project"
//							items: 
//								item
//							layoutInfo: LayoutInfo{ height: 18 }
//						}

button = Button {                text: "Select project"
                action: function() {
                    projectSelectionDialog.show();
                }
            }
						projectLabel = Label { text: "", layoutInfo: LayoutInfo{ height: 18 } }
					]	
				}
				VBox {
					content: [
						Label { text: "TestSuite", layoutInfo: LayoutInfo{ height: 18 } }
						testSuiteLabel = Label { text: "", layoutInfo: LayoutInfo{ height: 18 } }
					]	
				}
				VBox {
					content: [
						Label { text: "soapUI TestCase", layoutInfo: LayoutInfo{ height: 18 } }
						testCaseLabel = Label { text: "", layoutInfo: LayoutInfo{ height: 18 }, textOverrun: javafx.scene.control.OverrunStyle.ELLIPSES }
					]	
				}
			]
		}
	}
	
	override function create() {
		hbox
	}
	
	override function getPrefWidth( height:Float ) {
		menuButton.getPrefWidth(height)
	}
	
	override function getPrefHeight( width:Float ) {
		menuButton.getPrefHeight(width)
	}

	override function handleEvent( e:EventObject ) {
		def event = e as PropertyEvent;
		if( event.getEvent() == PropertyEvent.Event.VALUE ) {
			FxUtils.runInFxThread( function():Void {
				testSuiteSelector.disable = false;
				testCaseSelector.disable = false;
				
				def projectName = (project.getProperty().getValue() as File).getName() as String;
				testSuiteSelector.value = testSuite.getProperty().getValue() as String;
				testCaseSelector.value = testCase.getProperty().getValue() as String;
				updateLabels();
			} );
		}
	}
	
	function updateLabels()
	{
		def projectName = (project.getProperty().getValue() as File).getName() as String;
		if( projectName.length() > 4 )
			projectLabel.text = projectName.substring(0, projectName.length() - 4 );
		testSuiteLabel.text = testSuite.getProperty().getValue() as String;
		testCaseLabel.text = testCase.getProperty().getValue() as String;
	}
};



public class ProjectSelectionDialog {
	
	public-init var wc:WindowController;
    
    var dialog:Dialog;
    
    function show()
    {
    	wc.bringToFront();
    	dialog.show();
    }
    
    function generateDialog()
    {
    	  dialog = null;
    	  
    	  dialog = Dialog {
        		showPostInit: false
            title: "Select soapUI Project"
            content: [
            	FormFieldWidget {
						plc: project
						width: 250
						field: FileInputField {
							label: project.getLabel()
							value: project.getProperty().getValue()
							selectMode : FileInputField.FILES_ONLY
						}
					}
					testSuiteSelector = SelectWidget {
						plc: testSuite
						disable: project.getProperty().getValue() == null
					}
					testCaseSelector = SelectWidget {
						plc: testCase
						disable: project.getProperty().getValue() == null
					}
            ]
            noOk: true
            noCancel: true
//            onOk: function() {
//					dialog.close();
//            }
        }
    }
    
    init {
		generateDialog()
    }
};


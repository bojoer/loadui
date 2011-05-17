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
package com.eviware.loadui.fx.widgets.componet;

import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.awt.BorderLayout;
import java.util.EventObject;

import com.eviware.loadui.util.table.LTable;

import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.WidgetCreationException;
import com.eviware.loadui.api.layout.WidgetFactory;
import com.eviware.loadui.api.ui.table.LTableModel;
import javax.swing.table.TableModel;
import javafx.ext.swing.SwingComponent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.CustomNode;
import javafx.scene.Node;
import javafx.geometry.VPos;
import javafx.geometry.HPos;

import javafx.scene.control.Label;
import javafx.scene.shape.*;
import javafx.scene.paint.*;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;


import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.fx.MainWindow;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.AppState;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.ui.dialogs.Dialog;
import java.io.File;

import javax.swing.UIManager;
import org.jdesktop.swingx.plaf.nimbus.NimbusLookAndFeelAddons;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;


/**
* @author robert
*/

public class TableWidget extends VBox, EventHandler, TableModelListener {
	
	var table: LTable;
	var saveFile: FileInputField;
	public var model: LTableModel;
	
	var cb: CheckBox;
	var enabledInDistModeCb: CheckBox;
	
	def workspace: WorkspaceItem = bind MainWindow.instance.workspace on replace oldVal {
		oldVal.removeEventListener( BaseEvent.class, this );
		workspace.addEventListener( BaseEvent.class, this );
	}
	
	var distributedMode: Boolean = not workspace.isLocalMode();
	
	def inTestCase = bind AppState.byName("MAIN").state.startsWith( "testcase." );
	
	def componentDisabled = bind distributedMode and inTestCase and not enabledInDistModeCb.selected;
	
	init {
	   model.addTableModelListener(this);
		table = new LTable(model); 
		table.setAutoCreateColumnsFromModel(true);
		table.setVisibleRowCount(5);
		table.setHorizontalScrollEnabled(true);
		table.setSortable( true );
		table.setEditable( false );
		var scrollPane:JScrollPane = new JScrollPane(table);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setWheelScrollingEnabled(true);
		var panel = new JPanel( new BorderLayout() );
		panel.add( scrollPane, BorderLayout.CENTER );
		
		panel.setSize( new Dimension(600, 250));
		panel.setMaximumSize( new Dimension(600, 250));
		panel.setPreferredSize( new Dimension(600, 250));
		
		def node = SwingComponent.wrap( panel );
		node.blocksMouse = true;
		
		content = [
			node,
			HBox {
				spacing: 10
				vpos: VPos.CENTER
				nodeVPos: VPos.CENTER
				layoutInfo: LayoutInfo { vfill: false, hfill: true, vgrow: Priority.NEVER, hgrow: Priority.ALWAYS }
				content: [
					Button {
						layoutInfo: LayoutInfo { hfill: false, hgrow: Priority.NEVER }
						text: "Reset"
						action: function() {
							(model as LTableModel).reset();
						}
						disable: bind componentDisabled
					}, Button {
						layoutInfo: LayoutInfo { hfill: false, hgrow: Priority.NEVER }
						text: "Clear"
						action: function() {
							(model as LTableModel).clear();
						}
						disable: bind componentDisabled
					}, Button {
						layoutInfo: LayoutInfo { hfill: false, hgrow: Priority.NEVER }
						text: "Save"
						action: function() {
							def dialog:Dialog = Dialog {
							            title: "Save Table!"
							            content: Form {
								            formContent: [
								                LabelField { value: 'Choose where to save table log:'},
								            	saveFile = FileInputField{id: "saveLog", description: "Save TableLog"}
								            ]
							            }
							            okText: "Ok"
							            cancelText: "Cancel"
							            onOk: function() {
							            	var sf = (saveFile.value as File).getAbsolutePath();
							            	if ( not (sf.endsWith(".txt") or sf.endsWith(".csv")) ) {
							            		sf = "{sf}.csv"
							            	}	
											table.save(new File(sf));
											dialog.close();
							            }
							        }
						}
						disable: bind componentDisabled
					}, cb = CheckBox {
						layoutInfo: LayoutInfo { hfill: false, hgrow: Priority.NEVER }
						text: "Follow"
						onMouseClicked: function(e) {
							model.setFollow(cb.selected);   
						}
						disable: bind componentDisabled
					},	enabledInDistModeCb = CheckBox {
						layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS }
						hpos: HPos.RIGHT
						text: "Enable in distributed mode"
						onMouseClicked: function(e) {
							model.setEnabledInDistMode(enabledInDistModeCb.selected);
						}
						disable: bind not (distributedMode and inTestCase)
						visible: bind inTestCase
					}
				] 
			}
		];
		
		cb.selected = model.isFollow();
		enabledInDistModeCb.selected = model.isEnabledInDistMode();
		table.setAutoscroll(model.isFollow());
	}
	
	override function handleEvent( e:EventObject ) { 
		if(e.getSource() == MainWindow.instance.workspace){
			if( e instanceof PropertyEvent ) {
				def event = e as PropertyEvent;
				if( WorkspaceItem.LOCAL_MODE_PROPERTY == event.getProperty().getKey() ) {
					FxUtils.runInFxThread( function():Void {
						distributedMode = not workspace.isLocalMode();
					} );
				} 
			}
		} 
	}
	
	override function tableChanged(e: TableModelEvent): Void {
		cb.selected = model.isFollow();
		enabledInDistModeCb.selected = model.isEnabledInDistMode();
		table.setAutoscroll(model.isFollow());
	}
}

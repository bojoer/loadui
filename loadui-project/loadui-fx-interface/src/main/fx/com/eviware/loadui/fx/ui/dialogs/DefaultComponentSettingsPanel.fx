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
package com.eviware.loadui.fx.ui.dialogs;

import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Panel;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import com.eviware.loadui.api.ui.tabbedpane.SelectMode;
import com.eviware.loadui.api.model.*;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.fx.ui.tabs.*;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;
import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.dummy.*;
import com.eviware.loadui.fx.ui.layout.widgets.support.SelectSupport;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import net.miginfocom.layout.*;

import java.io.File;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import com.eviware.loadui.api.layout.SettingsLayoutContainer;
import com.eviware.loadui.api.ui.table.SettingsTableModel.PropertyProxy;

import java.util.HashMap;

import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.fx.ui.form.FormItem;
import com.eviware.loadui.api.layout.LabelLayoutComponent;
import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.api.layout.OptionsProvider;
import com.eviware.loadui.fx.StylesheetAware;
import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigLayout.*;
import javafx.geometry.VPos;

import com.eviware.loadui.api.layout.TableLayoutComponent;
import javax.swing.table.TableModel;

/**
 * @author robert
 * 
 * DefaultComponentSettingsPanel is panel witch enables a user to see and modify component connections and
 * properties.
 */

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.dialogs.DefaultComponentSettingsPanel" );

public class DefaultComponentSettingsPanel extends StylesheetAware {
	
	var propertyBuffer:HashMap= new HashMap();
	
	/*
	 * Component for which is this dialog.
	*/
	public-init var component:ComponentItem on replace {
		for( property in component.getContext().getProperties()[p | not (p.getKey() as String).startsWith("_")]) {
			propertyBuffer.put( property.getKey(), new PropertyProxy(property));
		}
	};
	
	var tabPanel:TabPanel;
	var settings:SettingsTableField;
	var title:String = component.getLabel();
	var dialogContent:Node = Label {
		text: "Modal Dialog !"
	};
							
	var tmp = bind settings.dataHashCode on replace {
        update();  
    }
    
    function update():Void {
        for( p in propertyBuffer.keySet() ) {
            for( tab in tabPanel.tabs ) {
                var form:Form = tab.content as Form;
                var field = form.getField(p as String);
                if ( field != null ) {
                    var pp = propertyBuffer.get(p) as PropertyProxy;
                    field.value = pp.getValue();
                }
            }
        } 
    }
	
	public function retrieveSettingsTabs(): Tab[] {
		var settingsTabCollection: Collection = component.getSettingsTabs();
		
		for (tab in settingsTabCollection){
			if(tab instanceof SettingsLayoutContainer) {
				var settingsTab: SettingsLayoutContainer = tab as SettingsLayoutContainer;
				
				Tab {
					label: settingsTab.getLabel();
					content: Form {
						singleColumn: true
						formContent: buildSettingsTabComponents(settingsTab.toArray(), 0)
					}
				}
			} else null
		}
	}
	
	public function buildSettingsTabComponents(componentArray: Object[], level: Integer): FormItem[] {
		for(component in componentArray){
			def c: LayoutComponent = component as LayoutComponent;
			
			if(c instanceof LabelLayoutComponent){
				LabelField {
					value: (c as LabelLayoutComponent).getLabel()
					styleClass: if(level == 0) "settingsTabGroup" else "settingsTabLabel"
					layoutInfo: LayoutInfo { 
						height: if(level == 0) 0 else 22
					}
					vpos: if(level == 0) VPos.TOP else VPos.BOTTOM 
				}
			} else if(c instanceof PropertyLayoutComponent) {
				var p: PropertyLayoutComponent = c as PropertyLayoutComponent;
				var key: String = p.getProperty().getKey();
				var pp = propertyBuffer.get(key) as PropertyProxy;
				def formField = if( p.has( OptionsProvider.OPTIONS ) ) {
					def combo = ComboBox { id: key, label: p.getLabel(), value: pp.getValue(), plc: p }
				} else Form.fieldForType( pp.getType(), key, p.getLabel(), pp.getValue() );
				formField.setOnValueChangedHandler(function (value: Object){
					pp.setValue(value);
					update();
				});
				
				formField;
			} else if(c instanceof TableLayoutComponent) {
				var p: TableLayoutComponent = c as TableLayoutComponent;
				Form.fieldForType(TableModel.class, p.getLabel(), p.getLabel(), p.getTableModel());
			} else if(c instanceof LayoutContainer) {
				buildSettingsTabComponents((c as LayoutContainer).toArray(), level + 1);
			} else null
		}
	}
	
	public var backgroundFill: Paint = Color.web("#dbdbdb");
	
	public var stripeFill: Paint = Color.rgb(178,178,178,0.2);
	
	public function show() {
	
		var sTable:Form = Form {
			formContent: settings = SettingsTableField {
				id: "settings"
				data: propertyBuffer
			}
		}
		
		var tabArray: Tab[] = retrieveSettingsTabs();
				
		var sTab: Tab = Tab { label: "Advanced", content: sTable};
		insert sTab into tabArray;
				
		def dialogRef: Dialog = Dialog {
			modal: true
			title: title
			showPostInit: true
			closable: true
			stripeVisible: true
			helpUrl: "http://www.loadui.org/interface/workspace-view.html"
			backgroundFill: bind backgroundFill
			stripeFill: bind stripeFill
			okText: "Save"
			width: 500
			height: 400
			content: javafx.scene.layout.Stack {
				content: [ 
					tabPanel = TabPanel {
						tabs: tabArray
					} 
				]
			}
			onOk: function() {
				for( property in component.getProperties()[p|not p.getKey().startsWith("_")] ) {
					var pp:PropertyProxy = propertyBuffer.get(property.getKey()) as PropertyProxy;
					if ( pp != null ) {
						(property as com.eviware.loadui.api.serialization.MutableValue).setValue( pp.getValue() );
					}
				} 
				dialogRef.close();
			}
		}
	}
}

class ComboBox extends SelectField, SelectSupport {
	public-init var plc:PropertyLayoutComponent on replace {
		setPlc( plc );
	}
}
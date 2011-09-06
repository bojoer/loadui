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
package com.eviware.loadui.fx.ui.dialogs;

import javafx.scene.Node;
import javafx.scene.control.Label;

import com.eviware.loadui.api.model.*;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.layout.*;
import com.eviware.loadui.api.ui.table.SettingsTableModel.PropertyProxy;

import com.eviware.loadui.fx.ui.tabs.TabDialog;
import com.eviware.loadui.fx.ui.tabs.Tab;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.SettingsLayoutContainerForm;
import com.eviware.loadui.fx.ui.form.fields.SettingsTableField;

import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * @author robert
 * 
 * DefaultComponentSettingsPanel is panel witch enables a user to see and modify component connections and
 * properties.
 */

public-read def log = LoggerFactory.getLogger( "com.eviware.loadui.fx.ui.dialogs.DefaultComponentSettingsPanel" );

public class DefaultComponentSettingsPanel {
	
	var propertyBuffer:HashMap= new HashMap();
	
	var tableModelBuffer: HashMap= new HashMap();
	
	/*
	 * Component for which is this dialog.
	*/
	public-init var component:ComponentItem on replace {
		for( property in component.getContext().getProperties()) {
			
			var propertyHasSettingsField:Boolean = false;
			
			for( tab in dialogRef.tabs ) {
         	var form:Form = tab.content as Form;
            var field = form.getField( property.getKey() );
            if ( field != null ) {
            	propertyHasSettingsField = true;
            	break;
            }
         }
			
			// invert this statement
			if( not property.getKey().charAt(0).equals('_') or propertyHasSettingsField )
			{
				propertyBuffer.put( property.getKey(), new PropertyProxy(property));
			}
			else
				log.debug("  property {property.getKey()} was skipped from propertyProxy");
		}
	};
	
	var dialogRef:TabDialog;
	
	var settings:SettingsTableField;
	var title:String = component.getLabel();
	var dialogContent = Label {
		text: "Modal Dialog !"
	};
							
	var tmp = bind settings.dataHashCode on replace {
        update();  
    }
    
    function update():Void {
        for( p in propertyBuffer.keySet() ) {
            for( tab in dialogRef.tabs ) {
                var form:Form = tab.content as Form;
                var field = form.getField(p as String);
                if ( field != null ) {
                    var pp = propertyBuffer.get(p) as PropertyProxy;
                    field.value = pp.getValue();
                    update();
                }
            }
        } 
    }
	
	public function retrieveSettingsTabs(): Tab[] {
		var settingsTabCollection = component.getSettingsTabs();
		
		for (tab in settingsTabCollection){
			if(tab instanceof SettingsLayoutContainer) {
				var settingsTab: SettingsLayoutContainer = tab as SettingsLayoutContainer;
				Tab {
					label: settingsTab.getLabel();
					content: SettingsLayoutContainerForm {
						singleColumn: true
						container: settingsTab
						onFieldChanged: function( field ):Void {
							def property = propertyBuffer.get( (field as Node).id ) as PropertyProxy;
							property.setValue( field.value );
						}
					}
				}
			} else null
		}
	}
	
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
				
		dialogRef = TabDialog {
			title: title
			width: 470
			height: 300
			subtitle: "Settings"
			helpUrl: component.getHelpUrl()
			okText: "Save"
			tabs: tabArray
			onOk: function() {
				for( tab in dialogRef.tabs[x|x.content instanceof SettingsLayoutContainerForm] ) {
					def form = (tab.content as SettingsLayoutContainerForm);
					form.commit();
				}
				for( property in component.getProperties()) {
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
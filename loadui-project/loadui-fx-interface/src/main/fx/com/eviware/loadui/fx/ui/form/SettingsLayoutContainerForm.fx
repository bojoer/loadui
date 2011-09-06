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
package com.eviware.loadui.fx.ui.form;

import com.eviware.loadui.api.layout.*;
import com.eviware.loadui.api.ui.table.StringToStringTableModel;

import com.eviware.loadui.fx.ui.form.fields.*;
import com.eviware.loadui.fx.ui.layout.widgets.support.SelectSupport;

import javax.swing.table.TableModel;

public class SettingsLayoutContainerForm extends Form {	
	public-read var label:String;
	
	public var container:SettingsLayoutContainer on replace {
		label = container.getLabel();
		formContent = for( component in container ) buildFormItems( component );
		for( formItem in formContent ) {
			for( formField in formItem.fields ) {
				formField.onValueChanged = function( value ):Void {
					onFieldChanged( formField );
				}
			}
		}
	}
	
	public var onFieldChanged: function( field:FormField ):Void;
	
	public function commit():Void {
		for( component in container ) updateProperties( component );
	}
	
	function updateProperties( component:LayoutComponent ):Void {
		if( component instanceof PropertyLayoutComponent ) {
			def plc = component as PropertyLayoutComponent;
			def property = plc.getProperty();
			
			property.setValue( getField( property.getKey() ).value );
		} else if( component instanceof LayoutContainer ) {
			for( subComponent in (component as LayoutContainer) ) updateProperties( subComponent );
		}
	}
	
	function buildFormItems( component:LayoutComponent ):FormItem[] {
		if( component instanceof LabelLayoutComponent ) {
			LabelField {
				value: (component as LabelLayoutComponent).getLabel()
			}
		} else if( component instanceof PropertyLayoutComponent ) {
			def plc = component as PropertyLayoutComponent;
			def property = plc.getProperty();
			
			if( plc.has( OptionsProvider.OPTIONS ) ) {
				ComboBox { id: property.getKey(), label: plc.getLabel(), value: property.getValue(), plc: plc }
			} else if(plc.has("widget") and plc.get("widget").equals("password")) {
				PasswordField { id: property.getKey(), label: plc.getLabel(), value: property.getValue() }
			} else { 
				Form.fieldForType( property.getType(), property.getKey(), plc.getLabel(), property.getValue() );
			}

		} else if( component instanceof TableLayoutComponent ) {
			def tlc = component as TableLayoutComponent;
			
			Form.fieldForType( TableModel.class, tlc.getLabel(), tlc.getLabel(), tlc.getTableModel() );
		} else if( component instanceof LayoutContainer ) {
			for( subComponent in (component as LayoutContainer) ) buildFormItems( subComponent );
		} else null
	}
}

class ComboBox extends SelectField, SelectSupport {
	public-init var plc:PropertyLayoutComponent on replace {
		setPlc( plc );
	}
}
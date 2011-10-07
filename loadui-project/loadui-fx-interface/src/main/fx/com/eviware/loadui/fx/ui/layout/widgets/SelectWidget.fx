/* 
 * Copyright 2011 SmartBear Software
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
*SelectWidget.fx
*
*Created on mar 26, 2010, 13:38:03 em
*/

package com.eviware.loadui.fx.ui.layout.widgets;

import com.eviware.loadui.fx.ui.layout.widgets.support.SelectSupport;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;
import com.eviware.loadui.fx.ui.layout.Widget;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.fields.SelectField;

import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.api.layout.OptionsProvider;
import com.eviware.loadui.impl.layout.OptionsProviderImpl;

import java.lang.Iterable;
import java.lang.Runnable;

public class SelectWidget extends FormFieldWidget, SelectSupport {	
	var selectField:SelectField;
	
	override var plc on replace {
		setPlc( plc );
		value = plc.getProperty().getValue();
		if(not FX.isInitialized(selectField)) {
			selectField = SelectField {
				value: value
				options: bind options
				labelProvider: function( o:Object ):String { optionsProvider.labelFor( o ) }
			}
		}
	}
	
	override var field = bind selectField;
}

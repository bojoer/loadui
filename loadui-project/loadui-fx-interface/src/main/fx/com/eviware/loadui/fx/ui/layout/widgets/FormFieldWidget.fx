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
*FormFieldWidget.fx
*
*Created on mar 22, 2010, 12:31:14 em
*/

package com.eviware.loadui.fx.ui.layout.widgets;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.layout.Panel;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Container;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.geometry.BoundingBox;
import javafx.util.Math;

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.popup.TooltipHolder;
import com.eviware.loadui.fx.ui.layout.Widget;
import com.eviware.loadui.fx.ui.form.Form;
import com.eviware.loadui.fx.ui.form.FormField;

import com.eviware.loadui.api.layout.PropertyLayoutComponent;

import java.lang.Iterable;

public class FormFieldWidget extends VBox, Widget {
	//override var tooltip = bind lazy "{label}: {value}";
	
	public-init protected var field:FormField = Form.fieldForType( property.getType(), "", label, property.getValue() ) on replace {
		(field as Node).layoutInfo = LayoutInfo {
			width: bind Math.max(width, 100)
		}
	}
	
	override var value on replace { field.value = value }
	def fieldValue = bind field.value on replace { plc.getProperty().setValue( fieldValue ) }
	
	init {
		content = [ if( not field.skipLabel ) Label { text: bind label } else null, field as Node ];
	}
}

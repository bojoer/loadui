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
*LayoutComponentNode.fx
*
*Created on mar 17, 2010, 11:47:44 fm
*/

package com.eviware.loadui.fx.ui.layout;

import javafx.scene.Node;
import javafx.scene.layout.Resizable;

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.osgi.WidgetRegistry;
import com.eviware.loadui.fx.ui.layout.widgets.FormattedStringLabel;
import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.fx.ui.layout.widgets.SoapUIProjectSelector;

import com.eviware.loadui.api.layout.*;
import com.eviware.loadui.util.layout.FormattedString;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import javax.swing.JComponent;
/**
 * 
 */
public function buildLayoutComponentNode( layoutComponent:LayoutComponent ):LayoutComponentNode {
	if( layoutComponent.has("widget") ) {
		
		def widget = WidgetRegistry.instance.buildWidget( layoutComponent );
		if( widget instanceof LayoutComponentNode ) widget as LayoutComponentNode
		else WidgetLayoutComponentNode { widget: widget }
	} else if( layoutComponent.has("fString") ) {
		WidgetLayoutComponentNode {
			widget: FormattedStringLabel {
				text: layoutComponent.get("label") as String
				formattedString: layoutComponent.get("fString") as FormattedString
			}
		}
	} else if( layoutComponent.has("component") ) {
		def swingComponent = layoutComponent.get("component") as JComponent;
//		println("swingComponent: {swingComponent} model: {(swingComponent as javax.swing.JScrollPane).getModel()}");
		SwingLayoutComponentNode {
			component: swingComponent
			fixedHeight: if( layoutComponent.has("componentHeight") ) layoutComponent.get("componentHeight") as Number else -1
			fixedWidth: if( layoutComponent.has("componentWidth") ) layoutComponent.get("componentWidth") as Number else -1
		}
	} else if( layoutComponent.has("soapUIProject") ) {
		SoapUIProjectSelector {
			project: layoutComponent.get("soapUIProject") as PropertyLayoutComponent
			testSuite: layoutComponent.get("testSuite") as PropertyLayoutComponent
			testCase: layoutComponent.get("testCase") as PropertyLayoutComponent
		}
	} else if( layoutComponent instanceof LayoutContainer ) {
		LayoutContainerNode { layoutComponent: layoutComponent }
	} else if( layoutComponent instanceof PropertyLayoutComponent ) {
		PropertyLayoutComponentNode { layoutComponent: layoutComponent }
	} else if( layoutComponent instanceof ActionLayoutComponent ) {
		ActionLayoutComponentNode { layoutComponent: layoutComponent }
	} else if( layoutComponent instanceof SeparatorLayoutComponent ) {
		SeparatorLayoutComponentNode { layoutComponent: layoutComponent }
	} else if( layoutComponent instanceof LabelLayoutComponent ) {
		LabelLayoutComponentNode { layoutComponent: layoutComponent }
	} else {
		null
	}
}

public abstract class LayoutComponentNode extends BaseNode, Resizable {
	public-init var layoutComponent:LayoutComponent;
	
	public function release():Void { 
		layoutComponent = null;
	}
}

class WidgetLayoutComponentNode extends LayoutComponentNode {
	public-init var widget:Node;
	
	def resizable = bind if(widget instanceof Resizable) widget as Resizable else null;
	
	override var width on replace {
		resizable.width = width;
	}
	
	override var height on replace {
		resizable.height = height;
	}
	
	override function create() {
		widget;
	}
	
	override function getPrefWidth( height:Float ) {
		if( resizable != null ) resizable.getPrefWidth( height )
		else width
	}
	
	override function getPrefHeight( width:Float ) {
		if( resizable != null ) resizable.getPrefHeight( width )
		else height
	}
	
	override function release() {
		super.release();
		widget = null;
	}
}

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
*LayoutContainerNode.fx
*
*Created on mar 17, 2010, 11:57:40 fm
*/

package com.eviware.loadui.fx.ui.layout;

import javafx.scene.layout.Resizable;

import org.jfxtras.scene.layout.XMigLayout;
import org.jfxtras.scene.layout.XMigNodeLayoutInfo;

import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.LayoutContainer;

public class LayoutContainerNode extends LayoutComponentNode {
	//public def layoutContainer:LayoutContainer = bind layoutComponent as LayoutContainer;
	
	override var layoutBounds = bind lazy node.layoutBounds;
	override var width on replace {
		node.width = width;
		node.requestLayout();
	}
	override var height on replace {
		node.height = height;
		node.requestLayout();
	}
	
	var node:XMigLayout;
	override function create() {
		def layoutContainer = layoutComponent as LayoutContainer;
		node = XMigLayout {
			constraints: layoutContainer.getLayoutConstraints()
			columns: layoutContainer.getColumnConstraints()
			rows: layoutContainer.getRowConstraints()
			content: for( layoutComponent in layoutContainer ) {
				def component = LayoutComponentNode.buildLayoutComponentNode( layoutComponent as LayoutComponent );
				if( component.layoutInfo instanceof XMigNodeLayoutInfo )
					component
				else
					XMigLayout.migNode( component, layoutComponent.getConstraints() )
			}
		}
	}
	
	override function getPrefHeight( width:Float ) {
		node.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Float ) {
		node.getPrefWidth( height )
	}
	
	override function release() {
		for( child in node.content[n|n instanceof LayoutComponentNode] )
			(child as LayoutComponentNode).release();
		node.content = [];
		node = null;
		super.release();
	}
}

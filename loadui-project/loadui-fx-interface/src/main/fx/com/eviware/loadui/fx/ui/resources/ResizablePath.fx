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

/*
*ResizablePath.fx
*
*Created on aug 10, 2010, 15:02:07 em
*/

package com.eviware.loadui.fx.ui.resources;

import javafx.scene.layout.Resizable;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.geometry.BoundingBox;

public abstract class ResizablePath extends Resizable, Path {
	
	init {
		elements = calculatePath();
	}
	
	protected function calculatePath():PathElement[] { [] }; //This should be abstract, but due to a compiler bug, sometimes subclasses refuse to compile.
	
	override var width on replace {
		elements = calculatePath();
	}
	
	override var height on replace {
		elements = calculatePath();
	}
	
	override var layoutBounds = bind BoundingBox {
		minX: 0 
		minY: 0
		width: width
		height: height
	}
	
	override function getPrefHeight( width:Number ) {
		-1
	}
	
	override function getPrefWidth( height:Number ) {
		-1
	}
}
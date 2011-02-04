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
*MenuArrow.fx
*
*Created on feb 17, 2010, 09:19:10 fm
*/

package com.eviware.loadui.fx.ui.resources;

import javafx.scene.CustomNode;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class MenuArrow extends CustomNode {
	
	public var fill:Paint = Color.BLACK;

	override function create() {
		Polygon {
			fill: bind fill
			points: [
				-2.5, -2.5,
				2.5, 0,
				-2.5, 2.5
			]
		}
	}
}

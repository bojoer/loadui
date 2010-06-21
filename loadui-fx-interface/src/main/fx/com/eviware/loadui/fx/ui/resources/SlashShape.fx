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
*PlayShape.fx
*
*Created on apr 22, 2010, 12:45:38 em
*/

package com.eviware.loadui.fx.ui.resources;

import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;

public class SlashShape extends Path {
	override var stroke = null;

	init {
		recalculateShape();
	}

	function recalculateShape() {
		elements = [
			MoveTo { x: width/2 },
			LineTo { y: height },
			LineTo { x: width/2, y: height },
			LineTo { x: width },
			LineTo { x: width/2 }
		];
	}

	public var width:Number on replace {
		recalculateShape();
	}

	public var height:Number on replace {
		recalculateShape();
	}
}

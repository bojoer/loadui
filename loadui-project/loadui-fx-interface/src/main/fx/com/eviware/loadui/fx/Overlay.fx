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
package com.eviware.loadui.fx;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.util.Sequences;

/**
 * An overlay layer.
 *
 * @author dain.nilsson
 */
public class Overlay {
	def dummyNode = Rectangle { id: "dummy", fill: Color.rgb(0,0,0,0.01), width: 1, height: 1 };
	
	public-init var group:Group;
	
	public var content:Node[] on replace {
		def dummyIndex = Sequences.indexOf( group.content, dummyNode );
		if( dummyIndex == -1 ) {
			group.content = [ dummyNode, content ];
		} else {
			for( i in Sequences.reverse([0..(sizeof group.content - 1)]) as Integer[] ) {
				if( i != dummyIndex ) delete group.content[i];
			}
			insert content into group.content;
		}
	}
}
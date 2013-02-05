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
*XWipePanel.fx
*
*Created on maj 11, 2010, 09:55:48 fm
*/

package com.eviware.loadui.fx.ui;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.layout.Resizable;

public class XWipePanel extends Group {
	public function next( node:Node ):Void {
		content = node;
		action();
	}
	
	public var wipe:Object;
	
	public var action: function():Void;
	
	public var width:Number;
	public var height:Number;
}

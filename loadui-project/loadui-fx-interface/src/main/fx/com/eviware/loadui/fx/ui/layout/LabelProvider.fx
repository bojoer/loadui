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
*LabelProvider.fx
*
*Created on apr 19, 2010, 10:34:00 fm
*/

package com.eviware.loadui.fx.ui.layout;

import javafx.scene.control.Label;

public def DEFAULT_LABEL_PROVIDER = DefaultLabelProvider {};

/**
 * Provides Labels for a number of objects.
 * The DefaultLabelProvider simply creates Labels with the arguments toString() text content.
 *
 * @authod dain.nilsson
 */
public mixin class LabelProvider {
	public abstract function labelFor( o:Object ):Label;
}

class DefaultLabelProvider extends LabelProvider {
	override function labelFor( o:Object ) {
		Label { text: o.toString() }
	}
}

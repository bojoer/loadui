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
*Selectable.fx
*
*Created on mar 1, 2010, 16:41:05 em
*/

package com.eviware.loadui.fx.widgets.canvas;

import javafx.scene.Node;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;

/**
 * The currently selected Selectable.
 */
public-read var current:Selectable;

/**
 * De-selects any selected Selectable;
 */
public function selectNone():Void {
	current = null;
}

public def effect:Effect = Glow { level: 0.5 };

/**
 * A mixin class that allows a Node to be selectable. Only a single Node may be selected at one time.
 * The currently selected Selectable can be found using the script level variable "current".
 *
 * @author dain.nilsson
 */
public mixin class Selectable {
	/**
	 * True if this Selectable is selected, false if not.
	 */
	public-read def selected = bind current == this;
	
	/**
	 * Selects this Selectable, deselecting any other Selectable which is already selected.
	 */
	public function select() {
		current = this;
		if( this instanceof Node )
			(this as Node).requestFocus();
	}
	
	/**
	 * De-selects this Selectable, if it is currently selected.
	 */
	public function deselect() {
		if( selected )
			current = null;
	}
}

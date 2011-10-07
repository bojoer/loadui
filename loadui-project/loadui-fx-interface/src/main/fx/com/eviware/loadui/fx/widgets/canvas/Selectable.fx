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
*Selectable.fx
*
*Created on mar 1, 2010, 16:41:05 em
*/

package com.eviware.loadui.fx.widgets.canvas;

import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.BaseNode.*;
import com.eviware.loadui.fx.ui.node.Deletable;

import javafx.scene.Node;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.util.Sequences;

/**
 * The currently selected Selectables.
 */
public-read var selects:Selectable[];

/**
 * De-selects any selected Selectable;
 */
public function selectNone():Void {
	selects = [];
}

public def effect:Effect = Glow { level: 0.5 };

/**
 * A mixin class that allows a Node to be selectable. Multiple Nodes may be selected at one time.
 * The currently selected Selectables can be found using the script level variable "selects".
 *
 * @author dain.nilsson
 */
public mixin class Selectable {
	
	postinit {
		if( this instanceof BaseNode ) {
			def basenode = this as BaseNode;
			
			basenode.addKeyHandler( KEY_PRESSED, function( e:KeyEvent ) {
				if( e.code == KeyCode.VK_DELETE ) {
					Deletable.deleteObjects( for( deletable in selects[s|s instanceof Deletable] ) deletable as Deletable, function():Void { selectNone(); } );
				}
			} );
			
			basenode.addKeyHandler( KEY_PRESSED, function( e:KeyEvent ) {
				if( e.code == KeyCode.VK_ESCAPE ) {
					selectNone();
				}
			} );
		}
	}
	
	/**
	 * True if this Selectable is selected, false if not.
	 */
	public-read var selected = false;
	
	def _selects = bind selects on replace { selected = Sequences.indexOf( selects, this ) >= 0 }
	
	/**
	 * Selects this Selectable, keeping any other Selectable which is already selected in the selection.
	 */
	public function select() {
		insert this into selects;
		if( this instanceof Node )
			(this as Node).requestFocus();
	}
	
	/**
	 * Selects this Selectable, deselecting any other Selectable which is already selected.
	 */
	public function selectOnly() {
		selects = this;
		if( this instanceof Node )
			(this as Node).requestFocus();
	}
	
	/**
	 * De-selects this Selectable, if it is currently selected.
	 */
	public function deselect() {
		if( selected )
			delete this from selects;
	}
}

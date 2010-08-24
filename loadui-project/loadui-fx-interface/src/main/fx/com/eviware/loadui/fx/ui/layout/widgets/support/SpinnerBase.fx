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
package com.eviware.loadui.fx.ui.layout.widgets.support;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.control.Button;
import javafx.scene.control.TextBox;

import com.sun.javafx.scene.layout.Region;

import java.lang.RuntimeException;

public abstract class SpinnerBase extends HBox {
	
	public var columns:Integer = 4 on replace {
		textBox.columns = columns;
	}
	
	public var selectOnFocus:Boolean on replace {
		textBox.selectOnFocus = selectOnFocus;
	}
	
	public var value:Object on replace oldVal {
		def newValue = clean( value );
		if( value != newValue )
			throw new RuntimeException( "Illegal value for Spinner set: {newValue}" );
		
		textBox.text = textFromValue( value );
	}
	
	override var styleClass = "spinner";
	
	protected function valueFromText( string:String ):Object { string }
	
	protected function textFromValue( value:Object ):String { "{value}" }
	
	protected function nextValue():Object { null }
	
	protected function prevValue():Object { null }
	
	protected function clean( newValue:Object ):Object { newValue }
	
	def textBox:TextBox = TextBox {
		layoutInfo: LayoutInfo { vfill: false, hfill: true, width: 30 }
		text: textFromValue( value );
	}
	def textBoxText = bind textBox.text on replace {
		def newVal = clean( valueFromText( textBoxText ) );
		if( value != newVal ) {
			value = newVal;
		} else {
			textBox.text = textFromValue( value );
		}
	}
	
	init {
		content = [
			textBox, VBox {
				content: [
					Button {
						styleClass: "up-button"
						focusTraversable: false
						graphic: Region { styleClass: "arrow" }
						action: function():Void {
							textBox.commit();
							textBox.requestFocus();
							value = clean( nextValue() );
						}
					}, Button {
						styleClass: "down-button"
						focusTraversable: false
						graphic: Region { styleClass: "arrow" }
						action: function():Void {
							textBox.commit();
							textBox.requestFocus();
							value = clean( prevValue() );
						}
					}
				]
			}
		];
	}
}
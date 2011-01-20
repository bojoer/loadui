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
*FormField.fx
*
*Created on feb 22, 2010, 12:45:31 em
*/

package com.eviware.loadui.fx.ui.form;

import javafx.scene.layout.Resizable;


/**
 * Base class for different types of form fields.
 * A FormField has a label, a description, and a value, which can be changed.
 * The FormField itself, is a Node which allows the user to view and modify the value.
 */
public mixin class FormField extends FormItem, Resizable {

	public var onValueChanged: function(value: Object);
	
	/**
	 * The value of the FormField.
	 */
	public var value:Object on replace {
		onValueChanged( value );
	}
	
	/**
	 * The label for the FormField.
	 */
	public-init protected var label:String;
	
	/**
	 * The description for the FormField.
	 */
	public-init protected var description:String;
	
	/**
	 * If set to true, this FormField provides its own label, and the label should not be rendered by the form.
	 */
	public-read protected var skipLabel = false;
	
	postinit {
		fields = this;
	}
	
	
}

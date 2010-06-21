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
package com.eviware.loadui.api.ui.dialogs;

import com.eviware.loadui.api.ui.dialogs.items.DialogItemInterface;

import com.eviware.loadui.api.ui.dialogs.items.*;

/**
 * Interface for defining an dialog structure.
 * 
 * @author robert
 *
 */
public interface DialogInterface {

	/**
	 * Add a text field with label on left.
	 * 
	 * @param id 	component id
	 * @param label		label
	 * @param enable 
	 */
	void addTextField(String id, String label, boolean enable);
	
	/**
	 * Add a checkbox with a label on left. 
	 * 
	 * @param id	component id
	 * @param label		label
	 * @param enable
	 */
	CheckBox addCheckBox(String id, String label, boolean enable);
	/**
	 * Add a filechooser. It has label on left, textbox in middle and a "Browse" button on left.
	 * 
	 * @param id
	 * @param label
	 * @param enable
	 */
	void addFileField(String id, String label, boolean enable);
	
	/**
	 * Add a horizontal separator (line)
	 *
	 * @param id
	 */
	void addSeparator(String id);
	
	/**
	 * Add a label
	 * 
	 * @param id
	 * @param label
	 */
	void addLabel(String id, String label);
	
	/**
	 * Add a Tab panel
	 * 
	 * @param id
	 */
	void addTabedPanel(String id, DialogItemInterface panel);
	
	/**
	 * Returns if element is enabled, with given id.
	 * @param id
	 * @return
	 */
	boolean isEnabled(String id);
	
	/**
	 * Enable/disable element
	 * 
	 * @param id
	 * @param enable
	 */
	void setEnable(String id, boolean enable);
	
	
	
}

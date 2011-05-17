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
*Tab.fx
*
*Created on feb 23, 2010, 10:57:06 fm
*/

package com.eviware.loadui.fx.ui.tabs;

import javafx.scene.Node;

/**
 * A tab in a TabDialog.
 *
 * @author dain.nilsson
 */
public class Tab {
	/**
	 * The label for the tab.
	 */
	public var label:String;
	
	/**
	 * The contents of the tab.
	 */
	public var content:Node;
	
	/**
	 * Optional callback which is called whenever the tab gets selected.
	 */
	public var onSelect:function():Void;
	
	/**
	 * Whether the tab is currently selected or not.
	 */
	public-read package var selected = false;
	
	/**
	 * The parent TabDialog containing this tab.
	 */
	public-read package var panel:TabDialog;
	
	/**
	 * Callback when tab lost focus.
	 */
	public var onUnselect:function():Void;
}

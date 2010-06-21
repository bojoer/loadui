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
package com.eviware.loadui.api.ui.tabbedpane;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.eviware.loadui.api.ui.dialogs.DialogModel;
import com.eviware.loadui.api.ui.dialogs.items.DialogItem;
import com.eviware.loadui.api.ui.dialogs.items.DialogItemType;

public class TabbedPane extends DialogItem {

	private HashMap<String, DialogModel> tabs = new HashMap<String, DialogModel>();
	
	public TabbedPane(String id, String label, boolean enabled) {
		super(id, label, DialogItemType.TABPANEL, enabled);
	}

	@Override
	public Object getValue() {
		return this;
	}
	
	public void addTab(DialogModel panel) {
		tabs.put(panel.getId(), panel);
	}

	public Iterable<DialogModel> getTabs() {
		return tabs.values();
	}
	
	public HashMap<String, DialogModel> getItems() {
		return tabs;
	}
}

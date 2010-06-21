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

import java.util.HashMap;

import com.eviware.loadui.api.ui.dialogs.items.*;

public class DialogModel implements DialogInterface, DialogItemInterface {

	private String title;
	protected HashMap<String, DialogItemInterface> items = new HashMap<String, DialogItemInterface>();
	private String id;
	private boolean enabled = true;
	
	public DialogModel(String id, String title) {
		this.id = id;
		this.title = title;
	}
	
	@Override
	public CheckBox addCheckBox(String id, String label, boolean enable) {
		items.put(id, new CheckBox(id, label, enable));
		return (CheckBox)items.get(id);
	}

	@Override
	public void addFileField(String id, String label, boolean enable) {
		items.put(id, new FileField(id, label, enable));
	}

	@Override
	public void addLabel(String id, String label) {
		items.put(id, new Label(id, label, enabled));
	}

	@Override
	public void addSeparator(String id) {
		items.put(id, new Separator(id));
	}

	@Override
	public void addTabedPanel(String id, DialogItemInterface panel) {
		items.put(id, panel);
	}

	@Override
	public void addTextField(String id, String label, boolean enable) {
		items.put(id, new TextField(id, label, enable));
	}

	@Override
	public boolean isEnabled(String id) {
		return items.get(id).isEnabled();
	}

	@Override
	public void setEnable(String id, boolean enable) {
		items.get(id).setEnabled(enable);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return title;
	}

	@Override
	public DialogItemType getType() {
		return DialogItemType.PANEL;
	}

	@Override
	public Object getValue() {
		return this;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public HashMap<String, DialogItemInterface> getItems() {
		return items;
	}
	
	public Iterable<DialogItemInterface> getItemList() {
		return items.values();
	}
}

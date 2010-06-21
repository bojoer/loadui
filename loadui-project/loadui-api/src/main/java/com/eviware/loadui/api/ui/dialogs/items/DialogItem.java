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
package com.eviware.loadui.api.ui.dialogs.items;


public abstract class DialogItem implements DialogItemInterface {

	private String id;
	protected String label;
	private DialogItemType type;
	private boolean enabled;

	public DialogItem(String id, String label, DialogItemType type, boolean enabled) {
		this.id = id;
		this.label = label;
		this.type = type;
		this.enabled = enabled;
	}

	
	/* (non-Javadoc)
	 * @see com.eviware.loadui.api.dialogs.DialogItemInterface#getId()
	 */
	public String getId() {
		return id;
	}


	/* (non-Javadoc)
	 * @see com.eviware.loadui.api.dialogs.DialogItemInterface#getType()
	 */
	public DialogItemType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see com.eviware.loadui.api.dialogs.DialogItemInterface#getLabel()
	 */
	public String getLabel() {
		return label;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}

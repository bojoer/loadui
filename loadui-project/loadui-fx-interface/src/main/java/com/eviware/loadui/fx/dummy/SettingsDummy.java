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
package com.eviware.loadui.fx.dummy;

import com.eviware.loadui.api.ui.dialogs.DialogModel;
import com.eviware.loadui.api.ui.dialogs.items.CheckBox;
import com.eviware.loadui.api.ui.dialogs.items.DialogItemInterface;
import com.eviware.loadui.api.ui.tabbedpane.TabbedPane;
import com.eviware.loadui.api.ui.dialogs.items.CheckBox;

public class SettingsDummy {

	public static DialogModel dialog; 
	public static CheckBox cb;
	
	private static SettingsDummy __instance = null;
	public static CheckBox visible;
	
	public static SettingsDummy getInstance() {
		if (__instance == null )
			__instance = new SettingsDummy();
		return __instance;
	}
	private SettingsDummy() {
		dialog = new DialogModel("xx", "Settings");
		TabbedPane tabpanel = new TabbedPane("Tab panel", "Tab pannel", true);
		DialogModel tabone = new DialogModel("Tab1", "tAB1");
		tabone.addLabel("lab",  "Label one");
		CheckBox cb = tabone.addCheckBox("chc", "Are you happy", true);
		visible = tabone.addCheckBox("toolbar", "Toolbar visible", true);
		DialogModel tabtwo = new DialogModel("Tab2", "tAB2");
		tabtwo.addLabel("lab",  "Label two");
		tabtwo.addTextField("fix", "Do you need fix?", true);
		tabpanel.addTab(tabone);
		tabpanel.addTab(tabtwo);
		dialog.addLabel("setLab", "Settings");
		dialog.addTabedPanel("xx", tabpanel);
	}
	
}

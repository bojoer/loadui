package com.eviware.loadui.ui.fx.views.canvas;

import javafx.scene.Node;

import com.eviware.loadui.ui.fx.control.SettingsDialog;
import com.eviware.loadui.ui.fx.control.SettingsDialog.SettingsTab;
import com.eviware.loadui.ui.fx.control.SettingsDialog.SettingsTabBuilder;
import com.google.common.collect.Lists;

public class LimitsDialog
{
	public static SettingsDialog instanceOf( Node parent )
	{
		SettingsTab generalTab = SettingsTabBuilder.create( "Limits" ).build();
		return new SettingsDialog( parent, "Limits", Lists.newArrayList( generalTab ) );
	}
}

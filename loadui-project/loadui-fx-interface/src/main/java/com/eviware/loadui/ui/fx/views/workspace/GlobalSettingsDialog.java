package com.eviware.loadui.ui.fx.views.workspace;

import javafx.scene.Node;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.control.SettingsDialog;
import com.eviware.loadui.ui.fx.control.SettingsTab;
import com.eviware.loadui.ui.fx.control.SettingsTab.Builder;
import com.google.common.collect.Lists;

@Deprecated
public class GlobalSettingsDialog
{
	public static SettingsDialog newInstance( @Nonnull Node parent, @Nonnull WorkspaceItem workspace )
	{
		SettingsTab generalTab = Builder.create( "Global Settings" )
				.field( "Max internal threads", workspace.getProperty( WorkspaceItem.MAX_THREADS_PROPERTY ) )
				.field( "Max internal thread queue size", workspace.getProperty( WorkspaceItem.MAX_THREAD_QUEUE_PROPERTY ) )
				.build();
		
		SettingsTab statsTab = Builder.create( "Statistics Settings" )
				.field( "Results path (affects all projects)", workspace.getProperty( WorkspaceItem.STATISTIC_RESULTS_PATH ) )
				.field( "Number of results to autosave", workspace.getProperty( WorkspaceItem.STATISTIC_NUMBER_OF_AUTOSAVES ) )
				.build();
		
		return new SettingsDialog( parent, "Execution", Lists.newArrayList( generalTab, statsTab ) );
	}
}

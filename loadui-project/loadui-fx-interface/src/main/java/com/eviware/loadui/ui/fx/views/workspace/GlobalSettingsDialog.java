/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.ui.fx.views.workspace;

import javafx.scene.Node;

import javax.annotation.Nonnull;

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.control.SettingsDialog;
import com.eviware.loadui.ui.fx.control.SettingsTab;
import com.eviware.loadui.ui.fx.control.SettingsTab.Builder;
import com.google.common.collect.Lists;

public class GlobalSettingsDialog
{
	public static SettingsDialog newInstance( @Nonnull Node parent, @Nonnull WorkspaceItem workspace )
	{
		SettingsTab generalTab = Builder.create( "Execution" )
				.field( "Max internal threads", workspace.getProperty( WorkspaceItem.MAX_THREADS_PROPERTY ) )
				.field( "Max internal thread queue size", workspace.getProperty( WorkspaceItem.MAX_THREAD_QUEUE_PROPERTY ) )
				.build();

		SettingsTab statsTab = Builder
				.create( "Statistics" )
				.field( "Results path (affects all projects)", workspace.getProperty( WorkspaceItem.STATISTIC_RESULTS_PATH ) )
				.field( "Number of test runs to autosave",
						workspace.getProperty( WorkspaceItem.STATISTIC_NUMBER_OF_AUTOSAVES ) ).build();

		return new SettingsDialog( parent, "Global settings", Lists.newArrayList( generalTab, statsTab ) );
	}
}

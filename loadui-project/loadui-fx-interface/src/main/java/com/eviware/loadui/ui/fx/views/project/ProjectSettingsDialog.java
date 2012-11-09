package com.eviware.loadui.ui.fx.views.project;

import static com.google.common.collect.Lists.newArrayList;
import javafx.scene.Node;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.ui.fx.control.FieldSaveHandler;
import com.eviware.loadui.ui.fx.control.SettingsDialog;
import com.eviware.loadui.ui.fx.control.SettingsTab;

public class ProjectSettingsDialog
{
	public static String IGNORE_INVALID_CANVAS = "gui.ignore_invalid_canvas";

	public static SettingsDialog newInstance( Node owner, final ProjectItem project )
	{
		SettingsTab executionTab = SettingsTab.Builder.create( "Execution" )
				.field( "Abort ongoing requests on finish", project.abortOnFinishProperty() ).build();

		SettingsTab reportsTab = SettingsTab.Builder.create( "Reports" )
				.field( "Export summary reports to file system", project.saveReportProperty() ).build();

		//TODO: add browse for report folder field

		SettingsTab miscTab = SettingsTab.Builder
				.create( "Misc." )
				.field( "Warn when starting a Canvas without both a Generator and a Runner",
						Boolean.valueOf( project.getAttribute( IGNORE_INVALID_CANVAS, "false" ) ),
						new FieldSaveHandler<Boolean>()
						{
							@Override
							public void save( Boolean fieldValue )
							{
								project.setAttribute( IGNORE_INVALID_CANVAS, fieldValue.toString() );
							}
						} ).build();

		return new SettingsDialog( owner, "Project Settings", newArrayList( executionTab, reportsTab, miscTab ) );
	}
}

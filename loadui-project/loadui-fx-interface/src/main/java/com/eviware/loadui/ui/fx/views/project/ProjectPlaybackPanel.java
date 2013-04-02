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
package com.eviware.loadui.ui.fx.views.project;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.ui.fx.views.canvas.ToolbarPlaybackPanel;

final public class ProjectPlaybackPanel extends ToolbarPlaybackPanel<ProjectItem>
{
	public ProjectPlaybackPanel( ProjectItem canvas )
	{
		super( canvas );

		getStyleClass().add( "project-playback-panel" );
		setMaxWidth( 750 );
		getChildren().setAll( separator(), playButton, separator(), time, requests, failures, resetButton(),
				limitsButton() );
	}
}

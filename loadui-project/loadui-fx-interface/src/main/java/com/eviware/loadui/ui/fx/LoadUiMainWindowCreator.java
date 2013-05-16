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
package com.eviware.loadui.ui.fx;

import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.ui.fx.views.analysis.FxExecutionsInfo;
import com.eviware.loadui.util.BeanInjector;

public class LoadUiMainWindowCreator
{
	private static final Logger log = LoggerFactory.getLogger( LoadUiMainWindowCreator.class );

	public LoadUiMainWindowCreator( WorkspaceProvider workspaceProvider, TestEventManager tem,
			FxExecutionsInfo executionsInfo )
	{
		boolean isHeadless = LoadUI.isHeadless();
		log.info( "JavaFX2 bundle started, is Headless mode ? " + isHeadless );

		if( !isHeadless )
		{
			// Stage published by OSGiFXLauncher after this bundle is started, so we have to wait for it
			new MainWindow( workspaceProvider ).withStage( BeanInjector.getBean( Stage.class ) )
					.withTestEventManager( tem ).provideInfoFor( executionsInfo ).show();
		}
	}

}

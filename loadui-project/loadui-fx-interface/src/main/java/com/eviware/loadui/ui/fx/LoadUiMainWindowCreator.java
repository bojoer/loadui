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

	public LoadUiMainWindowCreator( WorkspaceProvider workspaceProvider, TestEventManager tem, FxExecutionsInfo executionsInfo )
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

package com.eviware.loadui.test.ui.fx;

import gnu.cajo.invoke.Remote;
import gnu.cajo.utils.ItemServer;

import java.util.Arrays;

import javafx.stage.Stage;

import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.ScreenController;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.remote.ReferenceWrapper;
import com.eviware.loadui.util.remote.ReferenceWrapperImpl;

public class RemoteController
{
	public static void main( String[] args ) throws Exception
	{
		GUI.getStage();

		Thread.sleep( 1000 );

		Remote.config( null, 4711, null, 0 );

		ReferenceWrapper workspaceProviderWrapper = new ReferenceWrapperImpl(
				BeanInjector.getBean( WorkspaceProvider.class ), Arrays.asList( Object.class.getName() ),
				Arrays.asList( WorkspaceProvider.class.getName() ) );

		ItemServer.bind( workspaceProviderWrapper, "workspaceProvider" );

		ReferenceWrapper stageWrapper = new ReferenceWrapperImpl( BeanInjector.getBean( Stage.class ),
				Arrays.asList( Stage.class.getName() ), Arrays.<String> asList() );

		ItemServer.bind( stageWrapper, "stage" );

		ReferenceWrapper screenControllerWrapper = new ReferenceWrapperImpl( new FXScreenController(),
				Arrays.asList( Object.class.getName() ), Arrays.asList( ScreenController.class.getName() ) );

		ItemServer.bind( screenControllerWrapper, "screenController" );

		System.out.println( "OK!" );
	}
}

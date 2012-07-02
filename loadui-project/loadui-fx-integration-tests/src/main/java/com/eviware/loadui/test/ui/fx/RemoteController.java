package com.eviware.loadui.test.ui.fx;

import gnu.cajo.invoke.Remote;
import gnu.cajo.utils.ItemServer;

import java.util.Arrays;

import com.eviware.loadui.api.model.WorkspaceProvider;
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

		ReferenceWrapper wrapper = new ReferenceWrapperImpl( BeanInjector.getBean( WorkspaceProvider.class ),
				Arrays.asList( WorkspaceProvider.class.getName() ), Arrays.asList( Object.class.getName() ) );
		System.out.println( "OK!" );

		ItemServer.bind( wrapper, "workspaceProvider" );
	}
}

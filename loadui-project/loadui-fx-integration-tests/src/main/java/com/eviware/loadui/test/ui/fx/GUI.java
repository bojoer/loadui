package com.eviware.loadui.test.ui.fx;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javafx.stage.Stage;

import org.osgi.framework.BundleContext;

import com.eviware.loadui.ui.fx.util.test.ControllerApi;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.TestUtils;

public class GUI
{
	public static ControllerApi getController()
	{
		return getInstance().robot;
	}

	public static Stage getStage()
	{
		return getInstance().stage;
	}

	public static BundleContext getBundleContext()
	{
		return getInstance().controller.getBundleContext();
	}

	private static Holder getInstance()
	{
		if( Holder.instance.error != null )
		{
			throw new RuntimeException( Holder.instance.error );
		}

		return Holder.instance;
	}

	private static class Holder
	{
		private final ControllerFXWrapper controller;
		private final Stage stage;
		private final ControllerApi robot;
		private final Exception error;

		private static final Holder instance = new Holder();

		private Holder()
		{
			ControllerFXWrapper localController = null;
			Stage localStage = null;
			ControllerApi localRobot = null;
			Exception localError = null;

			try
			{
				localController = new ControllerFXWrapper();

				localStage = localController.getStageFuture().get( 10, TimeUnit.SECONDS );
				final Stage finalStage = localStage;

				TestUtils.awaitCondition( new Callable<Boolean>()
				{
					@Override
					public Boolean call() throws Exception
					{
						return finalStage.getScene() != null;
					}
				}, 60 );

				BeanInjector.setBundleContext( localController.getBundleContext() );

				Thread.sleep( 1000 );

				FXTestUtils.bringToFront( localStage );
				localRobot = ControllerApi.wrap( new FXScreenController() );
				ControllerApi.use( localStage );
			}
			catch( Exception e )
			{
				localError = e;
				e.printStackTrace();
			}

			controller = localController;
			stage = localStage;
			robot = localRobot;
			error = localError;
		}
	}
}

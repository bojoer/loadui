package com.eviware.loadui.test.ui.fx;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javafx.stage.Stage;

import com.eviware.loadui.ui.fx.util.test.FXRobot;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.TestUtils;

public class GUI
{
	public static FXRobot getRobot()
	{
		return getInstance().robot;
	}

	public static Stage getStage()
	{
		return getInstance().stage;
	}

	public static ControllerFXWrapper getController()
	{
		return getInstance().controller;
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
		private final FXRobot robot;
		private final Exception error;

		private static final Holder instance = new Holder();

		private Holder()
		{
			ControllerFXWrapper localController = null;
			Stage localStage = null;
			FXRobot localRobot = null;
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
				}, 20 );

				BeanInjector.setBundleContext( localController.getBundleContext() );

				Thread.sleep( 1000 );

				FXTestUtils.bringToFront( localStage );
				localRobot = new FXRobot();
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

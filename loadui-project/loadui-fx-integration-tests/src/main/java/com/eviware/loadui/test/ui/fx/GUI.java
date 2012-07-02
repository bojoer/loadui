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
		return Holder.instance.robot;
	}

	public static Stage getStage()
	{
		return Holder.instance.stage;
	}

	public static ControllerFXWrapper getController()
	{
		return Holder.instance.controller;
	}

	private static class Holder
	{
		private final ControllerFXWrapper controller;
		private final Stage stage;
		private final FXRobot robot;

		private static final Holder instance = new Holder();

		private Holder()
		{
			try
			{
				controller = new ControllerFXWrapper();

				stage = controller.getStageFuture().get( 10, TimeUnit.SECONDS );

				TestUtils.awaitCondition( new Callable<Boolean>()
				{
					@Override
					public Boolean call() throws Exception
					{
						return stage.getScene() != null;
					}
				}, 20 );

				BeanInjector.setBundleContext( controller.getBundleContext() );

				Thread.sleep( 1000 );

				FXTestUtils.bringToFront( stage );
				robot = new FXRobot();
			}
			catch( Exception e )
			{
				throw new RuntimeException( e );
			}
		}
	}
}

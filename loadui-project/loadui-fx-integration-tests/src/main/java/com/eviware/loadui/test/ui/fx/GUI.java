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
package com.eviware.loadui.test.ui.fx;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javafx.stage.Stage;

import org.osgi.framework.BundleContext;

import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.TestUtils;

public class GUI
{
	public static TestFX getController()
	{
		return getInstance().robot.target( getStage() );
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
		private final TestFX robot;
		private final Exception error;

		private static final Holder instance = new Holder();

		private Holder()
		{
			ControllerFXWrapper localController = null;
			Stage localStage = null;
			TestFX localRobot = null;
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
				localRobot = TestFX.wrap( new FXScreenController() );
				TestFX.targetWindow( localStage );
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

/*
 * Copyright 2011 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.test.ui.fx;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.osgi.framework.Bundle;

import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.ui.fx.util.test.FXRobot;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.collect.Iterables;

/**
 * Integration tests for testing the loadUI controller through its API.
 * 
 * @author dain.nilsson
 */
@Category( IntegrationTest.class )
public class ControllerFXTest
{
	private static ControllerFXWrapper controller;
	private static Stage stage;
	private static FXRobot robot;

	@BeforeClass
	public static void startController() throws Exception
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

	@AfterClass
	public static void stopController() throws Exception
	{
		controller.stop();
	}

	@Test
	public void shouldHaveNoFailedBundles()
	{
		Bundle[] bundles = controller.getBundleContext().getBundles();

		for( Bundle bundle : bundles )
		{
			assertThat( bundle.getSymbolicName() + " is not Active or Resolved", bundle.getState(),
					anyOf( is( Bundle.ACTIVE ), is( Bundle.RESOLVED ) ) );
			System.out.println( "Bundle: " + bundle );
		}

		System.out.println( "BUNDLES: " + bundles.length );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void shouldCreateNewProject() throws Exception
	{
		final ListView<ProjectRef> projectList = ( ListView<ProjectRef> )stage.getScene().lookup( "#projectRefNodeList" );

		robot.click( projectList, MouseButton.SECONDARY );
		robot.mouseMoveBy( 15, 10 );
		robot.click();

		WorkspaceItem workspace = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return projectList.getItems().size() == 1;
			}
		} );

		assertThat( workspace.getProjectRefs().size(), is( 1 ) );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void shouldDeleteProject() throws Exception
	{
		final ListView<Node> projectList = ( ListView<Node> )stage.getScene().lookup( "#projectRefNodeList" );

		Node projectRef = Iterables.getOnlyElement( projectList.getItems() );

		robot.click( projectRef.lookup( ".button" ) );

		WorkspaceItem workspace = BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();

		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return projectList.getItems().size() == 0;
			}
		} );

		assertThat( workspace.getProjectRefs().size(), is( 0 ) );
	}
}

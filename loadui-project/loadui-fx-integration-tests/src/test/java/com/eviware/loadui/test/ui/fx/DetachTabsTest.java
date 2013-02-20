package com.eviware.loadui.test.ui.fx;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import javafx.scene.input.KeyCode;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.test.TestUtils;

/**
 * 
 * @author OSTEN
 *	
 */
@Category( IntegrationTest.class )
public class DetachTabsTest
{
	
	private static TestFX controller;
	
	@BeforeClass
	public static void enterState() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();
		controller = GUI.getController();
		
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return TestFX.findAll( ".detachable-tab" ).size() > 1;
			}
		});
		
		//Magical mumbojumbo for the garbage-collection god.  
		System.gc();
		System.gc();
		System.gc();
	}
	
	@Test
	public void shouldDetachAndReattachWorkspace() throws Exception{

		controller.click( "#designTab" ).click( "#designTab .styleable-graphic" );
		
		try{
			TestUtils.awaitCondition( new Callable<Boolean>()
			{
				@Override
				public Boolean call() throws Exception
				{
					return TestFX.findAll( ".detached-content .project-canvas-view" ).size() == 1;
				}
			}, 3);
		}catch(TimeoutException e){
			fail("cannot create project-canvas-view");
		}
		//Check so that 
		assertThat(TestFX.findAll(".detached-content .project-canvas-view").size(), is(1));
		
		controller.press( KeyCode.ALT ).press( KeyCode.F4 ).release( KeyCode.F4 ).release( KeyCode.ALT );
	}
	
	@Test 
	public void ShouldDetachAndReattachStatistics() throws Exception{
		
		controller.click( "#statsTab" ).click( "#statsTab .styleable-graphic" );
		
		try{
			TestUtils.awaitCondition( new Callable<Boolean>()
			{
				@Override
				public Boolean call() throws Exception
				{
					return TestFX.findAll( ".detached-content .result-view" ).size() == 1;
				}
			}, 3);
		}catch(TimeoutException e){
			fail("cannot create result-view");
		}
		
		//Check so that 
		assertThat(TestFX.findAll(".detached-content .result-view").size(), is(1));
		
		controller.press( KeyCode.ALT ).press( KeyCode.F4 ).release( KeyCode.F4 ).release( KeyCode.ALT );
	}
	
	@AfterClass
	public static void cleanup(){
		controller = null; 
	}
	
}

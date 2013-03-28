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
package com.eviware.loadui.util.execution;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.collect.Lists;

public final class TestExecutionUtils
{
	private static final Logger log = LoggerFactory.getLogger( TestExecutionUtils.class );
	public static final String WARN_STOPPING_TEST = "gui.warn_stopping_test";
	public static final TestRunner testRunner = BeanInjector.getBean( TestRunner.class );

	public static TestExecution startCanvas( CanvasItem canvas )
	{
		log.info( "Starting canvas" );
		TestExecution currentExecution = popCurrentExecution();

		if( currentExecution != null )
		{
			if( !( canvas instanceof ProjectItem ) && canvas.getProject() == currentExecution.getCanvas() )
			{
				canvas.triggerAction( CanvasItem.START_ACTION );
				return null;
			}
			if( Boolean.parseBoolean( canvas.getProject().getAttribute( WARN_STOPPING_TEST, "true" ) ) )
			{
				//TODO: Add this dialog

				//				CheckBox checkbox = new CheckBox( "Don't show this dialog again" );
				//				Dialog dialog = new ConfirmationDialog()
				//					title: "Stop current test?"
				//					content: [
				//						Label { text: "Starting {canvas.getLabel()} requires that the current test be stopped.\r\nDo you wish to stop the currently running test?\r\n" },
				//						checkbox
				//					]
				//					okText: "Yes"
				//					cancelText: "No"
				//					onOk: function() {
				//						if( checkbox.selected ) canvas.getProject().setAttribute( WARN_STOPPING_TEST, "false" );
				//						for( execution in queuedExecutions ) execution.abort( "Aborting queued execution" );
				//						currentExecution.complete();
				//						testRunner.enqueueExecution( canvas );
				//						dialog.close();
				//					}
				//				}
				return null;
			}
			abortAllExecutions();
			currentExecution.complete();
		}
		log.info( "Enqueing new Execution: " + canvas.getDescription() );
		return testRunner.enqueueExecution( canvas );
	}

	public static TestExecution stopCanvas( CanvasItem canvas )
	{
		TestExecution execution = getCurrentExecution();
		if( execution != null )
		{
			if( execution.getCanvas() == canvas )
			{
				execution.complete();
				return execution;
			}
			else if( execution.getCanvas() == canvas.getProject() )
			{
				canvas.triggerAction( CanvasItem.STOP_ACTION );
			}
		}
		return null;
	}

	public static boolean isExecutionRunning()
	{
		List<TestExecution> e = testRunner.getExecutionQueue();
		return e.size() > 0 && !e.get( 0 ).isAborted();
	}

	private static TestExecution popCurrentExecution()
	{
		List<TestExecution> queuedExecutions = Lists.newArrayList( testRunner.getExecutionQueue() );
		if( !queuedExecutions.isEmpty() )
		{
			return queuedExecutions.remove( 0 );
		}
		return null;
	}

	private static TestExecution getCurrentExecution()
	{
		List<TestExecution> queuedExecutions = testRunner.getExecutionQueue();
		if( !queuedExecutions.isEmpty() )
		{
			return queuedExecutions.get( 0 );
		}
		return null;
	}

	private static void abortAllExecutions()
	{
		List<TestExecution> queuedExecutions = testRunner.getExecutionQueue();
		for( TestExecution execution : queuedExecutions )
		{
			execution.abort( "Aborting queued execution" );
		}
	}

}

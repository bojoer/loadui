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
package com.eviware.loadui.impl.execution;

import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.StringUtils;
import com.eviware.loadui.util.testevents.AbstractTestEvent;

public class TestExecutionEvent extends AbstractTestEvent implements TestEvent
{
	private static final TestEvent.Source<TestExecutionEvent> testExecutionEventSource = new TestEvent.Source<TestExecutionEvent>()
	{
		@Override
		public String getLabel()
		{
			return "TestExecution";
		}

		@Override
		public Class<TestExecutionEvent> getType()
		{
			return TestExecutionEvent.class;
		}

		@Override
		public byte[] getData()
		{
			return new byte[0];
		}

		@Override
		public String getHash()
		{
			return "";
		}
	};

	static TestExecutionEvent logExecutionEvent( ExecutionAction action )
	{
		TestExecutionEvent testEvent = new TestExecutionEvent( System.currentTimeMillis(), action );
		BeanInjector.getBean( TestEventManager.class ).logTestEvent( testExecutionEventSource, testEvent );

		return testEvent;
	}

	public enum ExecutionAction
	{
		STARTED, ABORTED, COMPLETED
	}

	private final ExecutionAction action;

	private TestExecutionEvent( long timestamp, ExecutionAction action )
	{
		super( timestamp );

		this.action = action;
	}

	public ExecutionAction getAction()
	{
		return action;
	}

	@Override
	public String toString()
	{
		return "Test " + StringUtils.capitalize( action.toString() );
	}

	public static class Factory extends AbstractTestEvent.Factory<TestExecutionEvent>
	{
		public Factory()
		{
			super( TestExecutionEvent.class );
		}

		@Override
		public TestExecutionEvent createTestEvent( long timestamp, byte[] sourceData, byte[] entryData )
		{
			ExecutionAction type = null;
			switch( entryData[0] )
			{
			case 0 :
				type = ExecutionAction.STARTED;
				break;
			case 1 :
				type = ExecutionAction.COMPLETED;
				break;
			case 2 :
				type = ExecutionAction.ABORTED;
				break;
			default :
				throw new IllegalArgumentException( "Illegal value for entry data" );
			}
			return new TestExecutionEvent( timestamp, type );
		}

		@Override
		public byte[] getDataForTestEvent( TestExecutionEvent testEvent )
		{
			switch( testEvent.getAction() )
			{
			case STARTED :
				return new byte[] { 0 };
			case COMPLETED :
				return new byte[] { 1 };
			case ABORTED :
				return new byte[] { 2 };
			}

			throw new IllegalArgumentException();
		}
	}
}

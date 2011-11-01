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

import java.util.concurrent.Future;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.execution.ExecutionResult;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.execution.AbstractTestExecution;

public class AgentTestExecution extends AbstractTestExecution
{
	private static CanvasItem lookupCanvas( String canvasId )
	{
		return ( CanvasItem )BeanInjector.getBean( AddressableRegistry.class ).lookup( canvasId );
	}

	public AgentTestExecution( String canvasId )
	{
		super( lookupCanvas( canvasId ) );
	}

	@Override
	public Future<ExecutionResult> complete()
	{
		throw new UnsupportedOperationException();
	}
}

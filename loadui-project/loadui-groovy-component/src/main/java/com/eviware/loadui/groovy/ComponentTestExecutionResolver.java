/*
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.groovy;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.execution.Phase;
import com.eviware.loadui.api.execution.TestExecution;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.util.groovy.resolvers.TestExecutionResolver;

/**
 * Modified TestExecutionResolver which only runs when the surrounding Canvas is
 * running.
 * 
 * @author dain.nilsson
 */
public class ComponentTestExecutionResolver extends TestExecutionResolver
{
	private final ComponentContext context;

	public ComponentTestExecutionResolver( ComponentContext context )
	{
		this.context = context;
	}

	@Override
	protected ClosureTestExecutionTask createTask()
	{
		return new ClosureTestExecutionTask()
		{
			@Override
			public void invoke( TestExecution execution, Phase phase )
			{
				if( LoadUI.CONTROLLER.equals( System.getProperty( LoadUI.INSTANCE ) ) )
				{
					CanvasItem myCanvas = context.getCanvas();
					if( !myCanvas.getProject().getWorkspace().isLocalMode() || !execution.contains( myCanvas ) )
					{
						return;
					}
				}
				super.invoke( execution, phase );
			}
		};
	}
}

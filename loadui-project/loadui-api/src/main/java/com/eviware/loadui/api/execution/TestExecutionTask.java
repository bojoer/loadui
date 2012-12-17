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
package com.eviware.loadui.api.execution;

/**
 * A task which is invoked during one or several phases of a TestExecution
 * life-cycle. A life-cycle phase will not complete until all TestExecutionTasks
 * for the given phase have completed. Tasks are executed in parallel.
 * 
 * @author dain.nilsson
 */
public interface TestExecutionTask
{
	/**
	 * Called when the given Phase is initiated.
	 * 
	 * @param context
	 * @param phase
	 */
	public void invoke( TestExecution execution, Phase phase );
}

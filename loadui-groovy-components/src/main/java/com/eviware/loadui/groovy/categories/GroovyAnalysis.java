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
package com.eviware.loadui.groovy.categories;

import java.util.Map;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.summary.MutableChapter;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.groovy.GroovyScriptSupport;
import com.eviware.loadui.impl.component.categories.AnalysisBase;
import com.eviware.loadui.util.ReleasableUtils;

public class GroovyAnalysis extends AnalysisBase
{
	private final GroovyScriptSupport scriptSupport;

	public GroovyAnalysis( EventFirer scriptUpdateFirer, ComponentContext context )
	{
		super( context );

		scriptSupport = new GroovyScriptSupport( scriptUpdateFirer, this, context );
	}

	@Override
	public void analyze( TerminalMessage message )
	{
		scriptSupport.invokeClosure( false, "analyze", message );
	}

	@Override
	public void onTerminalConnect( OutputTerminal output, InputTerminal input )
	{
		super.onTerminalConnect( output, input );
		scriptSupport.invokeClosure( true, "onTerminalConnect", output, input );
	}

	@Override
	public void onTerminalDisconnect( OutputTerminal output, InputTerminal input )
	{
		super.onTerminalDisconnect( output, input );
		scriptSupport.invokeClosure( true, "onTerminalDisconnect", output, input );
	}

	@Override
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, TerminalMessage message )
	{
		super.onTerminalMessage( output, input, message );
		scriptSupport.invokeClosure( true, "onTerminalMessage", output, input, message );
	}

	@Override
	public void onTerminalSignatureChange( OutputTerminal output, Map<String, Class<?>> signature )
	{
		super.onTerminalSignatureChange( output, signature );
		scriptSupport.invokeClosure( true, "onTerminalSignatureChange", output, signature );
	}

	@Override
	public Object collectStatisticsData()
	{
		Object result = scriptSupport.invokeClosure( true, "collectStatisticsData" );
		return result != null ? result : super.collectStatisticsData();
	}

	@Override
	public void handleStatisticsData( Map<AgentItem, Object> statisticsData )
	{
		super.handleStatisticsData( statisticsData );
		scriptSupport.invokeClosure( true, "handleStatisticsData", statisticsData );
	}

	@Override
	public void generateSummary( MutableChapter summary )
	{
		super.generateSummary( summary );
		scriptSupport.invokeClosure( true, "generateSummary", summary );
	}

	@Override
	public void onRelease()
	{
		super.onRelease();
		ReleasableUtils.release( scriptSupport );
	}
}

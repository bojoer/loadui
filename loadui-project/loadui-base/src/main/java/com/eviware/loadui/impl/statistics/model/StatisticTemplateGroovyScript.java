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
package com.eviware.loadui.impl.statistics.model;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.StatisticTemplate;
import com.eviware.loadui.api.traits.Releasable;

/**
 * StatisticTemplate which runs a Groovy script.
 * 
 * @author dain.nilsson
 */
public class StatisticTemplateGroovyScript implements StatisticTemplate, Releasable
{
	public static final Logger log = LoggerFactory.getLogger( StatisticTemplateGroovyScript.class );

	private final GroovyShell shell;
	private final Script script;

	public StatisticTemplateGroovyScript( String scriptString )
	{
		shell = new GroovyShell();
		script = tryParse( scriptString );
	}

	private Script tryParse( String scriptString )
	{
		try
		{
			return shell.parse( scriptString );
		}
		catch( CompilationFailedException e )
		{
			log.error( "TemplateScript failed to compile!", e );
			return null;
		}
	}

	@Override
	public void filter( StatisticHolder statisticHolder, ChartGroup chartGroup )
	{
		/*
		 * for( Chart chart : chartGroup.getChildren() ) if(
		 * chart.getStatisticHolder() == statisticHolder ) return;
		 */

		Binding binding = script.getBinding();
		binding.setVariable( "log", log );
		binding.setVariable( "statisticHolder", statisticHolder );
		binding.setVariable( "chartGroup", chartGroup );
		try
		{
			script.run();
		}
		catch( Exception e )
		{
			log.error( "TemplateScript for ChartGroup " + chartGroup.getLabel() + " threw an exception:", e );
		}
	}

	@Override
	public void release()
	{
		InvokerHelper.removeClass( script.getClass() );
		shell.resetLoadedClasses();
	}
}

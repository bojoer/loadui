/*
 * Copyright 2010 eviware software ab
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
package com.eviware.loadui.impl.component.categories;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.categories.AnalysisCategory;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.impl.component.ActivityStrategies;

/**
 * Base class for analysis components which defines base behavior which can be
 * extended to fully implement an analysis ComponentBehavior.
 * 
 * @author dain.nilsson
 */
public abstract class AnalysisBase extends BaseCategory implements AnalysisCategory
{
	private final InputTerminal inputTerminal;
	private Date lastMsgDate = new Date();
	
	private static Timer timer = new Timer();
	private BlinkTask blinkTask = new BlinkTask();
	

	/**
	 * Constructs an AnalysisBase.
	 * 
	 * @param context
	 *           A ComponentContext to bind the AnalysisBase to.
	 */
	public AnalysisBase( ComponentContext context )
	{
		super( context );
		getContext().setActivityStrategy(ActivityStrategies.ON);
		inputTerminal = context.createInput( INPUT_TERMINAL, "Input Data to be Analysed" );
		context.addEventListener(ActionEvent.class, new ActionListener() );
	}

	/**
	 * Analyzes the given TerminalMessage.
	 * 
	 * @param message
	 */
	public abstract void analyze( TerminalMessage message );

	@Override
	final public InputTerminal getInputTerminal()
	{
		return inputTerminal;
	}

	@Override
	public void onTerminalMessage( OutputTerminal output, InputTerminal input, TerminalMessage message )
	{
		if( input == inputTerminal ) {
			lastMsgDate = new Date();
			analyze( message );
		}
	}

	@Override
	final public String getCategory()
	{
		return CATEGORY;
	}

	@Override
	final public String getColor()
	{
		return COLOR;
	}
	
	private class BlinkTask extends TimerTask {
		@Override
		public void run()
		{
			if (lastMsgDate != null) {
				if ((lastMsgDate.getTime() + 1000) > (new Date()).getTime()) {
					getContext().setActivityStrategy(ActivityStrategies.BLINKING);
				} else {
					getContext().setActivityStrategy(ActivityStrategies.ON);
				}
			} else {
				getContext().setActivityStrategy(ActivityStrategies.ON);
			}
		}
	}
	
	private class ActionListener implements EventHandler<ActionEvent>
	{
		@Override
		public void handleEvent( ActionEvent event )
		{
			blinkTask.cancel();
				if (event.getKey() == "START") {
					getContext().setActivityStrategy(ActivityStrategies.BLINKING);
					blinkTask = new BlinkTask();
					timer.schedule(blinkTask, 500, 500);
				} else {
					getContext().setActivityStrategy(ActivityStrategies.ON);
				}
		}
	}

}

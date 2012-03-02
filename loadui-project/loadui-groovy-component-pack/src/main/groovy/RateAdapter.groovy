// 
// Copyright 2011 SmartBear Software
// 
// Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
// versions of the EUPL (the "Licence");
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at:
// 
// http://ec.europa.eu/idabc/eupl5
// 
// Unless required by applicable law or agreed to in writing, software distributed under the Licence is
// distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the Licence for the specific language governing permissions and limitations
// under the Licence.
// 


/**
 * Adapts the rate of a configured Generator for the optimum TPS output
 * 
 * @name Rate Adapter
 * @nonBlocking true
 * @help http://loadui.org/Custom-Components/rate-adapter.html
 * @id com.eviware.RateAdapter
 *
 */

import com.eviware.loadui.api.events.CollectionEvent
import com.eviware.loadui.impl.layout.OptionsProviderImpl
import com.eviware.loadui.api.events.EventHandler
import com.eviware.loadui.api.model.CanvasItem
import com.eviware.loadui.impl.component.categories.BaseCategory
import com.eviware.loadui.util.layout.DelayedFormattedString
import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.impl.component.ActivityStrategies
import com.eviware.loadui.api.ui.table.LTableModel
import com.eviware.loadui.api.summary.MutableSection

import java.lang.Math

import java.util.concurrent.TimeUnit 

// inputs and outputs
createInput( "resultsInput", "Results Input" )
createInput( "runnerStatsInput", "Runner Statistics Input" )
createOutput( "statisticsOutput", "Rate / Measured TPS" )

// properties
createProperty( 'start', Long, 1 )
createProperty( 'target', String )
createProperty( 'period', Long, 10 )
createProperty( 'step', Long, 1 )
createProperty( 'rollingCnt', Long, 10 )

// local variables for calculations, etc
def lastAvgTps = 0f
def currentAvgTps = 0f
def currentCnt = 0
def currentSum = 0
def currentRate = start.value
def completed = true
def decreaseCnt = 0
def avgTpsHistory = []

def rollingAvg = 0f
def stdDev = 0f
def stdDevPercent = 0f
def queuedRequests = 0
def targets = new OptionsProviderImpl()
def requestCnt = 0

// formatted strings for displaying counters
def displayRate = new DelayedFormattedString( '%d', 500, value { currentRate } )
def displayTPS = new DelayedFormattedString( '%.2f', 500, value { currentAvgTps } )
def displayLastTPS = new DelayedFormattedString( '%.2f', 500, value { lastAvgTps } )
def displayRollingAvg = new DelayedFormattedString( '%.2f', 500, value { rollingAvg } )
def displayStdDev = new DelayedFormattedString( '%.2f', 500, value { stdDev } )
def displayStdDevPercent = new DelayedFormattedString( '%.2f', 500, value { stdDevPercent } )

// method for updating the list of target components
def updateTargets =
{
	def t = []

	for( c in context.canvas.components )
	{
	   	if( c.getProperty( "rate" ) != null )
			t.add( c.label )
	}
   
	targets.options = t
}

// call the method once for initialization
updateTargets()

// add event listener to handle new or removed components
def eventListener = addEventListener( context.canvas, CollectionEvent.class ) { event ->
		if( event.key == CanvasItem.COMPONENTS )
			updateTargets()
}

// schedule closure for calculating current TPS and updating the LED 
scheduleAtFixedRate( 
{
	if( context.canvas.running )
	{
	   currentCnt++
	   currentSum += requestCnt
	   currentAvgTps = currentSum / currentCnt
	   
	   requestCnt = 0
    }
	
	updateLed()
}, 1, 1, TimeUnit.SECONDS )

// standard message handler that counts finished requests (for TPS calculation) and
// checks for queued messages
onMessage = { outgoing, incoming, message ->

	if( message["TimeTaken"] != null )
	{
		requestCnt++
	}
	
	def queued = message["Queued"]
	if( queued != null )
	{
		queuedRequests = queued
	}
}

// reset method called when tests are restarted
def reset = 
{
	lastAvgTps = 0f
	currentAvgTps = 0f
	currentCnt = 0
	currentSum = 0
	currentRate = start.value
	decreaseCnt = 0
	requestCnt = 0
	rollingAvg = 0f
	stdDevPercent = 0f
	stdDev = 0f
	stopScheduledFuture()
}

// method for stopping the main scheduled task; called so it isn't running
// multiple instances
stopScheduledFuture = 
{
	if( scheduledFuture != null )
	{
	   scheduledFuture.cancel( false )
	   scheduledFuture = null
	}
}

// sets the current rate on the configured target components
def setTargetRate =
{ rate ->
    def comp = context.canvas.getComponentByLabel( target.value )
      comp?.getProperty("rate")?.value = rate
}

// updates the LED display to show if component is actually doing anything
updateLed = {
	if (context.canvas.running && context.canvas.getComponentByLabel( target.value )?.getProperty( "rate" ) != null )
		setActivityStrategy(ActivityStrategies.BLINKING)
	else 
		setActivityStrategy(ActivityStrategies.OFF)
}

scheduledFuture = null

// method for starting and restarting the main workhorse methods; a bit 
// cumbersome scheduling logic because the period can be changed by the user
startScheduler =
{
	stopScheduledFuture()

	scheduledFuture = schedule( 
	{
		if( context.canvas.running )
		{
			try
			{
				def action = "None"
			
			    // decrease the target rate?
				if( queuedRequests > 0 || (currentAvgTps < lastAvgTps && currentRate > 1) )
				{
				    // increase after every second decrease to make sure that the TPS decrease isn't because of the rate decrease
					if( decreaseCnt >= 2 && queuedRequests == 0 )
					{
						currentRate += step.value
						decreaseCnt = 0
						action = "Increase"
					}
					else
					{
						currentRate -= step.value
						decreaseCnt++
						action = "Decrease"
					}
						
					setTargetRate( currentRate )
				}
				// or increase the target rate (if the current average TPS is higher than the previous one)?
				else if( currentAvgTps > lastAvgTps )
				{
					currentRate += step.value
					setTargetRate( currentRate )
					action = "Increase"
				}
				
				// output current values for logging purposes
				def message = newMessage();
				message["AvgTps"] = currentAvgTps
				message["Rate"] = currentRate
				message["Diff"] = currentAvgTps - lastAvgTps
				message["Action"] = action
				send( statisticsOutput, message );
			
				// save current TPS for next time and update rolling average
				lastAvgTps = currentAvgTps
				avgTpsHistory.add( currentAvgTps )
				while( avgTpsHistory.size() > rollingCnt.value ) 
					avgTpsHistory.remove( 0 )
					
				rollingAvg = avgTpsHistory.sum() / avgTpsHistory.size()
				
				// update standard deviation counters
				stdDev = Math.sqrt( avgTpsHistory.collect( { Math.pow( it - rollingAvg, 2 ) } ).sum() / avgTpsHistory.size() )
				
				stdDevPercent = stdDev/rollingAvg * 100
			   
			    // reset for next measurement period
				currentAvgTps = 0f
				currentCnt = 0
				currentSum = 0
				
				scheduledFuture = null
				
				// reschedule myself!
				startScheduler()
			}
			catch( Throwable t )
			{
				t.printStackTrace()
			}
		}
	}, period.value, TimeUnit.SECONDS )
}

// handle global state events
addEventListener( ActionEvent ) { event ->
 
	// test is stopped -> stop the main calculation
	if ( event.key == "STOP" ) {
		stopScheduledFuture()
	}
	// test is finished; set flag
	else if ( event.key == "COMPLETE" ) {
		completed = true
	}
	// reset everything (for example when user presses stop twice)
	else if ( event.key == "RESET" ) {
		reset()
		setTargetRate( currentRate )
	}
	// start scheduler, reset everything if this came after a complete
	else if ( event.key == "START" ) {
		if( completed )
		{
		   reset()   
		   setTargetRate( currentRate )
		   completed = false
		}
		
		startScheduler()
	}
}

// main layout
layout
{
	box( layout:'wrap 2, ins 0' )
	{
		property( property: target, label: "Generator", options: targets, widget: "comboBox", constraints: 'w 100!, spanx 2' )
		property( property: start, label: "Start At", min:1 )
		property( property: step, label: "Step", min:1 )
	}
	separator( vertical : true )
	box( widget:'display', layout:'wrap 3, align right' ) {
		node( label:'Rate', fString:displayRate, constraints:'w 60!' )
		node( label:'Avg TPS', fString:displayTPS, constraints:'w 60!' )
		node( label:'Last Avg TPS', fString:displayLastTPS, constraints:'w 70!' )
		node( label:'Rolling Avg', fString:displayRollingAvg, constraints:'w 60!' )
		node( label:'Std Dev', fString:displayStdDev, constraints:'w 60!' )
		node( label:'Std Dev (%)', fString:displayStdDevPercent, constraints:'w 60!' )
	}
}

// compact mode properties
compactLayout {
	box( widget:'display', layout:'wrap 3, align right' ) {
		node( label:'Rate', fString:displayRate, constraints:'w 60!' )
		node( label:'Avg TPS', fString:displayTPS, constraints:'w 60!' )
		node( label:'Last Avg TPS', fString:displayLastTPS, constraints:'w 70!' )
		node( label:'Rolling Avg', fString:displayRollingAvg, constraints:'w 60!' )
		node( label:'Std Dev', fString:displayStdDev, constraints:'w 60!' )
		node( label:'Std Dev (%)', fString:displayStdDevPercent, constraints:'w 60!' )
	}
}

// basic settings tab
settings( label: "Basic" ) {
	property( property: period, label: "Measurement Period" )		
	property( property: rollingCnt, label: "Rolling Count" )		
}

// add last counters to the summary report
generateSummary = { chapter ->
		// create table and its columns
		LTableModel table = new LTableModel(1, false);
	
		table.addColumn( "Target Generator" )
		table.addColumn( "Rate" );
		table.addColumn( "Avg TPS" )
		table.addColumn( "Last Avg TPS" )
		table.addColumn( "Rolling Avg" );
		table.addColumn( "Std Dev" )
		table.addColumn( "Std Dev %" )

		// add values
		ArrayList values = new ArrayList();
		values.add( target.value )
		values.add(displayRate);
		values.add( displayTPS )
		values.add( displayLastTPS )
		values.add( displayRollingAvg );
    	values.add( displayStdDev )
		values.add( displayStdDevPercent )
		
		table.addRow(values);
		
		// add section to report
		MutableSection sect = chapter.addSection(getLabel());
		sect.addTable(getLabel(), table)
   	}

// cleanup; we need to release listeners, etc when the component is removed
onRelease =
{
	context.canvas.removeEventListener( CollectionEvent.class, eventListener )
	displayRate.release()
	displayTPS.release()
	displayLastTPS.release()
	displayRollingAvg.release()
	displayStdDev.release()
	displayStdDevPercent.release()
}



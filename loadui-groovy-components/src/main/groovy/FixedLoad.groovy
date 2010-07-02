// 
// Copyright 2010 eviware software ab
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
 * Simulates a "classic" fixed load situation
 * 
 * @help http://www.loadui.org/Triggers/fixed-load.html
 * @name Fixed Load
 * @category generators
 * @nonBlocking true
 */

import com.eviware.loadui.api.component.categories.SamplerCategory
import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.util.layout.DelayedFormattedString

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

executor = Executors.newSingleThreadScheduledExecutor()

//Properties
createProperty( 'load', Long, 100 )
createProperty( 'interval', Long, 10 )

loadDisplay = new DelayedFormattedString( '%d', 200, 0 )
intervalDisplay = new DelayedFormattedString( '%d/ms', 200, interval.value )

sampleCount = createInput( 'Sample Count' )
count = 0;

running = true
future = null

onRelease = {
	executor.shutdownNow()
	loadDisplay.release()
	intervalDisplay.release()
}

schedule = {
	future?.cancel( true )
	if (stateProperty.value) {
		if (interval.value > 0) {
			running = true;
			future = executor.scheduleAtFixedRate( { if( count < load.value ) trigger() }, interval.value, interval.value, TimeUnit.MILLISECONDS )
		} else {
			running = false;
		}
	}
}

onMessage = { outgoing, incoming, message ->
	if ( incoming == sampleCount ) {
		def currentCount = message.get( SamplerCategory.CURRENTLY_RUNNING_MESSAGE_PARAM )
		count = currentCount
		
		if (currentCount < load.value && !running) {
			trigger()
			currentCount++
		}
		
		loadDisplay.args = currentCount
	}
}

onConnect = { outgoing, incoming ->
	if (outgoing == triggerTerminal && interval.value == 0)
		trigger()
}

addEventListener( PropertyEvent ) { event ->
	if ( event.event == PropertyEvent.Event.VALUE ) {
		if ( event.property == stateProperty ) {
			if( stateProperty.value ) schedule()
			else future?.cancel( true )
		} else if( event.property == interval ) {
			intervalDisplay.args = interval.value
			schedule()
		} else if( event.property == load && !running && count < load.value ) {
			trigger()
			loadDisplay.args = count + 1
		}
	}
}

addEventListener( ActionEvent ) { event ->
	if ( event.key == "STOP" ) future?.cancel( true )
	if ( event.key == "START" ) {
		schedule()
		if(!running && load.value > 0) trigger()
	}
	
	//RESET in this case would not really do anything
}

//Layout
layout  { 
	property( property:load, label:'Load', min:0 ) 
	separator( vertical:true )
	property( property:interval, label:'Interval\n(ms)', min:0 ) 
	separator( vertical:true )
	box( widget:'display' ) {
		node( label:'Load', fString:loadDisplay, constraints:"w 60!" )
		node( label:'Interval', fString:intervalDisplay, constraints:"w 60!" )
	}
}

schedule()

//Settings
//settings( label: "Settings", constraints: 'wrap 2' ) {
//	box(constraints:"growx, wrap 1") {
//		property( property:load, label:'Load', min:0, constraints:"w 60!") 
//		property( property:interval, label:'Interval', min:0, constraints:"w 60!" ) 
//	}
//} 

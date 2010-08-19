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
 * Sends an empty message periodically, at a set rate
 * 
 * @help http://www.loadui.org/Triggers/fixed-rate.html
 * @name Fixed Rate
 * @category generators
 * @nonBlocking true
 */

import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.util.layout.DelayedFormattedString

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

executor = Executors.newSingleThreadScheduledExecutor()

//Properties
createProperty( 'rate', Long, 10 )
createProperty( 'unit', String, 'Sec' )


milisecondsPerUnit = 1000000				
delay = milisecondsPerUnit/rate.value

display = new DelayedFormattedString( '%d / %s', 200, rate.value, unit.value )

onRelease = { 
	executor.shutdownNow() 
	display.release()
}
future = null
schedule = {
	if (stateProperty.value) {
		future?.cancel(true);
		future = executor.scheduleAtFixedRate( { trigger() }, delay.longValue(), delay.longValue(), TimeUnit.MICROSECONDS )
	}
}

addEventListener( PropertyEvent ) { event ->
	if ( event.event == PropertyEvent.Event.VALUE ) {
		if( event.property == unit ) {
			if ( unit.value == "Sec" )
				milisecondsPerUnit = 1000000
			if ( unit.value == "Min" )
				milisecondsPerUnit = 60000000
			if ( unit.value == "Hour" )
				milisecondsPerUnit = 3600000000
		}
		if (event.property == stateProperty && !stateProperty.value)
			future?.cancel(true)
		if (stateProperty.value)
			future?.cancel(true)
		if( rate.value != null && rate.value > 0 ) {
			delay = milisecondsPerUnit/rate.value
			if ( delay < 1 )
				delay = 1
			display.setArgs( rate.value, unit.value )
			schedule()
		}
	}
}

addEventListener( ActionEvent ) { event ->
	if ( event.key == "STOP" ) {
		future?.cancel(true)
	}
	
	if ( event.key == "START" ) {
		schedule()
	}
	
	//RESET in this case would not really do anything
}

//Layout
layout { 
	property( property:rate, label:'Rate', min:1 ) 
	separator( vertical:true )
	property( property:unit, label:'Unit', options:['Sec','Min','Hour'] )
	separator( vertical:true )
	box( widget:'display' ) {
		node( label:'Rate', fString:display, constraints:'w 75!' )
	}
}

//Compact Layout
compactLayout {
	box( widget:'display' ) {
		node( label:'Rate', fString:display, constraints:'w 75!' )
	}
}

//Settings
//settings( label: "Settings", layout: 'wrap 2' ) {
//	box(layout:"growx, wrap 1") {
//		property( property:rate, label:'Rate', min:0 ) 
//		property( property:unit, label:'Unit', options:['Sec','Min','Hour'] )
//	}
//} 

//Start scheduler
if ( unit.value == "Sec" )
	milisecondsPerUnit = 1000000
if ( unit.value == "Min" )
	milisecondsPerUnit = 60000000
if ( unit.value == "Hour" )
	milisecondsPerUnit = 3600000000

delay = milisecondsPerUnit/rate.value

if (running)
	schedule();



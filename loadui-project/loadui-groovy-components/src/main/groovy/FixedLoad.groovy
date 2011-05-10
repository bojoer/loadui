// 
// Copyright 2011 eviware software ab
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
 * @help http://www.loadui.org/Generators/fixed-load-component.html
 * @name Fixed Load
 * @category generators
 * @nonBlocking true
 */

import com.eviware.loadui.api.component.categories.RunnerCategory
import com.eviware.loadui.util.layout.DelayedFormattedString

import java.util.concurrent.TimeUnit

sampleCount = createInput( 'Sample Count' )
count = 0;

future = null

//Properties
createProperty( 'load', Long, 10 ) { value ->
	if( !doDelay && count < value ) {
		trigger()
		loadDisplay.args = count + 1
	}
}
createProperty( 'interval', Long, 0 ) { value ->
	intervalDisplay.args = value
	doDelay = stateProperty.value && value > 0
	schedule()
}

onReplace( stateProperty ) { value ->
	doDelay = value && interval.value > 0
	if( value ) schedule()
	else future?.cancel( true )
}

doDelay = stateProperty.value && interval.value > 0

loadDisplay = new DelayedFormattedString( '%d', 200, 0 )
intervalDisplay = new DelayedFormattedString( '%d ms', 200, interval.value )

onRelease = {
	loadDisplay.release()
	intervalDisplay.release()
}

schedule = {
	future?.cancel( true )
	if( doDelay ) future = scheduleAtFixedRate( { if( count < load.value ) trigger() }, interval.value, interval.value, TimeUnit.MILLISECONDS )
}

onMessage = { outgoing, incoming, message ->
	if ( incoming == sampleCount ) {
		def currentCount = message.get( RunnerCategory.CURRENTLY_RUNNING_MESSAGE_PARAM )
		count = currentCount
		
		if (currentCount < load.value && !doDelay) {
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

onAction( "START" ) {
	schedule()
	if( !doDelay && load.value > 0 ) trigger()
}

onAction( "STOP" ) { future?.cancel( true ) }

//Layout
layout  { 
	property( property:load, label:'Load', min:0 ) 
	separator( vertical:true )
	property( property:interval, label:'Min. Delay', min:0 ) 
	separator( vertical:true )
	box( widget:'display' ) {
		node( label:'Load', fString:loadDisplay, constraints:"w 60!" )
		node( label:'Min. Delay', fString:intervalDisplay, constraints:"w 60!" )
	}
}

//Compact Layout
compactLayout  {
	box( widget:'display' ) {
		node( label:'Load', fString:loadDisplay )
		node( label:'Min. Delay', fString:intervalDisplay )
	}
}

schedule()

//Settings
//settings( label: "Settings", layout: 'wrap 2' ) {
//	box(layout:"growx, wrap 1") {
//		property( property:load, label:'Load', min:0, constraints:"w 60!") 
//		property( property:interval, label:'Interval', min:0, constraints:"w 60!" ) 
//	}
//} 
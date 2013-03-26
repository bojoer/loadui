// 
// Copyright 2013 SmartBear Software
// 
// Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
// versions of the EUPL (the "Licence");
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at:
// 
// http://ec.europa.eu/idabc/eupl
// 
// Unless required by applicable law or agreed to in writing, software distributed under the Licence is
// distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the Licence for the specific language governing permissions and limitations
// under the Licence.
// 

/**
 * Simulates a "classic" fixed load situation
 * 
 * @id com.eviware.FixedLoad
 * @help http://www.loadui.org/Generators/fixed-load-component.html
 * @name Fixed Load
 * @category generators
 * @nonBlocking true
 */

import com.eviware.loadui.api.component.categories.RunnerCategory

import java.util.concurrent.TimeUnit

sampleCount = createInput( 'Sample Count', 'Currently running feedback', 'Used to recieve the number of currently running requests from the triggered Runner.' ) {
	it.name == "runningTerminal"
}

count = 0;

future = null

feedbackProviders = [:]
sampleCount.connections.each { feedbackProviders[it.outputTerminal] = 0 }

//Properties
createProperty( 'load', Long, 10 ) { value ->
	if( !doDelay && count < value ) {
		trigger()
		loadDisplay = count + 1
	}
}
createProperty( 'interval', Long, 0 ) { value ->
	doDelay = stateProperty.value && value > 0
	schedule()
}

onReplace( stateProperty ) { value ->
	doDelay = value && interval.value > 0
	if( value ) schedule()
	else future?.cancel( true )
}

doDelay = stateProperty.value && interval.value > 0

loadDisplay = 0

latestAction = 'NONE'

schedule = {
	future?.cancel( true )
	if( doDelay ) future = scheduleAtFixedRate( { if( count < load.value ) trigger() }, interval.value, interval.value, TimeUnit.MILLISECONDS )
	else if( load.value > 0 ) trigger()
}

onMessage = { outgoing, incoming, message ->
	if( incoming == sampleCount && latestAction != 'STOP' ) {
	
		// use the sum of all connected runners' currently running requests
		feedbackProviders[outgoing] = message[RunnerCategory.CURRENTLY_RUNNING_MESSAGE_PARAM]

		def currentCount = feedbackProviders.values().sum()
		count = currentCount
		
		if( currentCount < load.value && !doDelay ) {
			trigger()
			currentCount += feedbackProviders.size()
		}
		
		loadDisplay = currentCount
	}
}

onConnect = { outgoing, incoming ->
	if( incoming == sampleCount )
		feedbackProviders[outgoing] = 0

	if( outgoing == triggerTerminal && interval.value == 0 )
		trigger()
}

onDisconnect = { outgoing, incoming ->
	if( incoming == sampleCount )
		feedbackProviders.remove( outgoing )
}

onAction( 'START' ) {
	schedule()
	latestAction = 'START'
	if( !doDelay && load.value > 0 ) trigger()
}

onAction( 'STOP' ) {
	future?.cancel( true )
	latestAction = 'STOP'
}

//Layout
layout  { 
	property( property:load, label:'Load', min:0 ) 
	separator( vertical:true )
	property( property:interval, label:'Min. Delay', min:0 ) 
	separator( vertical:true )
	box( widget:'display' ) {
		node( label:'Load', content: { loadDisplay }, constraints:"w 60!" )
		node( label:'Min. Delay', content: { "$interval.value ms" }, constraints:"w 60!" )
	}
}

//Compact Layout
compactLayout  {
	box( widget:'display' ) {
		node( label:'Load', content: { loadDisplay } )
		node( label:'Min. Delay', content: { "$interval.value ms" } )
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
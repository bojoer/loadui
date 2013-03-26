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
 * Simulates the behaviour of a set of users on the system
 * 
 * @id com.eviware.VirtualUsers
 * @help http://loadui.org/Generators/virtual-users-components.html
 * @name Usage
 * @category generators
 * @nonBlocking true
 */

import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.api.events.ActionEvent

//Properties
createProperty( 'numUsers', Long, 20 )
createProperty( 'interval', Long, 10 )
createProperty( 'isRandomised', Boolean, false )

baseRate = numUsers.value/interval.value

defaultDelay = 1000/baseRate

timer = new Timer(true)
random = new Random()

future = null

schedule = {
	if (defaultDelay > 0 && stateProperty.value) {
		if (isRandomised.value) {
			currentDelay = defaultDelay * (-Math.log(1-(random.nextDouble())))
		} else
			currentDelay = defaultDelay
		
		
		future = timer.runAfter(currentDelay.intValue()) {
			trigger()
			schedule()
		}
	}
}

addEventListener( PropertyEvent ) { event ->
	if ( event.event == PropertyEvent.Event.VALUE ) {
		
		future?.cancel()
		baseRate = numUsers.value/interval.value
		if (baseRate > 0)
			defaultDelay = 1000/baseRate
		else
			defaultDelay = 0
		schedule()
	}
}

onAction( "START" ) {
	future?.cancel()
	schedule()
}

onAction( "STOP" ) { future?.cancel() }

//Layout
layout  { 
	property( property:numUsers, label:'Number of\nUsers', min:0 ) 
	separator( vertical:true )
	property( property:interval, label:'Request\nInterval\n(Sec)', min:1 ) 
	separator( vertical:true )
	property( property:isRandomised, label:'Random' )
	separator( vertical:true )
	box( widget:'display' ) {
		node( label:'Rate', content: { "${baseRate.longValue()} / Sec" } , constraints:"w 60!")
	}
}

//CompactLayout
compactLayout  {
	box( widget:'display' ) {
		node( label:'Rate', content: { "${baseRate.longValue()} / Sec" } )
	}
}

if (running)
	schedule()

//Settings
//settings( label: "Settings", layout: 'wrap 2' ) {
//	box(layout:"growx, wrap 1") {
//		property( property:numUsers, label:'Number of Users', min:0 ) 
//		property( property:interval, label:'Request Interval (ms)', min:1 ) 
//		property( property:variance, label:'Variance', min: 0, max: 100, step: 1 )
//	}
//}

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
 * Simulates the behaviour of a set of users on the system
 * 
 * @help http://www.loadui.org/Triggers/virtual-user.html
 * @name Virtual Users
 * @category generators
 * @nonBlocking true
 */

import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.util.layout.DelayedFormattedString

//Properties
createProperty( 'numUsers', Long, 20 )
createProperty( 'interval', Long, 10 )
createProperty( 'isRandomised', Boolean, false )

baseRate = numUsers.value/interval.value

defaultDelay = 1000/baseRate

display = new DelayedFormattedString( '%d', 200, baseRate.longValue() )

timer = new Timer(true)
random = new Random()

future = null

onRelease = {  display.release() }

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
		display.setArgs( baseRate.longValue() )
		schedule()
	}
}

addEventListener( ActionEvent ) { event ->
	if ( event.key == "STOP" ) {
		future?.cancel()
	}
	
	if ( event.key == "START" ) {
		future?.cancel()
		schedule()
	}
	
	//RESET in this case would not really do anything
}


//Layout
layout  { 
	property( property:numUsers, label:'Number of\nUsers', min:0 ) 
	separator( vertical:true )
	property( property:interval, label:'Request\nInterval\n(Sec)', min:1 ) 
	separator( vertical:true )
	property( property:isRandomised, label:'Random' )
	separator( vertical:true )
	box( widget:'display' ) {
		node( label:'Rate', fString:display , constraints:"w 60!")
	}
}

if (running)
	schedule()

//Settings
//settings( label: "Settings", constraints: 'wrap 2' ) {
//	box(constraints:"growx, wrap 1") {
//		property( property:numUsers, label:'Number of Users', min:0 ) 
//		property( property:interval, label:'Request Interval (ms)', min:1 ) 
//		property( property:variance, label:'Variance', min: 0, max: 100, step: 1 )
//	}
//}

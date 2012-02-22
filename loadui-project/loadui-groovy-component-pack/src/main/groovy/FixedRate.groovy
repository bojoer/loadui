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
 * Sends an empty message periodically, at a set rate
 * 
 * @id com.eviware.FixedRate
 * @help http://www.loadui.org/Generators/fixed-rate-component.html
 * @name Fixed Rate
 * @category generators
 * @nonBlocking true
 */

import java.util.concurrent.TimeUnit

//Properties
createProperty( 'rate', Long, 10 ) { schedule() }
createProperty( 'unit', String, 'Sec' ) { schedule() }

onReplace( stateProperty ) { value ->
	if( value ) schedule()
	else future?.cancel( true )
}

createProperty( 'burstSize', Long, 1 )

triggerBurst = { burstSize.value.times { trigger() } }

future = null
schedule = {
	if( stateProperty.value ) {
		long microsecondsPerUnit = unit.value == 'Sec' ? 1000000 : unit.value == 'Min' ? 60000000 : 3600000000
		long delay = Math.max( 1, (long)(microsecondsPerUnit / rate.value) )
		future?.cancel( true )
		future = scheduleAtFixedRate( triggerBurst, delay, delay, TimeUnit.MICROSECONDS )
	}
}

onAction( "START" ) { schedule() }
onAction( "STOP" ) { future?.cancel( true ) }

//Layout
layout { 
	property( property:rate, label:'Rate', min:1 ) 
	separator( vertical:true )
	property( property:unit, label:'Unit', options:['Sec','Min','Hour'] )
	separator( vertical:true )
	box( widget:'display' ) {
		node( label:'Rate', content: { "$rate.value / $unit.value" }, constraints:'wmin 75' )
	}
}

//Compact Layout
compactLayout {
	box( widget:'display' ) {
		node( label:'Rate', content: { "$rate.value / $unit.value" } )
	}
}

//Settings
settings( label: "General" ) {
	property( property: burstSize, label: 'Burst size' ) 
}

if( running ) schedule()
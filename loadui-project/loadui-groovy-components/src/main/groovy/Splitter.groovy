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
 * Splits input to specified number of outputs
 *
 * @help http://www.loadui.org/Flow-Control/splitter-component.html
 * @category flow
 * @nonBlocking true
 */

import com.eviware.loadui.util.layout.DelayedFormattedString
import com.eviware.loadui.util.ReleasableUtils

total = counters['total_output']
countDisplays = [:]
resetValues = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ]
totalReset = 0

for( i in 0..outgoingTerminalList.size() - 1 ) {
	countDisplays[i] = new DelayedFormattedString( '%d', 500, value { counters["output_$i"].get() - resetValues[i] } )
}

createProperty( 'type', String, "Round-Robin" )
createProperty( 'numOutputs', Integer, 1 ) { outputCount ->
	while( outgoingTerminalList.size() < outputCount ) {
		createOutgoing()
		def i = outgoingTerminalList.size() - 1
		countDisplays[i] = new DelayedFormattedString( '%d', 500, value { counters["output_$i"].get() - resetValues[i] } )
	}
	while( outgoingTerminalList.size() > outputCount ) {
		def i = outgoingTerminalList.size() - 1
		deleteOutgoing()
		countDisplays.remove( i )?.release()
	}
	refreshLayout()
}

random = new Random()
lastOutput = -1

totalDisplay = new DelayedFormattedString( '%d', 500, value { total.get() - totalReset } )
compactDisplay = new DelayedFormattedString( '%s', 500, value {
	(0..outgoingTerminalList.size() - 1).collect( { counters["output_$it"].get() - resetValues[it] } ).join( " " )
} )

onMessage = { incoming, outgoing, message ->
	if( type.value == "Round-Robin" ) lastOutput = (lastOutput + 1) % numOutputs.value
	else lastOutput = random.nextInt( numOutputs.value )
	send( outgoingTerminalList[lastOutput], message )
	counters["output_$lastOutput"].increment()
	total.increment()
}

onAction( "RESET" ) {
	lastOutput = -1
	resetValues = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ]
	totalReset = 0
}

onRelease = { ReleasableUtils.releaseAll( totalDisplay, compactDisplay, countDisplay.values ) }

refreshLayout = {
	layout ( layout:'gap 10 5' ) {
		node( widget: 'selectorWidget', label: "Type", labels: [ "Round-Robin", "Random" ], default: type.value, selected: type )
		separator( vertical: true )
		node( widget: 'sliderWidget', property: numOutputs, constraints: 'center, w 270!' )
		separator( vertical: true )
		box( layout: 'wrap, ins 0' ) {
			box( widget: 'display',  constraints: 'w 100!' ) {
				node( label: 'Count', fString: totalDisplay, constraints: 'wrap' )
			}
			action( label:'Clear', action: {
				for( i in 0..9 ) resetValues[i] = counters["output_$i"].get()
				totalReset = total.get()
			}, constraints:'right' )
		}
		separator( vertical: false )
		box( layout: 'ins 0, center', constraints: 'span 5, w 498!' ) {
			def gap = (int)((249/numOutputs.value)-19)
			for( i in 0..numOutputs.value - 1 ) {
				if( i != 0 ) separator( vertical: true )
				box( widget: 'display', layout: 'ins -5, center', constraints: "w 32!, h 24!, gap "+gap+" "+gap ) {
					node( fString: countDisplays[i], constraints: 'pad -6 -4' )
				}
			}
		}
	}
}

compactLayout {
	box( widget: 'display', layout: 'wrap, fillx', constraints: 'growx' ) {
		node( label: 'Count', fString: totalDisplay )
		node( label: 'Distribution', fString: compactDisplay )
	}
}
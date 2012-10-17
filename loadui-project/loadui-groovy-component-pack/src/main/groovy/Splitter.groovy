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
 * Splits input to specified number of outputs
 *
 * @id com.eviware.Splitter
 * @help http://www.loadui.org/Flow-Control/splitter-component.html
 * @category flow
 * @nonBlocking true
 */

import com.eviware.loadui.ui.fx.util.Properties
import javafx.scene.control.Slider

//Here to support Splitters created in loadUI 1.0, remove in the future:
try { renameProperty( 'outputs', 'numOutputs' ) } catch( e ) {}

incomingTerminal.description = 'Recieved messages will be outputted in different output terminals.'

total = counters['total_output']
countDisplays = [:]
terminalProbabilities = [:]
latestChanged = [:]
resetValues = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ]
totalReset = 0
changesDueToPropagation = [:]

for( i=0; i < outgoingTerminalList.size(); i++ ) {
	countDisplays[i] = { counters["output_$i"].get() - resetValues[i] }
	initialValue = outgoingTerminalList.size() > 1 ? 0 : 100
	terminalProbabilities[i] = createProperty( 'probability' + i, Integer, initialValue ) { newVal, oldVal ->
		if( oldVal != null && !wasChangedDueToPropagation( i ) )
		{
			compensateProbabilities( i, newVal - oldVal )
		}
	}
	latestChanged[i] = 0
}

def wasChangedDueToPropagation( propertyIndex ) {
	if ( changesDueToPropagation.containsKey( propertyIndex ) )
		return changesDueToPropagation.get( propertyIndex ) + 300 >  System.currentTimeMillis() 
	return false
}

def randomizeTerminal()
{
	 r = random.nextInt( 100 )
	 s = 0
	 for(entry in terminalProbabilities) {
		  p = entry.value.value
		  if( s <= r && s+p > r )
				return entry.key
		  s += p
	 }
	 return randomizeTerminal() //in case no terminal matched because of rounding errors, we try it again
}

def compensateProbabilities( changedProperty, diff ) {
	isCompensatingProbabilities = true
	latestChanged[changedProperty] = System.currentTimeMillis()
	
	while( diff > 0 )
	{
		indexToChange = latestChanged.find{ it.value == latestChanged.findAll{ terminalProbabilities[it.key].value != 0  }.collect{ it.value }.min() }.key
		
		propertyToChange = terminalProbabilities[indexToChange]
		
		changeSize = Math.min( propertyToChange.value, diff )
		
		if( changeSize == 0 )
			break
		
		propertyToChange.value -= changeSize
		changesDueToPropagation[indexToChange] = System.currentTimeMillis()
		diff -= changeSize
	}
	
	while( diff < 0 )
	{
		indexToChange = latestChanged.find{ it.value == latestChanged.findAll{ terminalProbabilities[it.key].value != 100  }.collect{ it.value }.min() }.key
		
		propertyToChange = terminalProbabilities[indexToChange]
		
		changeSize = Math.min( 100 - propertyToChange.value, Math.abs(diff) )
		
		if( changeSize == 0 )
			break
		
		propertyToChange.value += changeSize
		changesDueToPropagation[indexToChange] = System.currentTimeMillis()
		diff += changeSize
	}
}

createProperty( 'type', String, "Round-Robin" ) {
	refreshLayout()
}
createProperty( 'numOutputs', Integer, 1 ) { outputCount ->
	while( outgoingTerminalList.size() < outputCount ) {
		createOutgoing()
		def i = outgoingTerminalList.size() - 1
		
		countDisplays[i] = { counters["output_$i"].get() - resetValues[i] }
		
		initialValue = outgoingTerminalList.size() > 1 ? 0 : 100
		terminalProbabilities[i] = createProperty( 'probability' + i, Integer, initialValue ) { newVal, oldVal ->
			if( oldVal != null && !wasChangedDueToPropagation( i ) )
			{
				compensateProbabilities( i, newVal - oldVal )
			}
		}
		latestChanged[i] = 0
	}
	while( outgoingTerminalList.size() > outputCount ) {
		def i = outgoingTerminalList.size() - 1
		deleteOutgoing()
		countDisplays.remove( i )?.release()
		compensateProbabilities( i, terminalProbabilities[i].value * -1 )
		latestChanged.remove( i )
		deleteProperty( terminalProbabilities.remove( i )?.key )
	}
	
	refreshLayout()
}

random = new Random()
lastOutput = -1

onMessage = { outgoing, incoming, message ->
	if( incoming == incomingTerminal ) {
		if( type.value == "Round-Robin" ) lastOutput = (lastOutput + 1) % numOutputs.value
		else lastOutput = randomizeTerminal() //random.nextInt( numOutputs.value )
		send( outgoingTerminalList[lastOutput], message )
		counters["output_$lastOutput"].increment()
		total.increment()
	}
}

onAction( "RESET" ) {
	lastOutput = -1
	resetValues = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ]
	totalReset = 0
}

refreshLayout = {
	layout ( layout:'gap 10 5' ) {
		node( widget: 'selectorWidget', label: "Type", labels: [ "Round-Robin", "Random" ], default: type.value, selected: type )
		separator( vertical: true )
		//node( widget: 'sliderWidget', property: numOutputs, constraints: 'center, w 270!' )
		def slider = new Slider(min: 1, max: 10, value: numOutputs.value, majorTickUnit:1, minorTickCount:0, showTickLabels: true, snapToTicks: true, showTickMarks: true)
		slider.valueProperty().bindBidirectionally( Properties.convert( numOutputs ) )
		node( component: slider, constraints: 'center, w 270!' )
		
		separator( vertical: true )
		box( layout: 'wrap, ins 0' ) {
			box( widget: 'display',  constraints: 'w 100!' ) {
				node( label: 'Count', content: { total.get() - totalReset }, constraints: 'wrap' )
			}
//			action( label:'Clear', action: {
//				for( i in 0..9 ) resetValues[i] = counters["output_$i"].get()
//				totalReset = total.get()
//			}, constraints:'right' )
		}
		separator( vertical: false )
		box( layout: 'ins 0, center', constraints: 'span 5, w 498!' ) {
			def gap = (int)((249/numOutputs.value)-19)
			def moreThanOneTerminal = numOutputs.value > 1;
			for( i=0; i < outgoingTerminalList.size(); i++ ) {
				if( i != 0 ) separator( vertical: true )
				
				if( type.value == "Random" ) {
					property( property:terminalProbabilities[i], label:'%', min: 0, max: 100, step: 1, enabled:moreThanOneTerminal, layout: 'ins -15, center', constraints: "w 32!, gap "+gap+" "+gap )
				}
				else {
					box( widget: 'display', layout: 'ins -5, center', constraints: "w 32!, h 24!, gap "+gap+" "+gap ) {
						node( content: countDisplays[i], constraints: 'pad -6 -4' )
					}
				}
			}
		}
	}
}

compactLayout {
	box( widget: 'display', layout: 'wrap, fillx', constraints: 'growx' ) {
		node( label: 'Count', content: { total.get() - totalReset } )
		node( label: 'Distribution', content: { (0..outgoingTerminalList.size() - 1).collect( { counters["output_$it"].get() - resetValues[it] } ).join( " " ) } )
	}
}
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

<<<<<<< Updated upstream
=======
import com.eviware.loadui.ui.fx.util.Properties
import javafx.scene.control.Slider
import javafx.beans.InvalidationListener
import java.util.Timer

>>>>>>> Stashed changes
//Here to support Splitters created in loadUI 1.0, remove in the future:
try { renameProperty( 'outputs', 'numOutputs' ) } catch( e ) {}

incomingTerminal.description = 'Received messages will be outputted in different output terminals.'

random = new Random()

total = counters['total_output']
countDisplays = [:]
terminalProbabilities = [:]
userChanges = []
resetValues = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ]
propagatedIndexes = [:]
vals = [ 100, 0, 0, 0, 0, 0, 0, 0, 0, 0 ]

for( i=0; i < outgoingTerminalList.size(); i++ ) {
	countDisplays[i] = { counters["output_$i"].get() - resetValues[i] }
	initialValue = outgoingTerminalList.size() > 1 ? 0 : 100
	createProbabilityProperty(i)
}

callQueue = []
timer = new Timer("SplitterComponentQueueExecutor")

def createProbabilityProperty(i)
{
	terminalProbabilities[i] = createProperty( 'probability' + i, Integer, initialValue ) { newVal, oldVal ->
		
		log.info("***************Setting index $i, new: $newVal, old: $oldVal")
		
		if( oldVal != null  )
		{
			def isPropagation = propagatedIndexes.containsKey( i ) && propagatedIndexes.get( i ) == newVal
			if (isPropagation) {
				synchronized( callQueue ) {
					// schedule propagations to happen as soon as possible
					callQueue = [[isPropagation, i, oldVal, newVal]] + callQueue
				}
			} else {
				synchronized( callQueue ) {
					// schedule manually set values to happen after any propagation
					callQueue << [isPropagation, i, oldVal, newVal]
				}
			}
		}
		
		log.info("END, propagated: " + propagatedIndexes + ", callQueue size = " + callQueue.size())
		
		runNext()
		
	}

}

def runNext() {
	log.info("Running next, callQueue = " + Arrays.toString(callQueue))
	def nxt = null
	synchronized( callQueue ) {
		if (!callQueue.isEmpty()) {
			def headIsPropagated = callQueue[0][0]
			log.info("Head is propagated $headIsPropagated")
			if (propagatedIndexes.isEmpty() || headIsPropagated) {
				nxt =  callQueue.remove(0)
			}
		}
	}
	
	if (nxt != null) onChangeValue(nxt[1], nxt[2], ensureBounds(nxt[3]))
	
	if (!callQueue.isEmpty() || !propagatedIndexes.isEmpty())
	{
		timer.runAfter(20, { runNext() })
	}
	
}

def onChangeValue(i, oldVal, newVal)
{
	log.info("Propagated: " + propagatedIndexes)
	if (propagatedIndexes.containsKey(i)  && propagatedIndexes.get( i ) == newVal) {
		log.info("Index propagated: $i, will not trigger handler")
		propagatedIndexes.remove( i )
	} else {
		
		log.info("********** Requesting to change index " + i + " from $oldVal to $newVal, history : " + userChanges)
		vals[i] = newVal
		compensateAfterChange( i )

		// userChanges behaves as a sorted (insertion order) Set, so before adding anything to it, we try to remove it
		userChanges -= i;
		userChanges << i
	}

}

def compensateAfterChange( modifiedIndex ) {
	
	def sum1 = sum( vals )
	log.info("All Values = $vals sum to " + sum1 )

	if (sum1 == 100)
	{
		log.info("Done as sum is 100! Propagated: $propagatedIndexes")
		
	}
	else
	{
		while ( (sum1 = sum( vals )) != 100 ) {
			log.info("Auto-adjusting")
			int indexToChange = findNextIndexToChange(terminalProbabilities.size(), [modifiedIndex] + propagatedIndexes.keySet())
			
			log.info("Will change index $indexToChange")
		
			int desiredVal = 100 - ( sum1 - vals[indexToChange] );
			log.info("Desired value is " + desiredVal)
			def terminal = terminalProbabilities[indexToChange]
		
			terminal.value = ensureBounds( desiredVal );
			vals[indexToChange] = terminal.value
			propagatedIndexes.put( indexToChange, terminal.value )
		
			log.info("Desired value: " + desiredVal + ", terminal value: " + terminal.value)
			log.info("And the vals now looks like " + vals)
		
			if( desiredVal == terminal.value )
			{
				log.info("Done as desired value was reached!")
			}
		}
		
	}

	

}

def sum( vals )
{
	def sum = 0;
	vals.each { sum += it }
	return sum;
}

def ensureBounds( value )
{
	return Math.max( 0, Math.min( 100, value ) );
}

def findNextIndexToChange(terminalsCount, forbiddenIndexes)
{
	log.info("Finding index to change, forbidden: " + forbiddenIndexes + ", userChanges: $userChanges")
	def notUsedYet = (0..<terminalsCount) - userChanges - forbiddenIndexes
	log.info("Never used terminals: " + notUsedYet)
	if (notUsedYet.isEmpty()) {
		def res = userChanges.find { x -> !forbiddenIndexes.contains(x) }
		if (res == null) throw new RuntimeException()
		if (vals[res] == 0) {
			log.info("Would like to return $res but the value for it is 0")
			return findNextIndexToChange(terminalsCount, forbiddenIndexes << res)
		} else {
			return res
		}
	} else {
		return notUsedYet[0]
	}
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

createProperty( 'type', String, "Round-Robin" ) {
	refreshLayout()
}
<<<<<<< Updated upstream
createProperty( 'numOutputs', Integer, 1 ) { outputCount ->
=======

slider = new Slider(min: 2, max: 10, majorTickUnit:1, minorTickCount:0, showTickLabels: true, snapToTicks: true, showTickMarks: true)
invalidator = { if(!slider.valueChanging) numOutputs.value = slider.value } as InvalidationListener
slider.valueChangingProperty().addListener( invalidator )
slider.valueProperty().addListener( invalidator )

createProperty( 'numOutputs', Integer, 2 ) { outputCount ->

>>>>>>> Stashed changes
	while( outgoingTerminalList.size() < outputCount ) {
		createOutgoing()
		def i = outgoingTerminalList.size() - 1

		countDisplays[i] = { counters["output_$i"].get() - resetValues[i] }

		initialValue = outgoingTerminalList.size() > 1 ? 0 : 100
		log.info("Adding new terminal, current history: " + userChanges)
		
		createProbabilityProperty(i)
	}

	while( outgoingTerminalList.size() > outputCount ) {
		def i = outgoingTerminalList.size() - 1
		deleteOutgoing()
		countDisplays.remove( i )?.release()
		userChanges -= i
		deleteProperty( terminalProbabilities.remove( i )?.key )
		vals[i] = 0
		compensateAfterChange( i )
	}
<<<<<<< Updated upstream
	
=======

	slider.value = outputCount
>>>>>>> Stashed changes
	refreshLayout()

}


lastOutput = -1

onMessage = { outgoing, incoming, message ->
	if( incoming == incomingTerminal ) {
		if( type.value == "Round-Robin" ) lastOutput = (lastOutput + 1) % numOutputs.value
		else lastOutput = randomizeTerminal()
		send( outgoingTerminalList[lastOutput], message )
		counters["output_$lastOutput"].increment()
		total.increment()
	}
}

onAction( "RESET" ) {
	lastOutput = -1
	resetValues = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ]
}

refreshLayout = {
	layout ( layout:'gap 10 5' ) {
		node( widget: 'selectorWidget', label: "Type", labels: [ "Round-Robin", "Random" ], default: type.value, selected: type )
		separator( vertical: true )
<<<<<<< Updated upstream
		node( widget: 'sliderWidget', property: numOutputs, constraints: 'center, w 270!' )
=======
		box( layout: 'wrap, ins 0' ) {
			label( 'Number of Outputs' )
			node( component: slider, constraints: 'center, w 270!' )
		}

>>>>>>> Stashed changes
		separator( vertical: true )
		box( layout: 'wrap, ins 0' ) {
			box( widget: 'display',  constraints: 'w 100!' ) {
				node( label: 'Count', content: { total.get() }, constraints: 'wrap' )
			}
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
					box( widget: 'display', layout: 'ins -5, center', constraints: "w 50!, h 24!, gap "+gap+" "+gap ) {
						node( content: countDisplays[i], constraints: 'pad -6 -4' )
					}
				}
			}
		}
	}
}

compactLayout {
	box( widget: 'display', layout: 'wrap, fillx', constraints: 'growx' ) {
		node( label: 'Count', content: { total.get() } )
		node( label: 'Distribution', content: { (0..outgoingTerminalList.size() - 1).collect( { counters["output_$it"].get() - resetValues[it] } ).join( " " ) } )
	}
}
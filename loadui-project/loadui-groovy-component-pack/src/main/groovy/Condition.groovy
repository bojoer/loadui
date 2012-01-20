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
 * Routes messages based on whether a condition is true or false.
 * 
 * @id com.eviware.Condition
 * @help http://www.loadui.org/Flow/condition-component.html
 * @category flow
 * @nonBlocking true
 */

import com.eviware.loadui.impl.layout.OptionsProviderImpl
import groovy.lang.GroovyShell
import groovy.lang.Binding

createOutgoing( 'trueOutput' )
trueOutput.label = 'True'
trueOutput.description = 'VUs that satisfy the condition'

createOutgoing( 'falseOutput' )
falseOutput.label = 'False'
falseOutput.description = 'VUs that do not satisfy the condition'

incomingTerminal.description = 'VUs to test condition on'

shell = null
script = null

//Properties
createProperty( 'advancedMode', Boolean, false ) {
	if( it ) {
		shell = new GroovyShell()
		parseScript()
	} else {
		shell = null
		script = null
		setInvalid( valueName.value == "Select value" )
	}
	redraw()
}
createProperty( 'condition', String ) {
	if( advancedMode.value ) parseScript()
}
createProperty( 'valueName', String, "Select value" )
createProperty( 'max', Long, 1000 )
createProperty( 'min', Long, 0 ) { value ->
	if(max.value < value ) max.value = value
}

trueCount = 0
falseCount = 0

parseScript = {
	if( condition.value ) {
		try {
			script = shell.parse( condition.value )
			setInvalid( false )
		} catch( e ) {
			log.error( "Unable to parse condition: $condition.value", e )
			setInvalid( true )
		}
	} else {
		setInvalid( true )
	}
}

provider = new OptionsProviderImpl()

onAction( "RESET" ) { ->
	trueCount = 0
	falseCount = 0
}

onConnect = { outgoing, incoming ->
	if( incoming == incomingTerminal ) {
		redraw()
	}
}

onDisconnect = { outgoing, incoming ->
	if( incoming == incomingTerminal ) {
		if( !options().contains( valueName.value ) ) valueName.value = 'Select value'
		redraw()
	}
}

onSignature = { outgoing, signature ->
	if( !options().contains( valueName.value ) ) valueName.value = 'Select value'
	redraw()
}

onMessage = { incoming, outgoing, message ->
	def result = false
	if( advancedMode.value ) {
		try {
			script.binding = new Binding( new HashMap( message ) )
			result = script.run() as Boolean
		} catch( e ) {
			log.error( "Unable to parse condition: $condition.value", e )
			setInvalid( true )
		}
	} else {
		result = assertValue( message[valueName.value] )
	}
	if( result ) {
		trueCount++
		send( trueOutput, message )
	} else {
		falseCount++
		send( falseOutput, message )
	}
}

assertValue = { value ->
	if( !( value instanceof Number ) ) {
		try {
			value = Double.valueOf( String.toString( value ) )
		} catch( e ) {
			try {
				value = Long.valueOf( String.toString( value ) )
			} catch( e2 ) {
				return false
			}
		}
	}
	
	return min.value <= value && value <= max.value
}

def options() {
	incomingTerminal.connections.collect( { it.outputTerminal.messageSignature.keySet() } ).flatten() as Set
}

redraw = {
	provider.options = options()

	layout {
		if( advancedMode.value ) {
			box( layout: 'insets 0, gap 33, wrap 1' ) {
				property( property: condition, label: 'Condition', constraints: 'w 221!' )
				property( property: advancedMode, label: 'Advanced Mode' )
			}
			separator( vertical: true )
			box( widget:'display', layout:'wrap 2', column: '40' ) {
				node( label:'True', content: { trueCount } )
				node( label:'False', content: { falseCount } )
				node( label:'Condition', content: { invalid ? 'Invalid' : 'OK' }, constraints: 'span 2' )
			}
		} else {
			box( layout: 'insets 0, gap 33, wrap 1' ) {
				property( property: valueName, widget: 'comboBox', label: 'Value', options: provider, constraints: 'w 100!' )
				property( property: advancedMode, label: 'Advanced Mode' )
			}
			separator( vertical: true )
			property( property: min, label: 'Min', min: 0 )
			property( property: max, label: 'Max', min: 0 )
			separator( vertical: true )
			box( widget:'display', layout:'wrap 2', column: '40' ) {
				node( label:'True', content: { trueCount } )
				node( label:'False', content: { falseCount } )
				node( label:'Min', content: { min.value } )
				node( label:'Max', content: { max.value } )
			}
		}
	}
}

compactLayout {
	box( widget:'display', layout:'wrap 2', column: '40' ) {
		node( label:'True', content: { trueCount } )
		node( label:'False', content: { falseCount } )
	}
}

redraw()
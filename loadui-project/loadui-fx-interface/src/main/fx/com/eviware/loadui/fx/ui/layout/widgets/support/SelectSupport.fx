/* 
 * Copyright 2011 eviware software ab
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
/*
*SelectSupport.fx
*
*Created on apr 15, 2010, 13:16:34 em
*/

package com.eviware.loadui.fx.ui.layout.widgets.support;

import com.eviware.loadui.fx.FxUtils;

import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.api.layout.OptionsProvider;
import com.eviware.loadui.impl.layout.OptionsProviderImpl;

import java.lang.Iterable;
import java.lang.Runnable;

public mixin class SelectSupport {
	protected var options:Object[];

	protected var optionsProvider:OptionsProvider on replace {
		optionsProvider.registerListener( Runnable {
			override function run () {
				FxUtils.runInFxThread( function():Void { options = for( option in optionsProvider ) option } );
			}
		} );
	}
	
	protected function setPlc( plc:PropertyLayoutComponent ):Void {
		def opts = plc.get("options");
		optionsProvider = if( opts instanceof OptionsProvider ) opts as OptionsProvider
			else if( opts instanceof Iterable ) new OptionsProviderImpl( opts as Iterable )
			else new OptionsProviderImpl( opts );
		options = for( option in optionsProvider ) option;
	}
}

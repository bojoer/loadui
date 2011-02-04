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
*BaseMixin.fx
*
*Created on mar 10, 2010, 11:26:03 fm
*/

package com.eviware.loadui.fx.ui.node;

import java.lang.RuntimeException;

public mixin class BaseMixin {
	//This should NOT be used by Mixin classes extending BaseMixin, as they may be initialized before sometimes.
	public def node = this as BaseNode;
	
	init {
		if( not (this instanceof BaseNode) )
			throw new RuntimeException( "BaseMixin must extend BaseNode!" );
	}
}

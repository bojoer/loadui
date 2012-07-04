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
package com.eviware.loadui.components.soapui;

import com.eviware.loadui.api.component.BehaviorProvider;
import com.eviware.loadui.api.component.ComponentBehavior;
import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.component.ComponentRegistry;

public class SoapUIBehaviorProvider implements BehaviorProvider
{
	private ComponentRegistry registry;

	@Override
	public ComponentBehavior createBehavior( ComponentDescriptor descriptor, ComponentContext context )
	{
		return loadBehavior( descriptor.getType(), context );
	}

	@Override
	public ComponentBehavior loadBehavior( String componentType, ComponentContext context )
	{
		if( SoapUISamplerComponent.TYPE.equals( componentType ) )
			return new SoapUISamplerComponent( context );
		
		if( MockServiceComponent.TYPE.equals( componentType ) )
			return new MockServiceComponent( context );

		return null;
	}

	public void destroy()
	{
		registry.unregisterProvider( this );
	}

	public ComponentRegistry getRegistry()
	{
		return registry;
	}

	public void setRegistry( ComponentRegistry registry )
	{
		this.registry = registry;
	}
}

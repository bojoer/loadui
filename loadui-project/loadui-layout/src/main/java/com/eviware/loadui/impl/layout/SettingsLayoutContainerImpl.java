/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.layout;

import java.util.Map;

import com.eviware.loadui.api.layout.SettingsLayoutContainer;
import com.google.common.collect.ImmutableMap;

public class SettingsLayoutContainerImpl extends LayoutContainerImpl implements SettingsLayoutContainer
{
	public final static String LABEL = "label";

	public SettingsLayoutContainerImpl( Map<String, ?> args )
	{
		super( args );

		if( !( properties.get( LABEL ) instanceof String ) )
			throw new IllegalArgumentException( "Illegal arguments: " + args );
	}

	public SettingsLayoutContainerImpl( String label, String layoutConstraints, String colConstraints,
			String rowConstraints, String constraints )
	{
		super( ImmutableMap.<String, Object> builder().put( LAYOUT_CONSTRAINTS, layoutConstraints )
				.put( COLUMN_CONSTRAINTS, colConstraints ).put( ROW_CONSTRAINTS, rowConstraints )
				.put( CONSTRAINTS, constraints ).put( LABEL, label ).build() );

		if( !( properties.get( LABEL ) instanceof String ) )
			throw new IllegalArgumentException( "Illegal label: " + properties.get( LABEL ) );
	}

	@Override
	public String getLabel()
	{
		return ( String )properties.get( LABEL );
	}

	@Override
	public Map<String, ?> getProperties()
	{
		return properties;
	}
}

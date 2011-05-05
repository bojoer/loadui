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
package com.eviware.loadui.impl.layout;

import java.util.Map;

import com.eviware.loadui.api.layout.LabelLayoutComponent;
import com.eviware.loadui.util.MapUtils;
import com.google.common.collect.ImmutableMap;

public class LabelLayoutComponentImpl extends LayoutComponentImpl implements LabelLayoutComponent
{
	public static final String LABEL = "label";

	public LabelLayoutComponentImpl( Map<String, ?> args )
	{
		super( args );
	}

	public LabelLayoutComponentImpl( String label, String constraints )
	{
		this( ImmutableMap.of( LABEL, label, CONSTRAINTS, constraints ) );
	}

	@Override
	public String getLabel()
	{
		return MapUtils.getOr( properties, LABEL, "" );
	}
}

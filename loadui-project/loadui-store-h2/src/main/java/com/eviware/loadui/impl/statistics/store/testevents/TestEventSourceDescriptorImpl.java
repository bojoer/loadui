/* 
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.impl.statistics.store.testevents;

import java.util.Map;
import java.util.Set;

import com.eviware.loadui.api.testevents.TestEventSourceDescriptor;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class TestEventSourceDescriptorImpl implements TestEventSourceDescriptor
{
	private final String label;
	private final Map<String, TestEventSourceConfig> configs = Maps.newHashMap();

	public TestEventSourceDescriptorImpl( String label )
	{
		this.label = label;
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	public Set<TestEventSourceConfig> getConfigs()
	{
		return ImmutableSet.copyOf( configs.values() );
	}

	public void putConfig( String hash, TestEventSourceConfig testEventSourceConfig )
	{
		configs.put( hash, testEventSourceConfig );
	}
}

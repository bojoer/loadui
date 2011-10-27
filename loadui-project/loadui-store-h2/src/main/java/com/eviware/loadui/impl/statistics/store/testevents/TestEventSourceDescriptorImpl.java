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

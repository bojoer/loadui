package com.eviware.loadui.api.statistics;

import java.util.Set;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.statistics.store.Execution;

public interface ExecutionAddon extends Addon
{
	public Set<Execution> getExecutions();

	public Set<Execution> getExecutions( boolean includeRecent, boolean includeArchived );
}

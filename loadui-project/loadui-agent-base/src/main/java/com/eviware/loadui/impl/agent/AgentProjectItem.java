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
package com.eviware.loadui.impl.agent;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import com.eviware.loadui.api.addon.Addon;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.addressable.AddressableRegistry.DuplicateAddressException;
import com.eviware.loadui.api.component.ComponentDescriptor;
import com.eviware.loadui.api.counter.Counter;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.Assignment;
import com.eviware.loadui.api.model.CanvasObjectItem;
import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.api.summary.MutableSummary;
import com.eviware.loadui.api.summary.Summary;
import com.eviware.loadui.api.terminal.Connection;
import com.eviware.loadui.api.terminal.InputTerminal;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class AgentProjectItem implements ProjectItem
{
	private HashSet<SceneItem> scenes = Sets.newHashSet();
	private final MessageEndpoint controller;
	private final String id;

	public AgentProjectItem( MessageEndpoint controller, String id ) throws DuplicateAddressException
	{
		this.controller = controller;
		this.id = id;

		BeanInjector.getBean( AddressableRegistry.class ).register( this );
	}

	public void addScene( SceneItem scene )
	{
		scenes.add( scene );
	}

	public void removeScene( SceneItem scene )
	{
		scenes.remove( scene );
	}

	public MessageEndpoint getEndpoint()
	{
		return controller;
	}

	@Override
	public ProjectItem getProject()
	{
		return this;
	}

	@Override
	public boolean isDirty()
	{
		return false;
	}

	@Override
	public ComponentItem createComponent( String label, ComponentDescriptor descriptor )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<ComponentItem> getComponents()
	{
		return Collections.emptySet();
	}

	@Override
	public ComponentItem getComponentByLabel( String label )
	{
		return null;
	}

	@Override
	public Collection<Connection> getConnections()
	{
		return Collections.emptySet();
	}

	@Override
	public Connection connect( OutputTerminal output, InputTerminal input )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRunning()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isStarted()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCompleted()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLimit( String counterName, long counterValue )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLimit( String counterName )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void generateSummary( MutableSummary summary )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Summary getSummary()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CanvasObjectItem duplicate( CanvasObjectItem obj )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLoadingError()
	{
		return false;
	}

	@Override
	public void cancelComponents()
	{
		for( SceneItem scene : scenes )
		{
			scene.cancelComponents();
		}
	}

	@Override
	public boolean isAbortOnFinish()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAbortOnFinish( boolean abort )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getHelpUrl()
	{
		return null;
	}

	@Override
	public void triggerAction( String actionName )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public <T extends Addon> T getAddon( Class<T> type )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends EventObject> void addEventListener( Class<T> type, EventHandler<T> listener )
	{
	}

	@Override
	public <T extends EventObject> void removeEventListener( Class<T> type, EventHandler<T> listener )
	{
	}

	@Override
	public void clearEventListeners()
	{
	}

	@Override
	public void fireEvent( EventObject event )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAttribute( String key, String value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAttribute( String key, String defaultValue )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttribute( String key )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<String> getAttributes()
	{
		return Collections.emptySet();
	}

	@Override
	public Property<?> getProperty( String propertyName )
	{
		return null;
	}

	@Override
	public Collection<Property<?>> getProperties()
	{
		return Collections.emptySet();
	}

	@Override
	public void renameProperty( String oldName, String newName )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType, Object initialValue )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Property<T> createProperty( String propertyName, Class<T> propertyType, Object initialValue,
			boolean propagates )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteProperty( String propertyName )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLabel( String label )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLabel()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDescription( String description )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDescription()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void release()
	{
		BeanInjector.getBean( AddressableRegistry.class ).unregister( this );
		scenes.clear();
	}

	@Override
	public Counter getCounter( String counterName )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<String> getCounterNames()
	{
		return Collections.emptySet();
	}

	@Override
	public StatisticVariable getStatisticVariable( String statisticVariableName )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> getStatisticVariableNames()
	{
		return Collections.emptySet();
	}

	@Override
	public File getProjectFile()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void save()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public WorkspaceItem getWorkspace()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<SceneItem> getScenes()
	{
		return ImmutableSet.copyOf( scenes );
	}

	@Override
	public SceneItem getSceneByLabel( final String label )
	{
		return Iterables.find( scenes, new Predicate<SceneItem>()
		{
			@Override
			public boolean apply( SceneItem input )
			{
				return Objects.equal( label, input.getLabel() );
			}
		} );
	}

	@Override
	public SceneItem createScene( String label )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<AgentItem> getAgentsAssignedTo( SceneItem scene )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSceneLoaded( SceneItem scene, AgentItem agent )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void broadcastMessage( SceneItem scene, String channel, Object data )
	{
		controller.sendMessage( channel, data );
	}

	@Override
	public Collection<SceneItem> getScenesAssignedTo( AgentItem agent )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Assignment> getAssignments()
	{
		return Collections.emptySet();
	}

	@Override
	public void assignScene( SceneItem scene, AgentItem agent )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void unassignScene( SceneItem scene, AgentItem agent )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSaveReport()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSaveReport( boolean save )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveAs( File dest )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getReportFolder()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setReportFolder( String path )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getReportFormat()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setReportFormat( String format )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public StatisticPages getStatisticPages()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void cancelScenes( boolean linkedOnly )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long getNumberOfAutosaves()
	{
		return 0;
	}

	@Override
	public void setNumberOfAutosaves( long n )
	{
		throw new UnsupportedOperationException();
	}
}

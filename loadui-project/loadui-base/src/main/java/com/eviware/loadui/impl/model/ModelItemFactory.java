package com.eviware.loadui.impl.model;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.xmlbeans.XmlException;
import org.springframework.core.convert.ConversionService;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.component.ComponentRegistry;
import com.eviware.loadui.api.counter.CounterSynchronizer;
import com.eviware.loadui.api.execution.TestRunner;
import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpointProvider;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.property.PropertySynchronizer;
import com.eviware.loadui.config.AgentItemConfig;
import com.eviware.loadui.config.ComponentItemConfig;
import com.eviware.loadui.config.LoaduiProjectDocumentConfig;
import com.eviware.loadui.config.LoaduiWorkspaceDocumentConfig;
import com.eviware.loadui.config.ProjectReferenceConfig;
import com.eviware.loadui.config.SceneItemConfig;

public class ModelItemFactory
{

	private AddressableRegistry addressableRegistry;
	private ConversionService conversionService;
	private BroadcastMessageEndpoint broadCastMsgEndPoint;
	private MessageEndpointProvider msgEndPointProvider;
	private ScheduledExecutorService scheduledExecutorService;
	private ComponentRegistry componentRegistry;
	private TestRunner testRunner;
	private CounterSynchronizer counterSynchronizer;
	private PropertySynchronizer propertySynchronizer;

	public void setAddressableRegistry( AddressableRegistry addressableRegistry )
	{
		this.addressableRegistry = addressableRegistry;
	}

	public void setConversionService( ConversionService conversionService )
	{
		this.conversionService = conversionService;
	}

	public void setBroadCastMessageEndPoint( BroadcastMessageEndpoint broadCastMsgEndPoint )
	{
		this.broadCastMsgEndPoint = broadCastMsgEndPoint;
	}

	public void setMessageEndpointProvider( MessageEndpointProvider msgEndPointProvider )
	{
		this.msgEndPointProvider = msgEndPointProvider;
	}

	public void setScheduledExecutorService( ScheduledExecutorService scheduledExecutorService )
	{
		this.scheduledExecutorService = scheduledExecutorService;
	}

	public void setComponentRegistry( ComponentRegistry componentRegistry )
	{
		this.componentRegistry = componentRegistry;
	}

	public void setTestRunner( TestRunner testRunner )
	{
		this.testRunner = testRunner;
	}

	public void setCounterSynchronizer( CounterSynchronizer counterSynchronizer )
	{
		this.counterSynchronizer = counterSynchronizer;
	}

	public void setPropertySynchronizer( PropertySynchronizer propertySynchronizer )
	{
		this.propertySynchronizer = propertySynchronizer;
	}

	public WorkspaceItem loadWorkspaceFrom( File workspaceFile ) throws XmlException, IOException
	{
		LoaduiWorkspaceDocumentConfig workspaceDoc = workspaceFile.exists() ? LoaduiWorkspaceDocumentConfig.Factory
				.parse( workspaceFile ) : LoaduiWorkspaceDocumentConfig.Factory.newInstance();
		WorkspaceItemImpl item = new WorkspaceItemImpl( workspaceFile, workspaceDoc, addressableRegistry,
				conversionService );
		item.setModelItemFactory( this );
		item.init();
		item.postInit();
		return item;
	}

	public AgentItemImpl createAgentItemImpl( WorkspaceItem workspace, AgentItemConfig config )
	{
		AgentItemImpl item = new AgentItemImpl( config, addressableRegistry, conversionService, workspace,
				broadCastMsgEndPoint, msgEndPointProvider, scheduledExecutorService );
		item.init();
		item.postInit();
		return item;
	}

	public SceneItemImpl newInstance( ProjectItem project, SceneItemConfig config )
	{
		SceneItemImpl item = new SceneItemImpl( project, config, addressableRegistry, conversionService,
				scheduledExecutorService, componentRegistry, testRunner, counterSynchronizer, this );
		item.init();
		item.postInit();

		return item;
	}

	public ProjectItemImpl loadProject( WorkspaceItem workspace, File projectFile ) throws XmlException, IOException
	{
		ProjectItemImpl item = new ProjectItemImpl( addressableRegistry, conversionService, scheduledExecutorService,
				componentRegistry, testRunner, workspace, projectFile,
				projectFile.exists() ? LoaduiProjectDocumentConfig.Factory.parse( projectFile )
						: LoaduiProjectDocumentConfig.Factory.newInstance(), propertySynchronizer, counterSynchronizer, this );
		item.init();
		item.postInit();
		return item;
	}

	public ProjectRefImpl createProjectRefImpl( WorkspaceItemImpl workspaceItem, ProjectReferenceConfig config )
			throws IOException
	{
		ProjectRefImpl item = new ProjectRefImpl( workspaceItem, config, this );
		return item;
	}

	public ComponentItemImpl createComponentItemImpl( CanvasItem canvas, ComponentItemConfig config )
	{
		ComponentItemImpl item = new ComponentItemImpl( canvas, config, addressableRegistry, conversionService,
				scheduledExecutorService, counterSynchronizer, testRunner );
		item.init();
		item.postInit();
		return item;
	}

	public SceneItemImpl createSceneItemImpl( ProjectItem project, SceneItemConfig config )
	{
		SceneItemImpl item = new SceneItemImpl( project, config, addressableRegistry, conversionService,
				scheduledExecutorService, componentRegistry, testRunner, counterSynchronizer, this );
		item.init();
		item.postInit();
		return item;
	}

}

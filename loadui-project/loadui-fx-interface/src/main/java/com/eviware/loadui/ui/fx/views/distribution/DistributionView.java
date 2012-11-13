package com.eviware.loadui.ui.fx.views.distribution;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.views.agent.AgentView;
import com.google.common.base.Function;

public class DistributionView extends HBox
{
	private static final Function<SceneItem, ScenarioToolboxItem> SCENARIO_TO_VIEW = new Function<SceneItem, ScenarioToolboxItem>()
	{
		@Override
		public ScenarioToolboxItem apply( SceneItem scenario )
		{
			return new ScenarioToolboxItem( scenario );
		}
	};

	private static final Function<AgentItem, AgentView> AGENT_TO_VIEW = new Function<AgentItem, AgentView>()
	{
		@Override
		public AgentView apply( AgentItem agent )
		{
			return new AgentView( agent );
		}
	};

	private final Property<ProjectItem> project = new SimpleObjectProperty<>( this, "project" );
	private final Property<WorkspaceItem> workspace = new SimpleObjectProperty<>( this, "workspace" );
	private ObservableList<AgentItem> agents;
	private ObservableList<SceneItem> scenarios;
	private ObservableList<ScenarioToolboxItem> scenarioViews;
	private ObservableList<AgentView> agentViews;

	public DistributionView()
	{
		project.addListener( new ChangeListener<ProjectItem>()
		{
			@Override
			public void changed( ObservableValue<? extends ProjectItem> arg0, ProjectItem arg1, ProjectItem arg2 )
			{
				if( arg2 == null )
				{
					workspace.setValue( null );
					scenarios = FXCollections.emptyObservableList();
				}
				else
				{
					workspace.setValue( arg2.getWorkspace() );
					scenarios = ObservableLists.fx( ObservableLists.ofCollection( arg2, ProjectItem.SCENES, SceneItem.class,
							arg2.getChildren() ) );
				}

				scenarioViews = ObservableLists.transform( scenarios, SCENARIO_TO_VIEW );
			}
		} );

		workspace.addListener( new ChangeListener<WorkspaceItem>()
		{
			@Override
			public void changed( ObservableValue<? extends WorkspaceItem> arg0, WorkspaceItem arg1, WorkspaceItem arg2 )
			{
				if( arg2 == null )
				{
					agents = FXCollections.emptyObservableList();
				}
				else
				{
					agents = ObservableLists.fx( ( ObservableLists.ofCollection( arg2, WorkspaceItem.AGENTS,
							AgentItem.class, arg2.getAgents() ) ) );
				}

				agentViews = ObservableLists.transform( agents, AGENT_TO_VIEW );
			}
		} );
	}

	public ObservableList<SceneItem> getScenarios()
	{
		return scenarios;
	}

	public Property<ProjectItem> projectProperty()
	{
		return project;
	}

	public ProjectItem getProject()
	{
		return project.getValue();
	}

	public void setProject( ProjectItem value )
	{
		this.project.setValue( value );
	}
}

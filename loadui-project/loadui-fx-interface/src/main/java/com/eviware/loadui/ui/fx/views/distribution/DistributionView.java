package com.eviware.loadui.ui.fx.views.distribution;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.Assignment;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.ui.fx.control.PageList;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
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

	private final Function<AgentItem, AgentView> AGENT_TO_VIEW = new Function<AgentItem, AgentView>()
	{
		@Override
		public AgentView apply( AgentItem agent )
		{
			AgentView agentView = new AgentView( agent );
			Bindings.bindContent( agentView.getAssignments(), assignments );
			return agentView;
		}
	};

	private final Property<ProjectItem> project = new SimpleObjectProperty<>( this, "project" );
	private final Property<WorkspaceItem> workspace = new SimpleObjectProperty<>( this, "workspace" );
	private ObservableList<AgentItem> agents;
	private ObservableList<SceneItem> scenarios;
	private ObservableList<ScenarioToolboxItem> scenarioViews;
	private ObservableList<AgentView> agentViews;
	private ObservableList<Assignment> assignments = FXCollections.emptyObservableList();

	@FXML
	private ToolBox<ScenarioToolboxItem> scenarioToolBox;
	@FXML
	private PageList<AgentView> agentList;

	public DistributionView()
	{
		project.addListener( new ChangeListener<ProjectItem>()
		{
			@Override
			public void changed( ObservableValue<? extends ProjectItem> arg0, ProjectItem oldProject,
					ProjectItem newProject )
			{
				if( newProject == null )
				{
					workspace.setValue( null );
					scenarios = FXCollections.emptyObservableList();
				}
				else
				{
					workspace.setValue( newProject.getWorkspace() );
					scenarios = ObservableLists.fx( ObservableLists.ofCollection( newProject, ProjectItem.SCENES,
							SceneItem.class, newProject.getChildren() ) );
					ObservableList<Assignment> oldAssignments = assignments;
					assignments = ObservableLists.fx( ObservableLists.ofCollection( newProject, ProjectItem.ASSIGNMENTS,
							Assignment.class, newProject.getAssignments() ) );

					for( AgentView agent : agentViews )
					{
						Bindings.unbindContent( agent.getAssignments(), oldAssignments );
						Bindings.bindContent( agent.getAssignments(), assignments );
					}
				}

				scenarioViews = ObservableLists.transform( scenarios, SCENARIO_TO_VIEW );
				Bindings.bindContent( scenarioToolBox.getItems(), scenarioViews );
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
				Bindings.bindContent( agentList.getItems(), agentViews );
			}
		} );

		FXMLUtils.load( this );
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

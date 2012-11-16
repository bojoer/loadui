package com.eviware.loadui.ui.fx.views.agent;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import javax.annotation.Nullable;

import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.Assignment;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.api.input.DraggableEvent;
import com.eviware.loadui.ui.fx.control.OptionsSlider;
import com.eviware.loadui.ui.fx.control.ScrollableList;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.views.distribution.AssignmentView;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class AgentView extends VBox
{
	private static final Function<Assignment, AssignmentView> ASSIGNMENT_TO_VIEW = new Function<Assignment, AssignmentView>()
	{
		@Override
		@Nullable
		public AssignmentView apply( @Nullable Assignment assignment )
		{
			return new AssignmentView( assignment );
		}
	};

	@FXML
	private OptionsSlider onOffSwitch;

	@FXML
	private Label agentLabel;

	@FXML
	private MenuButton menuButton;

	@FXML
	private ScrollableList<Node> scenarioList;

	private final AgentItem agent;
	private final StringProperty labelProperty;
	private final StringProperty urlProperty;
	private final BooleanProperty enabledProperty;
	private final ReadOnlyBooleanProperty readyProperty;
	private final ObservableList<Assignment> assignments = FXCollections.observableArrayList();
	private final ObservableList<AssignmentView> assignmentViews;

	public AgentView( final AgentItem agent )
	{
		this.agent = agent;

		labelProperty = Properties.forLabel( agent );
		urlProperty = Properties.stringProperty( agent, "url", AgentItem.URL );
		enabledProperty = Properties.booleanProperty( agent, "enabled", AgentItem.ENABLED );
		readyProperty = Properties.readOnlyBooleanProperty( agent, "ready", AgentItem.READY );

		ObservableList<Assignment> filtered = ObservableLists.filter( assignments, new Predicate<Assignment>()
		{
			@Override
			public boolean apply( @Nullable Assignment assignment )
			{
				return assignment.getAgent().equals( agent );
			}
		} );

		assignmentViews = ObservableLists.transform( filtered, ASSIGNMENT_TO_VIEW );

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		//Hack for setting CSS resources within an OSGi framework
		String dotPatternUrl = AgentView.class.getResource( "dot-pattern.png" ).toExternalForm();
		lookup( ".dots" ).setStyle( "-fx-background-image: url('" + dotPatternUrl + "');" );

		Tooltip readyTooltip = new Tooltip();
		readyTooltip.textProperty().bind( Bindings.when( readyProperty ).then( "Connected" ).otherwise( "Disconnected" ) );
		onOffSwitch.setTooltip( readyTooltip );

		agentLabel.textProperty().bind( labelProperty );

		Tooltip menuTooltip = new Tooltip();
		menuTooltip.textProperty().bind( Bindings.format( "%s (%s)", labelProperty, urlProperty ) );
		menuButton.setTooltip( menuTooltip );

		enabledProperty.addListener( new ChangeListener<Boolean>()
		{
			@Override
			public void changed( ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean state )
			{
				onOffSwitch.setSelected( state.booleanValue() ? "ON" : "OFF" );
			}
		} );

		onOffSwitch.setSelected( enabledProperty.getValue().booleanValue() ? "ON" : "OFF" );

		onOffSwitch.selectedProperty().addListener( new ChangeListener<String>()
		{
			@Override
			public void changed( ObservableValue<? extends String> arg0, String arg1, String state )
			{
				enabledProperty.setValue( "ON".equals( state ) );
			}
		} );

		Bindings.bindContent( scenarioList.getItems(), assignmentViews );

		addEventHandler( DraggableEvent.ANY, new EventHandler<DraggableEvent>()
		{
			@Override
			public void handle( DraggableEvent event )
			{
				if( event.getData() instanceof SceneItem )
				{
					final SceneItem scenario = ( SceneItem )event.getData();

					if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED )
					{

						event.consume();
						if( !scenario.getProject().getAgentsAssignedTo( scenario ).contains( agent ) )
						{
							event.accept();
						}

					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
					{
						event.consume();
						scenario.getProject().assignScene( scenario, agent );
					}
				}
				else if( event.getData() instanceof Assignment )
				{
					final Assignment assignment = ( Assignment )event.getData();
					final SceneItem scenario = assignment.getScene();

					if( event.getEventType() == DraggableEvent.DRAGGABLE_STARTED )
					{
						// TODO make assignment invisible
						event.consume();
					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_ENTERED )
					{
						event.consume();

						if( !scenario.getProject().getAgentsAssignedTo( scenario ).contains( agent ) )
						{
							event.accept();
						}

					}
					else if( event.getEventType() == DraggableEvent.DRAGGABLE_DROPPED )
					{
						event.consume();

						if( !assignment.getAgent().equals( agent )
								&& !scenario.getProject().getAgentsAssignedTo( scenario ).contains( agent ) )
						{
							scenario.getProject().unassignScene( scenario, assignment.getAgent() );
							scenario.getProject().assignScene( scenario, agent );
						}
						else
						{
							// TODO make assignment visible again
						}
					}
				}
			}
		} );
	}

	public ObservableList<Assignment> getAssignments()
	{
		return assignments;
	}

	@Override
	public String toString()
	{
		return agent.getLabel();
	}

	@FXML
	public void delete()
	{
		//TODO: Show dialog?
		agent.delete();
	}
}
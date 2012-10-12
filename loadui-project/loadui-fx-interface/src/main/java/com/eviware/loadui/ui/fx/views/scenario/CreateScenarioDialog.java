package com.eviware.loadui.ui.fx.views.scenario;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;
import com.eviware.loadui.ui.fx.control.fields.ValidatableStringField;
import com.eviware.loadui.ui.fx.control.fields.ValidatableTextField;

public class CreateScenarioDialog extends ConfirmationDialog
{
	private static final Logger log = LoggerFactory.getLogger( CreateScenarioDialog.class );

	private final ValidatableStringField scenarioNameField;
	private final ProjectItem project;
	private final Point2D position;

	public CreateScenarioDialog( final Node owner, final ProjectItem project, final Point2D position )
	{
		super( owner, "New Scenario in: " + project.getLabel(), "Create" );
		this.project = project;
		this.position = position;
		this.scenarioNameField = ValidatableStringField.Builder.create()
				.stringConstraint( ValidatableStringField.NOT_EMPTY ).id( "scenario-name" ).build();

		getItems().add( this.scenarioNameField );

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				if( scenarioNameField.validate() )
				{
					close();
					fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, new CreateScenarioTask() ) );
				}
			}
		} );
	}

	public class CreateScenarioTask extends Task<Void>
	{
		@Override
		protected Void call() throws Exception
		{
			log.debug( "About to create scenario: " + scenarioNameField.getText() );
			SceneItem scenario = project.createScene( scenarioNameField.getText() );
			scenario.setAttribute( "gui.layoutX", String.valueOf( ( int )position.getX() ) );
			scenario.setAttribute( "gui.layoutY", String.valueOf( ( int )position.getY() ) );
			return null;
		}
	}
}

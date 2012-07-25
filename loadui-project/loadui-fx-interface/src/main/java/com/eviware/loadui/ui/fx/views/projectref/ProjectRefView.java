package com.eviware.loadui.ui.fx.views.projectref;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;

public class ProjectRefView extends StackPane
{
	private final ProjectRef projectRef;
	private final ReadOnlyStringProperty labelProperty;

	public ProjectRefView( final ProjectRef projectRef )
	{
		this.projectRef = projectRef;
		labelProperty = Properties.forLabel( projectRef );

		setPrefWidth( 130 );
		setMaxHeight( 95 );

		getChildren().setAll( FXMLUtils.load( ProjectRefView.class, new Callable<Object>()
		{
			@Override
			public Object call() throws Exception
			{
				return new Controller();
			}
		} ) );
	}

	public ProjectRef getProjectRef()
	{
		return projectRef;
	}

	public void openProject()
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, projectRef ) );
	}

	@Override
	public String toString()
	{
		return labelProperty.get();
	}

	public final class Controller implements Initializable
	{
		@FXML
		private ToggleButton onOffSwitch;

		@FXML
		private MenuButton menuButton;

		@Override
		public void initialize( URL arg0, ResourceBundle arg1 )
		{
			menuButton.textProperty().bind( labelProperty );

			Tooltip menuTooltip = new Tooltip();
			menuTooltip.textProperty().bind(
					Bindings.format( "%s (%s)", labelProperty, projectRef.getProjectFile().getAbsolutePath() ) );
			menuButton.setTooltip( menuTooltip );
		}

		public void open()
		{
			fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, projectRef ) );
		}

		public void cloneProject()
		{
			fireEvent( IntentEvent.create( IntentEvent.INTENT_CLONE, projectRef ) );
		}

		public void delete()
		{
			//TODO: Show dialog.
			projectRef.delete( false );
		}

		public void regionClickHandler( MouseEvent event )
		{
			if( event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 )
			{
				open();
			}
		}
	}
}

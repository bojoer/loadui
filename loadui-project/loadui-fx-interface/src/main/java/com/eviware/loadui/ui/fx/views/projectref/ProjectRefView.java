package com.eviware.loadui.ui.fx.views.projectref;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.google.common.base.Preconditions;

public class ProjectRefView extends StackPane implements Labeled
{
	@FXML
	private ToggleButton onOffSwitch;

	@FXML
	private MenuButton menuButton;

	private final ProjectRef projectRef;
	private final ReadOnlyStringProperty labelProperty;

	public ProjectRefView( final ProjectRef projectRef )
	{
		this.projectRef = Preconditions.checkNotNull( projectRef );

		FXMLUtils.load( this );

		labelProperty = Properties.forLabel( projectRef );

		setPrefWidth( 130 );
		setMaxHeight( 95 );

		menuButton.textProperty().bind( labelProperty );

		Tooltip menuTooltip = new Tooltip();
		menuTooltip.textProperty().bind(
				Bindings.format( "%s (%s)", labelProperty, projectRef.getProjectFile().getAbsolutePath() ) );
		menuButton.setTooltip( menuTooltip );

	}

	public ReadOnlyStringProperty labelProperty()
	{
		return labelProperty;
	}

	@Override
	public String getLabel()
	{
		return labelProperty.get();
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

	public void open()
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, projectRef ) );
	}

	public void rename()
	{
		fireEvent( IntentEvent.create( IntentEvent.INTENT_RENAME, projectRef ) );
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

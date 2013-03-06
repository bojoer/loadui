package com.eviware.loadui.ui.fx.views.projectref;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.ui.fx.views.workspace.ProjectMenuItemsProvider;
import com.eviware.loadui.ui.fx.views.workspace.ProjectMenuItemsProvider.ProjectMenuItemHolder;
import com.google.common.base.Preconditions;

public class ProjectRefView extends StackPane implements Labeled
{
	@FXML
	private ToggleButton onOffSwitch;

	@FXML
	private MenuButton menuButton;

	@FXML
	private ImageView miniature;

	private final ProjectRef projectRef;
	private final ReadOnlyStringProperty labelProperty;
	private final ProjectMenuItemHolder menuItemProvider;

	public ProjectRefView( final ProjectRef projectRef )
	{
		this.projectRef = Preconditions.checkNotNull( projectRef );
		this.labelProperty = Properties.forLabel( projectRef );
		menuItemProvider = ProjectMenuItemsProvider.createWith( this, projectRef );

		FXMLUtils.load( this );

	}

	@FXML
	private void initialize()
	{
		setPrefWidth( 130 );
		setMaxHeight( 95 );
		setId( UIUtils.toCssId( projectRef.getLabel() ) );

		menuButton.textProperty().bind( labelProperty );

		String base64 = projectRef.getAttribute( "miniature_fx2", null );
		if( base64 == null )
			miniature.setImage( new Image( ProjectRefView.class.getResource( "grid.png" ).toExternalForm() ) );
		else
			miniature.setImage( NodeUtils.fromBase64Image( base64 ) );

		Tooltip menuTooltip = new Tooltip();
		menuTooltip.textProperty().bind(
				Bindings.format( "%s (%s)", labelProperty, projectRef.getProjectFile().getAbsolutePath() ) );
		menuButton.setTooltip( menuTooltip );
		menuButton.getItems().setAll( menuItemProvider.items() );

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

	@Override
	public String toString()
	{
		return labelProperty.get();
	}

	@FXML
	public void regionClickHandler( MouseEvent event )
	{
		if( event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 )
		{
			menuItemProvider.openProject();
			event.consume();
		}
	}

}

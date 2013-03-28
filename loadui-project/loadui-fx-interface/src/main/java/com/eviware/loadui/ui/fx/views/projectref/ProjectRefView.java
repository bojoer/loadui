/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
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

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.MenuItemsProvider;
import com.eviware.loadui.ui.fx.MenuItemsProvider.HasMenuItems;
import com.eviware.loadui.ui.fx.MenuItemsProvider.Options;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.NodeUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.ui.fx.views.workspace.WorkspaceView;
import com.google.common.base.Preconditions;

public class ProjectRefView extends StackPane implements Labeled
{
	public static final Options MENU_ITEM_OPTIONS = Options.are().open().clone().delete( "Delete", true )
			.create( ProjectItem.class, WorkspaceView.CREATE_PROJECT );

	@FXML
	private ToggleButton onOffSwitch;

	@FXML
	private MenuButton menuButton;

	@FXML
	private ImageView miniature;

	private final ProjectRef projectRef;
	private final ReadOnlyStringProperty labelProperty;
	private final HasMenuItems menuItemProvider;

	public ProjectRefView( final ProjectRef projectRef )
	{
		this.projectRef = Preconditions.checkNotNull( projectRef );
		this.labelProperty = Properties.forLabel( projectRef );
		menuItemProvider = MenuItemsProvider.createWith( this, projectRef, MENU_ITEM_OPTIONS );

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

	public HasMenuItems getMenuItemProvider()
	{
		return menuItemProvider;
	}

	public MenuButton getMenuButton()
	{
		return menuButton;
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
			fireEvent( IntentEvent.create( IntentEvent.INTENT_OPEN, projectRef ) );
			event.consume();
		}
	}

}

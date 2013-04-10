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
package com.eviware.loadui.ui.fx.control;

import static com.eviware.loadui.ui.fx.util.NodeUtils.bindStyleClass;
import static javafx.beans.binding.Bindings.size;

import java.util.List;

import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;
import javafx.stage.WindowEvent;

import javax.annotation.Nonnull;

public class SettingsDialog extends ConfirmationDialog
{
	public static final double VERTICAL_SPACING = 12;
	@Nonnull
	public final TabPane tabPane = new TabPane();

	@Nonnull
	private final List<? extends SettingsTab> tabs;

	public final ObservableBooleanValue hasExactlyOneTab;

	public SettingsDialog( @Nonnull Node owner, @Nonnull String title, @Nonnull final List<? extends SettingsTab> tabs )
	{
		super( owner, title, "Save" );
		this.tabs = tabs;

		refereshTabs( tabs );
		getItems().add( tabPane );

		setOnConfirm( new OnSaveHandler() );
		addStyleClass( "settings-dialog" );

		hasExactlyOneTab = size( tabPane.getTabs() ).isEqualTo( 1 );
		bindStyleClass( tabPane, "single-tab", hasExactlyOneTab );

		setOnShowing( new EventHandler<WindowEvent>()
		{
			@Override
			public void handle( WindowEvent arg0 )
			{
				refereshTabs( tabs );
			}
		} );

		setOnShown( new EventHandler<WindowEvent>()
		{
			@Override
			public void handle( WindowEvent _ )
			{
				final Region tabHeader = ( Region )tabPane.lookup( ".tab-header-area" );
				final double headerHeight = tabHeader.getHeight();

				if( hasExactlyOneTab.get() )
				{
					SettingsDialog.this.setHeight( SettingsDialog.this.getHeight() - headerHeight );
					tabHeader.setPrefHeight( 0.0 );
				}
			}
		} );
	}

	private void refereshTabs( List<? extends SettingsTab> tabs )
	{
		for( SettingsTab tab : tabs )
		{
			tab.refreshFields();
		}

		tabPane.getTabs().clear();
		tabPane.getTabs().addAll( tabs );
	}

	public class OnSaveHandler implements EventHandler<ActionEvent>
	{
		@Override
		public void handle( ActionEvent event )
		{
			boolean wasValid = true;
			for( SettingsTab tab : tabs )
			{
				boolean tabIsValid = tab.validate();
				if( !tabIsValid )
					tab.getTabPane().getSelectionModel().select( tab );
				wasValid = wasValid && tabIsValid;
			}
			if( wasValid )
			{
				for( SettingsTab tab : tabs )
				{
					tab.save();
				}
				close();
			}
		}
	}
}

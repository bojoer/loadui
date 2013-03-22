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

	public SettingsDialog( @Nonnull Node owner, @Nonnull String title, @Nonnull List<? extends SettingsTab> tabs )
	{
		super( owner, title, "Save" );
		this.tabs = tabs;

		for( SettingsTab tab : tabs )
		{
			tab.refreshFields();
		}

		tabPane.getTabs().addAll( tabs );
		getItems().add( tabPane );
		setOnConfirm( new OnSaveHandler() );
		addStyleClass( "settings-dialog" );

		hasExactlyOneTab = size( tabPane.getTabs() ).isEqualTo( 1 );
		bindStyleClass( tabPane, "single-tab", hasExactlyOneTab );

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
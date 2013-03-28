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
import static javafx.beans.binding.Bindings.equal;
import static javafx.beans.binding.Bindings.not;
import static javafx.beans.binding.Bindings.when;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.StackPaneBuilder;

import javax.annotation.Nonnull;

public class Wizard extends ConfirmationDialog
{
	@Nonnull
	private final TabPane tabPane = new TabPane();

	private final BooleanProperty isFirstStep = new SimpleBooleanProperty();
	private final BooleanProperty isLastStep = new SimpleBooleanProperty();

	public Wizard( Node owner, String title, @Nonnull final List<SettingsTab> tabs )
	{
		super( owner, title, "", true, false );
		tabPane.getTabs().addAll( tabs );
		addStyleClass( "wizard" );
		isFirstStep.bind( equal( 0, tabPane.getSelectionModel().selectedIndexProperty() ) );
		isLastStep.bind( equal( tabs.size() - 1, tabPane.getSelectionModel().selectedIndexProperty() ) );

		confirmationTextProperty().bind( when( isLastStep ).then( "Finish" ).otherwise( "Next >" ) );

		HBox steps = HBoxBuilder.create().spacing( 18.0 ).build();
		int i = 0;
		for( Tab t : tabs )
		{
			steps.getChildren().add( new StepIndicator( i++ , t.getText() ) );
		}
		getItems().setAll( steps, tabPane );

		tabPane.setTabMaxHeight( 0.0 );

		Button backButton = ButtonBuilder.create().text( "< Back" ).onAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				tabPane.getSelectionModel().selectPrevious();
			}
		} ).build();
		backButton.visibleProperty().bind( not( isFirstStep ) );
		getButtons().add( 2, backButton );

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent _ )
			{
				if( ( ( SettingsTab )tabPane.getSelectionModel().getSelectedItem() ).validate() )
				{
					if( isLastStep.get() )
					{
						for( SettingsTab tab : tabs )
						{
							tab.save();
						}
						close();
					}
					else
					{
						tabPane.getSelectionModel().selectNext();
					}
				}
			}
		} );
	}

	public class StepIndicator extends Label
	{
		private final ObservableBooleanValue isCurrentStep;

		public StepIndicator( final int stepIndex, String label )
		{
			setText( label );
			Label stepNumber = LabelBuilder.create().alignment( Pos.CENTER ).prefHeight( 16 ).prefWidth( 16 )
					.text( String.valueOf( stepIndex + 1 ) ).build();
			stepNumber.getStyleClass().add( "step-dot" );
			isCurrentStep = equal( stepIndex, tabPane.getSelectionModel().selectedIndexProperty() );
			bindStyleClass( stepNumber, "current-step", isCurrentStep );
			setGraphic( StackPaneBuilder.create().children( stepNumber ).build() );
		}
	}

}

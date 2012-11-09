package com.eviware.loadui.ui.fx.control;

import static com.eviware.loadui.ui.fx.util.NodeUtils.bindStyleClass;
import static javafx.beans.binding.Bindings.equal;
import static javafx.beans.binding.Bindings.when;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
	@Nonnull
	private final List<SettingsTab> tabs;

	private final BooleanProperty isLastStep = new SimpleBooleanProperty();

	public Wizard( Node owner, String title, @Nonnull List<SettingsTab> tabs )
	{
		super( owner, title, "", true );
		this.tabs = tabs;
		tabPane.getTabs().addAll( tabs );
		addStyleClass( "wizard" );
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

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent e )
			{
				if( isLastStep.get() )
					close();
				else
					tabPane.getSelectionModel().selectNext();
			}
		} );
	}

	public class StepIndicator extends Label
	{
		private final ObservableBooleanValue isCurrentStep;

		public StepIndicator( final int stepIndex, String label )
		{
			setText( label );
			Label l = LabelBuilder.create().alignment( Pos.CENTER ).prefHeight( 16 ).prefWidth( 16 )
					.text( String.valueOf( stepIndex + 1 ) ).build();
			l.getStyleClass().add( "step-dot" );
			isCurrentStep = equal( stepIndex, tabPane.getSelectionModel().selectedIndexProperty() );
			bindStyleClass( l, "current-step", isCurrentStep );
			setGraphic( StackPaneBuilder.create().children( l ).build() );
		}
	}

}
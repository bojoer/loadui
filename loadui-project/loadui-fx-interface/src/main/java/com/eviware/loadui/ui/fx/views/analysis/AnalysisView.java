package com.eviware.loadui.ui.fx.views.analysis;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;

public class AnalysisView extends StackPane
{
	private final ProjectItem project;
	private final ObservableList<Execution> executionList;

	private final Property<Execution> currentExecutionProperty = new SimpleObjectProperty<>();

	public Property<Execution> currentExecutionProperty()
	{
		return currentExecutionProperty;
	}

	public void setCurrentExecution( Execution currentExecution )
	{
		currentExecutionProperty.setValue( currentExecution );
	}

	public Execution getCurrentExecution()
	{
		return currentExecutionProperty.getValue();
	}

	public AnalysisView( ProjectItem project, ObservableList<Execution> executionList )
	{
		this.project = project;
		this.executionList = executionList;

		getChildren().add( FXMLUtils.load( AnalysisView.class, new Callable<Object>()
		{
			@Override
			public Object call() throws Exception
			{
				return new Controller();
			}
		} ) );
	}

	public final class Controller implements Initializable
	{
		@FXML
		private Label executionLabel;

		@FXML
		private StackPane chartContainer;

		@Override
		public void initialize( URL arg0, ResourceBundle arg1 )
		{
			currentExecutionProperty.addListener( new ChangeListener<Execution>()
			{
				@Override
				public void changed( ObservableValue<? extends Execution> arg0, Execution arg1, Execution arg2 )
				{
					executionLabel.textProperty().unbind();
					executionLabel.textProperty().bind( Properties.forLabel( arg2 ) );
				}
			} );

			try
			{
				LineChartView chartView = ( LineChartView )project.getStatisticPages().getChildAt( 0 ).getChildAt( 0 )
						.getChartView();

				chartContainer.getChildren().setAll( new LineChartViewNode( currentExecutionProperty, chartView ) );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}

		public void close()
		{
			AnalysisView.this.fireEvent( IntentEvent.create( IntentEvent.INTENT_CLOSE, getCurrentExecution() ) );
		}
	}
}

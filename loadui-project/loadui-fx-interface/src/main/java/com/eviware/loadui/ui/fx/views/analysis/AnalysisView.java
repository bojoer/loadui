package com.eviware.loadui.ui.fx.views.analysis;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.Properties;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class AnalysisView extends StackPane
{
	private static final Logger log = LoggerFactory.getLogger( AnalysisView.class );

	@FXML
	private Label executionLabel;

	@FXML
	private StackPane chartContainer;

	private final ProjectItem project;
	private final ObservableList<Execution> executionList;

	private final Property<Execution> currentExecution = new SimpleObjectProperty<>( this, "currentExecution" );

	public Property<Execution> currentExecutionProperty()
	{
		return currentExecution;
	}

	public void setCurrentExecution( Execution value )
	{
		currentExecution.setValue( value );
	}

	public Execution getCurrentExecution()
	{
		return currentExecution.getValue();
	}

	public AnalysisView( ProjectItem project, ObservableList<Execution> executionList )
	{
		this.project = project;
		this.executionList = executionList;

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		currentExecution.addListener( new ChangeListener<Execution>()
		{
			@Override
			public void changed( ObservableValue<? extends Execution> arg0, Execution arg1, Execution arg2 )
			{
				executionLabel.textProperty().unbind();
				System.out.println( "Changed currentExecution to: " + arg2 );
				executionLabel.textProperty().bind( Properties.forLabel( arg2 ) );
			}
		} );

		try
		{
			if( project.getStatisticPages().getChildCount() == 0 )
			{
				StatisticPage page = project.getStatisticPages().createPage( "New Page" );
				ChartGroup group = page.createChartGroup( LineChartView.class.getName(), "New Chart" );
				group.createChart( Iterables.find( project.getComponents(), new Predicate<ComponentItem>()
				{
					@Override
					public boolean apply( ComponentItem input )
					{
						return input.getLabel().startsWith( "Web" );
					}
				} ) );
			}
			LineChartView chartView = ( LineChartView )project.getStatisticPages().getChildAt( 0 ).getChildAt( 0 )
					.getChartView();

			chartContainer.getChildren().setAll( new LineChartViewNode( currentExecution, chartView ) );
		}
		catch( Exception e )
		{
			log.error( "Unable to initialize line chart view for statistics analysis", e );
		}
	}

	public void close()
	{
		AnalysisView.this.fireEvent( IntentEvent.create( IntentEvent.INTENT_CLOSE, getCurrentExecution() ) );
	}
}

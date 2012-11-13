package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;

import java.util.Collection;

import javax.annotation.Nullable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Properties;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

public class AnalysisView extends StackPane
{
	private static final Logger log = LoggerFactory.getLogger( AnalysisView.class );

	@FXML
	private Label executionLabel;

	@FXML
	private TabPane tabPane;

	@FXML
	private StackPane chartContainer;

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

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
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
			StatisticPages pagesObject = project.getStatisticPages();

			if( pagesObject.getChildCount() == 0 )
				pagesObject.createPage( "General" );

			ObservableList<StatisticPage> statisticPages = ObservableLists.ofCollection( pagesObject );

			ObservableList<Tab> tabs = transform( statisticPages, new Function<StatisticPage, Tab>()
			{
				@Override
				@Nullable
				public Tab apply( @Nullable StatisticPage input )
				{
					return new Tab( input.getTitle() );
				}
			} );

			Bindings.bindContent( tabPane.getTabs(), tabs );

			//			LineChartView chartView = ( LineChartView )project.getStatisticPages().getChildAt( 0 ).getChildAt( 0 )
			//					.getChartView();

			//			chartContainer.getChildren().setAll( new LineChartViewNode( currentExecutionProperty, chartView ) );
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

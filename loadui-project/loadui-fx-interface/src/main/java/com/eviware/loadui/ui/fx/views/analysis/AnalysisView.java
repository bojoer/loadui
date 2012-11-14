package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.ObservableLists.appendElement;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static javafx.beans.binding.Bindings.bindContent;

import java.util.Random;

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabBuilder;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.ComponentItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.api.statistics.model.chart.line.LineChartView;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.views.statistics.StatisticHolderToolBox;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class AnalysisView extends StackPane
{
	private static final Logger log = LoggerFactory.getLogger( AnalysisView.class );

	private final Function<StatisticPage, Tab> STATISTIC_PAGE_TO_TAB = new Function<StatisticPage, Tab>()
	{
		@Override
		@Nullable
		public StatisticTab apply( @Nullable StatisticPage page )
		{
			return new StatisticTab( page );
		}
	};

	@FXML
	private Label executionLabel;

	@FXML
	private TabPane tabPane;

	@FXML
	private StatisticHolderToolBox toolBox;

	private final ProjectItem project;
	private final ObservableList<Execution> executionList;
	private final Observable poll;

	private final Property<Execution> currentExecution = new SimpleObjectProperty<>( this, "currentExecution" );

	@FXML
	private Button button;

	private StatisticPages pagesObject;

	private ObservableList<StatisticPage> statisticPages;

	private ObservableList<Tab> tabs;

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

	public AnalysisView( ProjectItem project, ObservableList<Execution> executionList, Observable poll )
	{
		this.project = project;
		this.executionList = executionList;
		this.poll = poll;

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
				executionLabel.textProperty().bind( Properties.forLabel( arg2 ) );
			}
		} );

		try
		{
			pagesObject = project.getStatisticPages();

			if( project.getStatisticPages().getChildCount() == 0 )
			{
				StatisticPage page = project.getStatisticPages().createPage( "General" );
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

			statisticPages = ObservableLists.ofCollection( pagesObject );

			tabs = transform( ObservableLists.fx( statisticPages ), STATISTIC_PAGE_TO_TAB );

			final Tab plusButton = TabBuilder.create().text( "+" ).closable( false ).styleClass( "create-new-button" )
					.build();
			plusButton.setOnSelectionChanged( new EventHandler<Event>()
			{
				@Override
				public void handle( Event e )
				{
					if( plusButton.isSelected() )
					{
						pagesObject.createPage( "new page " + new Random().nextInt() );
					}
				}
			} );

			button = ButtonBuilder.create().text( "+" ).build();

			bindContent( tabPane.getTabs(), appendElement( tabs, plusButton ) );

			tabPane.getTabs().addListener( new ListChangeListener<Tab>()
			{
				@Override
				public void onChanged( javafx.collections.ListChangeListener.Change<? extends Tab> c )
				{
					while( c.next() )
						if( c.wasAdded() )
							tabPane.getSelectionModel().select( c.getAddedSubList().get( 0 ) );
				}
			} );

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

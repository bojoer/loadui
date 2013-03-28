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
package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.ObservableLists.appendElement;
import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.optimize;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabBuilder;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.model.StatisticPages;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionsInfo;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionsInfo.Data;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.views.result.ResultsPopup;
import com.google.common.base.Function;

public class AnalysisView extends StackPane
{
	protected static final Logger log = LoggerFactory.getLogger( AnalysisView.class );

	private static final String UNTITLED_PAGE_PREFIX = "Untitled Page ";

	private final EventHandler<Event> createNewTab = new EventHandler<Event>()
	{
		@Override
		public void handle( Event _ )
		{
			if( !plusButton.isSelected() )
				return;

			int maxNumber = 0;
			for( StatisticPage page : pagesObject.getChildren() )
			{
				String label = page.getLabel();
				if( label.startsWith( UNTITLED_PAGE_PREFIX ) )
				{
					try
					{
						int number = Integer.parseInt( label.substring( UNTITLED_PAGE_PREFIX.length() ) );
						maxNumber = Math.max( number, maxNumber );
					}
					catch( NumberFormatException e )
					{
						// ignore
					}
				}
			}

			pagesObject.createPage( UNTITLED_PAGE_PREFIX + ++maxNumber );
		}
	};

	private final Function<StatisticPage, Tab> statisticsPageToTab = new Function<StatisticPage, Tab>()
	{
		@Override
		@Nullable
		public StatisticTab apply( @Nullable StatisticPage page )
		{
			final StatisticTab statsTab = new StatisticTab( page, poll );
			executionsInfo.runWhenReady( new Callback<Data, Void>()
			{

				@Override
				public Void call( Data data )
				{
					statsTab.setCurrentExecution( data.getCurrentExecution() );
					return null;
				}
			} );
			return statsTab;
		}
	};

	@FXML
	private Label executionLabel;

	@FXML
	private TabPane tabPane;

	@FXML
	private AnalysisToolBox toolBox;

	@FXML
	private HBox analysisToolbar;

	private final ProjectItem project;
	private final Observable poll;
	private Tab plusButton;
	private StatisticPages pagesObject;
	private ObservableList<Tab> allTabs;
	private final ExecutionsInfo executionsInfo;

	public AnalysisView( ProjectItem project, Observable poll, ExecutionsInfo executionsInfo )
	{
		this.project = project;
		this.poll = poll;
		this.executionsInfo = executionsInfo;

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		toolBox.setProject( project );

		executionsInfo.runWhenReady( new Callback<ExecutionsInfo.Data, Void>()
		{
			@Override
			public Void call( Data data )
			{
				setCurrentExecutionLabelTo( data.getCurrentExecution().getValue() );
				data.getCurrentExecution().addListener( new ChangeListener<Execution>()
				{
					@Override
					public void changed( ObservableValue<? extends Execution> arg0, Execution arg1, Execution newExecution )
					{
						setCurrentExecutionLabelTo( newExecution );
					}
				} );
				return null;
			}
		} );

		try
		{
			pagesObject = project.getStatisticPages();

			if( project.getStatisticPages().getChildCount() == 0 )
			{
				project.getStatisticPages().createPage( UNTITLED_PAGE_PREFIX + "1" );
			}

			final ObservableList<StatisticPage> statisticPages = ofCollection( pagesObject );

			final ObservableList<Tab> tabs = transform( fx( statisticPages ), statisticsPageToTab );

			plusButton = TabBuilder.create().id( "plus-button" ).text( "+" ).closable( false )
					.onSelectionChanged( createNewTab ).styleClass( "create-new-button" ).build();

			allTabs = optimize( appendElement( tabs, plusButton ) );
			allTabs.addListener( new ListChangeListener<Tab>()
			{
				@Override
				public void onChanged( ListChangeListener.Change<? extends Tab> change )
				{
					while( change.next() )
					{
						tabPane.getTabs().removeAll( change.getRemoved() );
						for( Tab newTab : change.getAddedSubList() )
						{
							tabPane.getTabs().add( allTabs.indexOf( newTab ), newTab );
						}
					}
				}
			} );
			tabPane.getTabs().setAll( allTabs );

			// add a listener to the tabPane tabs collection itself, which is not the same as allTabs!
			tabPane.getTabs().addListener( new ListChangeListener<Tab>()
			{
				@Override
				public void onChanged( ListChangeListener.Change<? extends Tab> c )
				{
					while( c.next() )
					{
						if( c.wasAdded() )
						{
							Tab newTab = c.getAddedSubList().get( 0 );
							if( newTab != plusButton )
								tabPane.getSelectionModel().select( newTab );
						}
					}
				}
			} );

		}
		catch( Exception e )
		{
			log.error( "Unable to initialize line chart view for statistics analysis", e );
		}
	}

	private void setCurrentExecutionLabelTo( Execution execution )
	{
		executionLabel.setText( execution == null ? "No Execution" : execution.getLabel() );
	}

	public HBox getButtonContainer()
	{
		return analysisToolbar;
	}

	@SuppressWarnings( "resource" )
	// resultsPopup closeable is closed by the ResultView
	@FXML
	protected void openPreviousRuns()
	{
		new ResultsPopup( this, executionsInfo ).show();
	}

}

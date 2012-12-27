package com.eviware.loadui.ui.fx.views.result;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static javafx.beans.binding.Bindings.bindContent;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.control.PageList;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.google.common.base.Function;

public class ResultView extends StackPane
{
	protected static final Logger log = LoggerFactory.getLogger( ResultView.class );

	@FXML
	private PageList<ExecutionView> resultNodeList;
	
	@FXML
	private PageList<ExecutionView> currentResultNode;

	@FXML
	private PageList<ExecutionView> archiveNodeList;
	
	private final Property<Execution> currentExecution;
	private final ObservableList<Execution> executionList;
	private ObservableList<ExecutionView> executionViews;

	public ResultView( Property<Execution> currentExecution, ObservableList<Execution> executionList )
	{
		this.currentExecution = currentExecution;
		this.executionList = executionList;

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		currentExecution.addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable _ )
			{
				currentResultNode.getItems().setAll( new ExecutionView( currentExecution.getValue() ) );
			}
		} );

		executionViews = fx( transform( executionList, new Function<Execution, ExecutionView>()
		{
			@Override
			public ExecutionView apply( Execution e )
			{
				return new ExecutionView( e );
			}
		} ) );
		bindContent( resultNodeList.getItems(), executionViews );

		executionViews.addListener( new ListChangeListener<ExecutionView>()
		{
			@Override
			public void onChanged( Change<? extends ExecutionView> c )
			{
				for( ExecutionView e : executionViews )
					e.setId( "result-" + Integer.toString( executionViews.indexOf( e ) ) );
			}
		} );
	}
}

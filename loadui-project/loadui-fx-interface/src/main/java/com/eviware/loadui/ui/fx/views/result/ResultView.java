package com.eviware.loadui.ui.fx.views.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.control.PageList;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.views.analysis.AnalysisView;
import com.google.common.base.Function;

public class ResultView extends StackPane
{
	protected static final Logger log = LoggerFactory.getLogger( ResultView.class );

	@FXML
	private PageList<ExecutionView> resultNodeList;

	private final ObservableList<Execution> executionList;

	public ResultView( ObservableList<Execution> executionList )
	{
		this.executionList = executionList;

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		Bindings.bindContent( resultNodeList.getItems(),
				ObservableLists.fx( ObservableLists.transform( this.executionList, new Function<Execution, ExecutionView>()
				{
					@Override
					public ExecutionView apply( Execution projectRef )
					{
						return new ExecutionView( projectRef );
					}
				} ) ) );

	}

}

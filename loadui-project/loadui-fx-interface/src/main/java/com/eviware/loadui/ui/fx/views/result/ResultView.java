package com.eviware.loadui.ui.fx.views.result;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import static javafx.beans.binding.Bindings.bindContent;
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

	private final ObservableList<Execution> executionList;
	private ObservableList<ExecutionView> executionViews;

	public ResultView( ObservableList<Execution> executionList )
	{
		this.executionList = executionList;

		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		executionViews = fx( transform( executionList, new Function<Execution, ExecutionView>()
		{
			@Override
			public ExecutionView apply( Execution projectRef )
			{
				return new ExecutionView( projectRef );
			}
		} ) );
		bindContent( resultNodeList.getItems(), executionViews );

	}
}

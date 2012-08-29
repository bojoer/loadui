package com.eviware.loadui.ui.fx.views.result;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.google.common.base.Function;

public class ResultView extends StackPane
{
	@FXML
	private ListView<ExecutionNode> resultNodeList;

	private final ObservableList<Execution> executionList;

	public ResultView( ObservableList<Execution> executionList )
	{
		this.executionList = executionList;

		FXMLUtils.load( this );

		Bindings.bindContent( resultNodeList.getItems(),
				ObservableLists.fx( ObservableLists.transform( executionList, new Function<Execution, ExecutionNode>()
				{
					@Override
					public ExecutionNode apply( Execution projectRef )
					{
						return new ExecutionNode( projectRef );
					}
				} ) ) );

	}

}

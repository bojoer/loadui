package com.eviware.loadui.ui.fx.views.result;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.control.PageList;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.google.common.base.Function;

public class ResultView extends StackPane
{
	@FXML
	private PageList<ExecutionNode> resultNodeList;

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
				ObservableLists.fx( ObservableLists.transform( this.executionList, new Function<Execution, ExecutionNode>()
				{
					@Override
					public ExecutionNode apply( Execution projectRef )
					{
						return new ExecutionNode( projectRef );
					}
				} ) ) );
	}

}

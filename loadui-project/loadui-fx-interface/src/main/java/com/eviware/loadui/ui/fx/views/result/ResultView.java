package com.eviware.loadui.ui.fx.views.result;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.google.common.base.Function;

public class ResultView extends StackPane
{
	private final ObservableList<Execution> executionList;

	public ResultView( ObservableList<Execution> executionList )
	{
		this.executionList = executionList;

		getChildren().add( FXMLUtils.load( ResultView.class, new Callable<Object>()
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
		private ListView<ExecutionNode> resultNodeList;

		@Override
		public void initialize( URL arg0, ResourceBundle arg1 )
		{
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
}

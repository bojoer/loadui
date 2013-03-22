package com.eviware.loadui.ui.fx.views.result;

import java.io.Closeable;

import javafx.scene.Node;
import javafx.util.Callback;

import com.eviware.loadui.ui.fx.api.analysis.ExecutionsInfo;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionsInfo.Data;
import com.eviware.loadui.ui.fx.control.Dialog;

public class ResultsPopup extends Dialog implements Callback<Data, Void>, Closeable
{

	public ResultsPopup( Node owner, ExecutionsInfo executionsInfo )
	{
		super( owner, "Previous results" );
		getScene().getRoot().setStyle( "-fx-padding: 0;" );
		executionsInfo.runWhenReady( this );
	}
	
	@Override
	public Void call( Data data )
	{
		
		ResultView resultView = new ResultView( data.getRecentExecutions(), data.getArchivedExecutions(), this );
		getItems().setAll( resultView );
		return null;
	}

}

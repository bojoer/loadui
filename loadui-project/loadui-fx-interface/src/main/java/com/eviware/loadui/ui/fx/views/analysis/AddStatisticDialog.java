package com.eviware.loadui.ui.fx.views.analysis;

import static javafx.beans.binding.Bindings.not;

import java.util.Collection;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;

public class AddStatisticDialog extends ConfirmationDialog
{
	protected static final Logger log = LoggerFactory.getLogger( AddStatisticDialog.class );
	private final StatisticTree tree;

	public AddStatisticDialog( Node owner, Collection<StatisticHolder> holders )
	{
		super( owner, "Add Statistic", "Add" );

		tree = StatisticTree.forHolders( holders );

		confirmDisableProperty().bind( not( tree.isValidProperty() ) );

		HBox hBox = HBoxBuilder.create().children( tree ).build();
		getItems().add( hBox );
	}

	public StatisticTree.Selection getSelection()
	{
		return tree.getSelection();
	}
}

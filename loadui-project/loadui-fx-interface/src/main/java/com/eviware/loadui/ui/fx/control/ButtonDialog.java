package com.eviware.loadui.ui.fx.control;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javax.annotation.Nonnull;

public class ButtonDialog extends Dialog
{
	private final Pane itemPane = VBoxBuilder.create().spacing( 6 ).build();
	private final HBox buttonRow = HBoxBuilder.create().padding( new Insets( 12, 0, 0, 0 ) ).spacing( 15 )
			.alignment( Pos.BOTTOM_RIGHT ).build();

	public ButtonDialog( @Nonnull final Node owner, @Nonnull String header )
	{
		super( owner );

		//TODO: Replace with CSS.
		Label headerLabel = LabelBuilder.create().font( Font.font( null, FontWeight.BOLD, 14 ) ).text( header ).build();

		super.getItems().setAll( headerLabel, itemPane, buttonRow );
	}

	@Override
	public ObservableList<Node> getItems()
	{
		return itemPane.getChildren();
	}

	public ObservableList<Node> getButtons()
	{
		return buttonRow.getChildren();
	}
}

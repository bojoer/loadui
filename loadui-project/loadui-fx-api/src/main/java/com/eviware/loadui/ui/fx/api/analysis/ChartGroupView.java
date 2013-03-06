package com.eviware.loadui.ui.fx.api.analysis;

import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.traits.Releasable;

public interface ChartGroupView
{

	public ToggleGroup getChartGroupToggleGroup();

	public HBox getButtonBar();

	public AnchorPane getComponentGroupAnchor();

	public VBox getMainChartGroup();

	public ChartGroup getChartGroup();

	public MenuButton getMenuButton();

	public Node getNode();

}
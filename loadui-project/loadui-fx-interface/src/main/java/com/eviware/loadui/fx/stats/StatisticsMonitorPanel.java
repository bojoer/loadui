package com.eviware.loadui.fx.stats;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.util.table.LTable;

public class StatisticsMonitorPanel extends JFrame
{
	private Container container;
	private StatsModel2 model;
	private ProjectItem project;

	public StatisticsMonitorPanel(ProjectItem project) {
		super("Statistics Monitor");
		this.project = project;
		this.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
		this.setPreferredSize( new Dimension( 400, 300 ) );
		container = this.getContentPane();
		model = new StatsModel2(project);
		init();
	}

	private void init()
	{
		
/*
 * Commented dtuff is for table		
 */
//		LTable table = new LTable( model );
//		table.setAutoCreateColumnsFromModel(true);
//		table.setVisibleRowCount(5);
//		table.setHorizontalScrollEnabled(true);
//		table.setSortable( true );
//		table.setEditable( false );
//		table.setAutoscroll( true );
//		JScrollPane scrollPanel = new JScrollPane(table);
//		scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//		scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//		scrollPanel.setWheelScrollingEnabled(true);
//		scrollPanel.setBorder( BorderFactory.createTitledBorder( "Stats Table" ) );
//		JPanel panel = new JPanel( new BorderLayout() );
//		panel.add( scrollPanel, BorderLayout.CENTER );
//		panel.setSize( new Dimension(600, 250));
//		panel.setMaximumSize( new Dimension(600, 250));
//		panel.setPreferredSize( new Dimension(600, 250));
		
/*
 * Graph showing stats
 */
		JPanel panel = new JPanel( new BorderLayout() );
		panel.setBorder( BorderFactory.createTitledBorder( "Stats Graph" ) );
		panel.add( model, BorderLayout.CENTER );
		panel.setSize( new Dimension(600, 250));
		panel.setMaximumSize( new Dimension(600, 250));
		panel.setPreferredSize( new Dimension(600, 250));
		container.add(panel);
		
	}
}

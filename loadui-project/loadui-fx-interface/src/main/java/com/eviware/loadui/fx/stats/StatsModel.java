package com.eviware.loadui.fx.stats;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.ui.table.LTableModel;
import com.eviware.loadui.util.ScheduledExecutor;

/*
 * This is model for showing collected data in table
 */
public class StatsModel extends LTableModel
{

	private ProjectItem project;
	private UpdateTask updateTask = new UpdateTask();

	private ScheduledFuture<?> future;
	
	public StatsModel( ProjectItem project )
	{
		super( 1000, true);
		this.project = project;
		
		future = ScheduledExecutor.instance.scheduleAtFixedRate( updateTask, 1000, 1000, TimeUnit.MILLISECONDS );
		
		init();
	}
	
	private void init()
	{
		addColumn( ProjectItem.ASSERTION_COUNTER );
		addColumn( ProjectItem.FAILURE_COUNTER );
	}

	private class UpdateTask implements Runnable
	{
		@Override
		public void run()
		{
			update();
		}

	}
	
	private void update()
	{
		ArrayList<String> row = new ArrayList<String>();
		row.add( ((Long)project.getCounter( ProjectItem.ASSERTION_COUNTER ).get()).toString() );
		row.add( ((Long)project.getCounter( ProjectItem.FAILURE_COUNTER ).get()).toString() );
 		addRow( row );
		
	}

}

package com.eviware.loadui.ui.fx.api.analysis;

import java.util.concurrent.Callable;

import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.util.Callback;

import com.eviware.loadui.api.statistics.store.Execution;

public interface ExecutionsInfo
{
	
	/**
	 * The ExecutionsInfo Data will be passed on to the client through the {@link ExecutionsInfo#runWhenReady(Callable)}
	 * callback.
	 * @author renato
	 *
	 */
	public interface Data {

		/**
		 * @return all recent executions (ie. executions which have not been archived and not including the current run)
		 */
		ObservableList<Execution> getRecentExecutions();
		
		/**
		 * @return executions archived by the user
		 */
		ObservableList<Execution> getArchivedExecutions();
		
		/**
		 * @return current execution property. The actual value is updated as required.
		 */
		Property<Execution> getCurrentExecution();
		
	}

	
	/**
	 * The given callback will be called when all properties have been set.
	 * If all properties are already set when registering this will run immediately. 
	 * @param callback
	 */
	void runWhenReady( Callback<Data, Void> callback );
	
	/**
	 * Adds the given node to the executions menu.
	 * @param node
	 */
	void addToMenu( Node node );

}

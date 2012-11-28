package com.eviware.loadui.ui.fx.util;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class Observables
{
	/**
	 * Creates a Group out of the provided observables.
	 * 
	 * @param observables
	 * @return
	 */

	public static Group group( Observable... observables )
	{
		return new Group( observables );
	}

	/**
	 * A Group is an Observable that invalidates whenever one of its group
	 * members (observables) invalidates.
	 * 
	 * Note that a Group will NOT invalidate when observables are added or
	 * removed. To monitor added and removed group members, use
	 * myGroup.getObservables().addListener( ... ).
	 * 
	 */

	public static class Group extends ObservableBase
	{
		private final ObservableList<Observable> observables = FXCollections.observableArrayList();
		private final InvalidationListener invalidationListener = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				fireInvalidation();
			}
		};

		private Group( Observable... observables )
		{
			this.observables.addListener( new ListChangeListener<Observable>()
			{
				@Override
				public void onChanged( ListChangeListener.Change<? extends Observable> change )
				{
					while( change.next() )
					{
						for( Observable observable : change.getAddedSubList() )
						{
							observable.addListener( invalidationListener );
						}
						for( Observable observable : change.getRemoved() )
						{
							observable.removeListener( invalidationListener );
						}
					}
					//					fireInvalidation();
				}
			} );

			for( Observable observable : observables )
			{
				this.observables.add( observable );
			}
		}

		public ObservableList<Observable> getObservables()
		{
			return observables;
		}

		@Override
		public void fireInvalidation()
		{
			super.fireInvalidation();
		}
	}
}

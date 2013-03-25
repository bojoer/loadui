package com.eviware.loadui.ui.fx.util;

import java.util.Arrays;

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

	public static <T extends Observable> Group<T> group( T[] observables )
	{
		return new Group<T>( Arrays.asList( observables ) );
	}

	public static <T extends Observable> Group<T> group( Iterable<T> observables )
	{
		return new Group<T>( observables );
	}

	public static <T extends Observable> Group<T> group()
	{
		return new Group<T>( Arrays.<T> asList() );
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

	public static class Group<T extends Observable> extends ObservableBase
	{
		private final ObservableList<T> observables = FXCollections.observableArrayList();
		private final InvalidationListener invalidationListener = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				fireInvalidation();
			}
		};

		private Group( Iterable<T> list )
		{
			this.observables.addListener( new ListChangeListener<T>()
			{
				@Override
				public void onChanged( ListChangeListener.Change<? extends T> change )
				{
					while( change.next() )
					{
						for( T observable : change.getAddedSubList() )
						{
							observable.addListener( invalidationListener );
						}
						for( T observable : change.getRemoved() )
						{
							observable.removeListener( invalidationListener );
						}
					}
				}

			} );

			for( T observable : list )
			{
				this.observables.add( observable );
			}
		}

		public ObservableList<T> getObservables()
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

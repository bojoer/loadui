package com.eviware.loadui.ui.fx.util;

import java.util.Arrays;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Observables
{
	protected static final Logger log = LoggerFactory.getLogger( Observables.class );

	/**
	 * Creates a Group out of the provided observables.
	 * 
	 * @param observables
	 * @return
	 */

	public static Group group( Observable... observables )
	{
		return new Group( Arrays.asList( observables ), 0 );
	}

	public static Group group( long propagationEliminationPeriod, Observable... observables )
	{
		return new Group( Arrays.asList( observables ), propagationEliminationPeriod );
	}

	public static Group group( Iterable<Observable> observables )
	{
		return new Group( observables, 0 );
	}

	public static Group group( long propagationEliminationPeriod, Iterable<Observable> observables )
	{
		return new Group( observables, propagationEliminationPeriod );
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
			public void invalidated( Observable o )
			{
				if( lastInvalidated == o
						|| timeOfLatestInvalidation + propagationEliminationPeriod <= System.currentTimeMillis() )
				{
					lastInvalidated = o;
					timeOfLatestInvalidation = System.currentTimeMillis();
					fireInvalidation();
				}
			}
		};

		private Observable lastInvalidated;
		private volatile long timeOfLatestInvalidation = System.currentTimeMillis();
		private final long propagationEliminationPeriod;

		private Group( Iterable<Observable> observables, long propagationEliminationPeriod )
		{
			this.propagationEliminationPeriod = propagationEliminationPeriod;
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

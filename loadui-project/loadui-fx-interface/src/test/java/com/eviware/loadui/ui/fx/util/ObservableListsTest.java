package com.eviware.loadui.ui.fx.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;

public class ObservableListsTest
{
	@Test
	public void bindContentUnorderedCanBeRearangedAndStillStayInSync()
	{
		ObservableList<String> bindee = FXCollections.observableArrayList();
		bindee.addAll( "One", "Two", "Three", "Four", "Five" );
		ObservableList<String> binder = FXCollections.observableArrayList();
		ObservableLists.bindContentUnordered( binder, bindee );

		assertTrue( binder.containsAll( bindee ) );
		assertTrue( binder.equals( bindee ) );

		bindee.add( "Six" );
		bindee.remove( "Seven" );

		assertTrue( binder.containsAll( bindee ) );

		//Shuffle binder
		binder.add( binder.remove( 2 ) );
		binder.add( binder.remove( 4 ) );
		binder.add( binder.remove( 1 ) );

		bindee.add( "Eight" );
		bindee.remove( "Three" );

		assertTrue( binder.containsAll( bindee ) );
		assertFalse( binder.equals( bindee ) );
	}

	@Test
	public void bindContentUnorderedShouldKeepAStrongReferenceToTheBindee() throws InterruptedException
	{
		ObservableList<String> bindee = FXCollections.observableArrayList();
		WeakReference<ObservableList<String>> weakBindee = new WeakReference<>( bindee );

		ObservableList<String> binder = FXCollections.observableArrayList();
		ObservableLists.bindContentUnordered( binder, bindee );

		bindee = null;

		for( int i = 0; i < 5; i++ )
		{
			System.gc();
			Thread.sleep( 20 );
		}

		//There is no guarantee that the bindee is GCd even if there is no strong reference to it, but this test is better than nothing.
		assertThat( weakBindee.get(), notNullValue() );
	}

	@Test
	public void bindSortedReturnsAListThatIsAlwaysSorted()
	{
		ObservableList<Integer> unsorted = FXCollections.observableArrayList();
		ObservableList<Integer> sorted = FXCollections.observableArrayList();
		ObservableLists.bindSorted( sorted, unsorted, Ordering.natural() );

		unsorted.addAll( 3, 1, 4, 1, 5, 6, 9, 2 );
		assertThat( sorted, equalTo( Arrays.asList( 1, 1, 2, 3, 4, 5, 6, 9 ) ) );

		unsorted.removeAll( 2, 4 );
		unsorted.addAll( 7, 8 );
		assertThat( sorted, equalTo( Arrays.asList( 1, 1, 3, 5, 6, 7, 8, 9 ) ) );
	}

	@Test
	public void concatUnorderedShouldReturnAListThatIsKeptInSyncWithItsSubLists()
	{
		ObservableList<String> a = FXCollections.observableArrayList();
		ObservableList<String> b = FXCollections.observableArrayList();

		ObservableList<String> ab = ObservableLists.concatUnordered( a, b );

		a.add( "Foo" );
		System.gc();
		b.add( "Bar" );

		assertTrue( ab.size() == a.size() + b.size() );
		assertTrue( ab.containsAll( a ) );
		assertTrue( ab.containsAll( b ) );

		a.remove( "Foo" );
		System.gc();
		b.add( "Foo" );

		assertTrue( ab.size() == a.size() + b.size() );
		assertTrue( ab.containsAll( a ) );
		assertTrue( ab.containsAll( b ) );
	}

	@Test
	public void filterContainsOnlyValuesThatFulfilThePredicate()
	{
		ObservableList<Integer> allElements = FXCollections.observableArrayList();
		ObservableList<Integer> filteredElements = ObservableLists.filter( allElements, new Predicate<Integer>()
		{
			@Override
			public boolean apply( Integer input )
			{
				return input.intValue() % 2 == 0;
			}
		} );

		allElements.addAll( 1, 2, 3, 4 );
		allElements.addAll( 5, 6, 7 );
		assertThat( filteredElements, equalTo( Arrays.asList( 2, 4, 6 ) ) );
	}

	@Test
	public void transformContainsTransformedValues()
	{
		ObservableList<Integer> allElements = FXCollections.observableArrayList();
		ObservableList<Integer> transformedElements = ObservableLists.transform( allElements,
				new Function<Integer, Integer>()
				{
					@Override
					public Integer apply( Integer input )
					{
						return input.intValue() * 2;
					}
				} );

		allElements.addAll( 1, 2, 3, 4 );
		allElements.addAll( 5, 6, 7 );
		assertThat( transformedElements, equalTo( Arrays.asList( 2, 4, 6, 8, 10, 12, 14 ) ) );
	}
}

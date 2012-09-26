package com.eviware.loadui.ui.fx.api;

import static org.junit.Assert.assertTrue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Test;

import com.eviware.loadui.ui.fx.util.ObservableLists;

public class ObservableListsTest
{
	@Test
	public void testMe()
	{
		ObservableList<String> a = FXCollections.observableArrayList();
		ObservableList<String> b = FXCollections.observableArrayList();

		ObservableList<String> ab = ObservableLists.concat( a, b );

		a.add( "Foo" );

		assertTrue( ab.size() > 0 );
	}
}

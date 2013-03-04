package com.eviware.loadui.ui.fx.control;

import static com.eviware.loadui.ui.fx.util.test.TestFX.find;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPaneBuilder;
import javafx.scene.layout.PaneBuilder;
import javafx.stage.Stage;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.eviware.loadui.test.categories.GUITest;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.SettableFuture;

@Category( GUITest.class )
public class DetachableTabTest
{
	private final static SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static Stage stage;

	public static class DetachableTabTestApp extends Application
	{
		@Override
		public void start( Stage primaryStage ) throws Exception
		{
			Tab normalTab = new Tab( "Normal tab" );
			normalTab.setId( "normaltab" );

			DetachableTab detachableTab = new DetachableTab( "Detachable tab", DetachedTabsHolder.get() );
			detachableTab.setId( "detachabletab" );
			detachableTab.setDetachableContent( PaneBuilder.create().id( "detachablecontent" ).build() );

			primaryStage.setScene( SceneBuilder.create().width( 300 ).height( 200 )
					.root( TabPaneBuilder.create().id( "tabpane" ).tabs( normalTab, detachableTab ).build() ).build() );
			primaryStage.show();

			stageFuture.set( primaryStage );
		}
	}

	@BeforeClass
	public static void createWindow() throws Throwable
	{
		FXTestUtils.launchApp( DetachableTabTestApp.class );
		stage = stageFuture.get( 5, TimeUnit.SECONDS );
		TestFX.targetWindow( stage );
		FXTestUtils.bringToFront( stage );
	}

	@Test
	public void shouldDetachAndReattachWhenClosingDetachedStage() throws Throwable
	{
		final TabPane tabpane = find( "#tabpane" );
		assertNotNull( tabpane );
		final Button detachButton = find( ".button", tabpane );
		assertNotNull( detachButton );

		final DetachableTab tab = Iterables.getOnlyElement( Iterables.filter( tabpane.getTabs(), DetachableTab.class ) );
		assertNotNull( tab );

		assertThat( ( Stage )tab.getDetachableContent().getScene().getWindow(), is( stage ) );
		assertThat( tab.getContent(), is( ( Node )tab.getDetachableContent() ) );

		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				tab.setDetached( true );
			}
		}, 2 );

		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				assertThat( tab.isDetached(), is( true ) );
				Stage detachedStage = ( Stage )tab.getDetachableContent().getScene().getWindow();
				assertThat( detachedStage, not( is( stage ) ) );
				assertThat( detachedStage.getTitle(), is( tab.getText() ) );
				assertThat( tab.getContent(), not( is( ( Node )tab.getDetachableContent() ) ) );

				// tab should be in detached set
				assertFalse( DetachedTabsHolder.get().isEmpty() );

				detachedStage.close();
			}
		}, 2 );

		assertThat( ( Stage )tab.getDetachableContent().getScene().getWindow(), is( stage ) );
		assertThat( tab.isDetached(), is( false ) );
		assertThat( tab.getContent(), is( ( Node )tab.getDetachableContent() ) );
		assertTrue( DetachedTabsHolder.get().isEmpty() );
	}

	@Test
	public void shouldDetachAndReattachWhenButtonPressed() throws Throwable
	{
		final TabPane tabpane = find( "#tabpane" );
		tabpane.getSelectionModel().select( 1 );
		Button detachButton = find( ".button", tabpane );

		final DetachableTab tab = Iterables.getOnlyElement( Iterables.filter( tabpane.getTabs(), DetachableTab.class ) );
		assertNotNull( tab );

		assertThat( ( Stage )tab.getDetachableContent().getScene().getWindow(), is( stage ) );
		assertThat( tab.getContent(), is( ( Node )tab.getDetachableContent() ) );

		TestFX controller = TestFX.wrap( new FXScreenController() ).click( detachButton );

		FXTestUtils.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				assertThat( tab.isDetached(), is( true ) );
				Stage detachedStage = ( Stage )tab.getDetachableContent().getScene().getWindow();
				assertThat( detachedStage, not( is( stage ) ) );
				assertThat( detachedStage.getTitle(), is( tab.getText() ) );
				assertThat( tab.getContent(), not( is( ( Node )tab.getDetachableContent() ) ) );

				// tab contents should be in detached set
				assertFalse( DetachedTabsHolder.get().isEmpty() );
			}
		}, 2 );

		controller.click( detachButton );

		assertThat( ( Stage )tab.getDetachableContent().getScene().getWindow(), is( stage ) );
		assertThat( tab.isDetached(), is( false ) );
		assertThat( tab.getContent(), is( ( Node )tab.getDetachableContent() ) );
		assertTrue( DetachedTabsHolder.get().isEmpty() );
	}
}
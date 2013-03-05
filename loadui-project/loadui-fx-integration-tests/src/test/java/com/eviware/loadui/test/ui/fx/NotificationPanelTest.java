package com.eviware.loadui.test.ui.fx;

import static com.eviware.loadui.ui.fx.util.test.FXTestUtils.getOrFail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.loadui.api.testevents.MessageLevel;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.test.ui.fx.states.FXAppLoadedState;
import com.eviware.loadui.test.ui.fx.states.ProjectLoadedWithoutAgentsState;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.TestUtils;

public class NotificationPanelTest
{

	private static TestFX controller;

	@BeforeClass
	public static void enterState() throws Exception
	{
		controller = GUI.getController();
	}
	
	@Test
	public void notificationShowsUpInWorkspaceView() throws Exception
	{
		FXAppLoadedState.STATE.enter();
		Node panelNode = getOrFail( ".notification-panel" );

		assertFalse( panelNode.isVisible() );

		BeanInjector.getBeanFuture( TestEventManager.class ).get( 500, TimeUnit.MILLISECONDS )
				.logMessage( MessageLevel.WARNING, "A message" );

		controller.sleep( 1000 );

		assertTrue( panelNode.isVisible() );
		Set<Node> textNodes = TestFX.findAll( "#notification-text", panelNode );

		assertFalse( textNodes.isEmpty() );
		assertTrue( textNodes.iterator().next() instanceof Label );
		assertEquals( "A message", ( ( Label )textNodes.iterator().next() ).getText() );
		controller.click( "#hide-notification-panel" ).sleep( 1000 );

		assertFalse( panelNode.isVisible() );

	}

	@Test
	public void notificationShowsUpInProjectView() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();

		Node panelNode = getOrFail( ".notification-panel" );

		assertFalse( panelNode.isVisible() );

		BeanInjector.getBeanFuture( TestEventManager.class ).get( 500, TimeUnit.MILLISECONDS )
				.logMessage( MessageLevel.WARNING, "A message" );

		controller.sleep( 1000 );

		assertTrue( panelNode.isVisible() );
		Set<Node> textNodes = TestFX.findAll( "#notification-text", panelNode );

		assertFalse( textNodes.isEmpty() );
		assertTrue( textNodes.iterator().next() instanceof Label );
		assertEquals( "A message", ( ( Label )textNodes.iterator().next() ).getText() );

		controller.click( "#hide-notification-panel" ).sleep( 1000 );

		assertFalse( panelNode.isVisible() );

	}

	@Test
	public void notificationShowsUpInDetachedTab() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();

		Node panelNode = getOrFail( ".notification-panel" );

		controller.click( "#statsTab" ).click( "#statsTab .styleable-graphic" );

		class DetachedAnalysisViewHolder
		{
			Node content;
		}
		final DetachedAnalysisViewHolder detachedHolder = new DetachedAnalysisViewHolder();
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				Set<Node> nodes = TestFX.findAll( ".detached-content" );
				boolean ok = nodes.size() == 1;
				if( ok )
					detachedHolder.content = nodes.iterator().next();
				return ok;
			}
		}, 2 );

		assertNotNull( detachedHolder.content );

		Node clonedPanelNode = detachedHolder.content.lookup( ".notification-panel" );

		assertNotNull( clonedPanelNode );
		assertFalse( panelNode.isVisible() );
		assertFalse( clonedPanelNode.isVisible() );
		
		controller.move( 200, 200 );

		BeanInjector.getBeanFuture( TestEventManager.class ).get( 500, TimeUnit.MILLISECONDS )
				.logMessage( MessageLevel.WARNING, "A message" );

		controller.sleep( 1000 );

		assertTrue( panelNode.isVisible() );
		Set<Node> textNodes = TestFX.findAll( "#notification-text", panelNode );

		assertFalse( textNodes.isEmpty() );
		assertTrue( textNodes.iterator().next() instanceof Label );
		assertEquals( "A message", ( ( Label )textNodes.iterator().next() ).getText() );

		assertTrue( clonedPanelNode.isVisible() );
		textNodes = TestFX.findAll( "#notification-text", clonedPanelNode );

		assertFalse( textNodes.isEmpty() );
		assertTrue( textNodes.iterator().next() instanceof Label );
		assertEquals( "A message", ( ( Label )textNodes.iterator().next() ).getText() );

		controller.click( "#hide-notification-panel" ).sleep( 1000 );

		assertFalse( panelNode.isVisible() );
		assertFalse( clonedPanelNode.isVisible() );
		controller.closeCurrentWindow();

	}

	@Test
	public void inspectorViewIsShownWhenClickingOnButton() throws Exception
	{
		ProjectLoadedWithoutAgentsState.STATE.enter();

		Node panelNode = getOrFail( ".notification-panel" );

		Node inspectorView = getOrFail( ".inspector-view" );

		// inspector view is closed
		assertTrue( ( ( Region )inspectorView ).getHeight() < 50 );

		BeanInjector.getBeanFuture( TestEventManager.class ).get( 500, TimeUnit.MILLISECONDS )
				.logMessage( MessageLevel.WARNING, "A message" );

		controller.sleep( 1000 );

		getOrFail( "#show-system-log" );
		controller.click( "#show-system-log" ).sleep( 500 );

		// inspector view is opened!
		assertTrue( ( ( Region )inspectorView ).getHeight() > 150 );

		controller.click( "#hide-notification-panel" ).sleep( 1000 );

		assertFalse( panelNode.isVisible() );
	}
	
	@Test
	public void notificationPanelWontGoAwayIfMouseIsOnIt() throws Exception {
		FXAppLoadedState.STATE.enter();
		
		Node panelNode = getOrFail( ".notification-panel" );
		
		BeanInjector.getBeanFuture( TestEventManager.class ).get( 500, TimeUnit.MILLISECONDS )
				.logMessage( MessageLevel.WARNING, "A message" );

		// find position of notification panel, close it, then put mouse just below it
		controller.sleep( 1000 ).move( "#hide-notification-panel" ).click().moveBy( 0, 150 ).sleep( 500 );
		
		BeanInjector.getBeanFuture( TestEventManager.class ).get( 500, TimeUnit.MILLISECONDS )
		.logMessage( MessageLevel.WARNING, "A message" );
		
		// put mouse on notification panel and stay there for a while
		controller.sleep( 2000 ).move( "#hide-notification-panel" ).sleep( 5000 );
		assertTrue( panelNode.isVisible() );
		
		// if moving out and going back quickly, panel should still be visible
		controller.moveBy( 0, 150 ).moveBy( 0, -150 ).sleep( 1000 );
		assertTrue( panelNode.isVisible() );
		
		// now go away and let the panel vanish
		controller.moveBy( 0, 150 ).sleep( 1000 );
		assertFalse( panelNode.isVisible() );
	}
	
}

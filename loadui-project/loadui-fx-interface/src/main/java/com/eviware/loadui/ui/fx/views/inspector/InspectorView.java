package com.eviware.loadui.ui.fx.views.inspector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.TimelineBuilder;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabBuilder;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import com.eviware.loadui.ui.fx.api.Inspector;
import com.eviware.loadui.ui.fx.api.perspective.PerspectiveEvent;
import com.eviware.loadui.ui.fx.util.ErrorHandler;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.eviware.loadui.util.collections.SafeExplicitOrdering;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Ordering;

public class InspectorView extends AnchorPane
{
	public static final String MONITORS_TAB = "Monitors";
	public static final String ASSERTIONS_TAB = "Assertions";
	public static final String DISTRIBUTION_TAB = "Distribution";
	public static final String SYSTEM_LOG_TAB = "System Log";
	public static final String EVENT_LOG_TAB = "Event Log";

	private static final Ordering<String> INSPECTOR_ORDERING = SafeExplicitOrdering.of( SYSTEM_LOG_TAB,
			DISTRIBUTION_TAB, ASSERTIONS_TAB, EVENT_LOG_TAB, MONITORS_TAB ).compound( Ordering.natural() );

	private final BooleanProperty minimizedProperty = new SimpleBooleanProperty( this, "minimized", true );

	private final ObservableList<Inspector> inspectors = FXCollections.observableArrayList();

	private final Property<EventType<? extends PerspectiveEvent>> perspective = new SimpleObjectProperty<EventType<? extends PerspectiveEvent>>(
			this, "perspective", PerspectiveEvent.ANY );

	private StackPane tabHeaderArea;

	@FXML
	private TabPane tabPane;

	@FXML
	private HBox buttonBar;

	@FXML
	private Button helpButton;

	private ObservableList<Tab> inspectorTabs;

	private final DragBehavior dragBehavior = new DragBehavior();

	public InspectorView()
	{
		FXMLUtils.load( this );

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				init();
			}
		} );
	}

	public ObservableList<Inspector> getInspectors()
	{
		return inspectors;
	}

	public Property<EventType<? extends PerspectiveEvent>> perspectiveProperty()
	{
		return perspective;
	}

	public void setPerspective( EventType<? extends PerspectiveEvent> type )
	{
		perspective.setValue( type );
	}

	public EventType<? extends PerspectiveEvent> getPerspective()
	{
		return perspective.getValue();
	}

	public void ensureShowing( String optionalTabName )
	{
		tabPane.getSelectionModel().select( getTabByName( optionalTabName ) );
		dragBehavior.animateToLastHeight();
	}

	private Tab getTabByName( String name )
	{
		if( name == null )
			name = "";
		for( Tab tab : inspectorTabs )
		{
			if( name.equals( tab.getId() ) )
			{
				return tab;
			}
		}
		return new Tab();
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings( value = "NP_NULL_ON_SOME_PATH", justification = "Execution of code will stop after ErrorHandler.promptRestart()." )
	private void init()
	{
		tabHeaderArea = ( StackPane )tabPane.lookup( ".tab-header-area" );

		//TODO: tabHeaderArea can be null here, causing an unrecoverable NPE at startup
		if( tabHeaderArea == null )
			ErrorHandler.promptRestart();
		tabHeaderArea.addEventHandler( MouseEvent.ANY, dragBehavior );

		buttonBar.setPrefHeight( tabHeaderArea.prefHeight( -1 ) );

		helpButton.setOnAction( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				String helpUrl = ( ( Inspector )tabPane.getSelectionModel().getSelectedItem().getUserData() ).getHelpUrl();
				if( helpUrl != null )
				{
					UIUtils.openInExternalBrowser( helpUrl );
				}
			}
		} );

		inspectorTabs = ObservableLists.transform( inspectors, new Function<Inspector, Tab>()
		{
			@Override
			public Tab apply( Inspector inspector )
			{
				inspector.initialize( sceneProperty() );
				return TabBuilder.create().id( inspector.getName() ).userData( inspector ).text( inspector.getName() )
						.content( inspector.getPanel() ).build();
			}
		} );

		InvalidationListener invalidationListener = new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				refreshTabs();
			}
		};
		inspectorTabs.addListener( invalidationListener );
		perspective.addListener( invalidationListener );

		refreshTabs();

		tabPane.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<Tab>()
		{
			@Override
			public void changed( ObservableValue<? extends Tab> arg0, Tab oldTab, Tab newTab )
			{
				if( oldTab != null )
				{
					( ( Inspector )oldTab.getUserData() ).onHide();
				}
				else if( newTab != null )
				{
					if( !tabPane.getTabs().contains( newTab ) )
					{
						tabPane.getSelectionModel().selectFirst();
					}
					( ( Inspector )newTab.getUserData() ).onShow();
				}

				if( oldTab == null || newTab == null )
				{
					return;
				}

				if( minimizedProperty.get() )
				{
					dragBehavior.toggleMinimized();
				}
				else
				{
					double oldHeight = getMaxHeight();
					double newHeight = boundHeight( oldHeight );
					if( newHeight < oldHeight - 5.0 )
					{
						TimelineBuilder
								.create()
								.keyFrames(
										new KeyFrame( Duration.seconds( 0.1 ), new KeyValue( maxHeightProperty(), newHeight,
												Interpolator.EASE_BOTH ) ) ).build().playFromStart();
					}
					else
					{
						setMaxHeight( newHeight );
					}
				}
			}
		} );

		setMaxHeight( boundHeight( 30 ) );
	}

	private void refreshTabs()
	{
		List<Tab> shownTabs = new ArrayList<>();
		for( Tab tab : inspectorTabs )
		{
			Inspector inspector = ( Inspector )tab.getUserData();
			String regex = Objects.firstNonNull( inspector.getPerspectiveRegex(), ".*" );
			if( Pattern.matches( regex, PerspectiveEvent.getPath( perspective.getValue() ) ) )
			{
				shownTabs.add( tab );
			}
		}
		final SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		Tab selected = selectionModel.getSelectedItem();
		selectionModel.clearSelection();

		Collections.sort( shownTabs, new Comparator<Tab>()
		{
			@Override
			public int compare( Tab o1, Tab o2 )
			{
				return INSPECTOR_ORDERING.compare( o1.getText(), o2.getText() );
			}
		} );
		System.out.println( "Ordering: " + shownTabs );
		tabPane.getTabs().setAll( shownTabs );

		if( !tabPane.getTabs().contains( selected ) )
		{
			if( tabPane.getTabs().isEmpty() )
			{
				if( !minimizedProperty.get() )
				{
					dragBehavior.toggleMinimized();
				}
			}
			else
			{
				selectionModel.selectFirst();
			}
		}
		else
		{
			selectionModel.select( selected );
		}
	}

	private double boundHeight( double desiredHeight )
	{
		double headerHeight = tabHeaderArea.prefHeight( -1 );
		Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
		if( selectedTab != null )
		{
			Node selectedNode = selectedTab.getContent();
			if( selectedNode != null )
			{
				desiredHeight = Math.min( desiredHeight, headerHeight + selectedNode.maxHeight( -1 ) );
			}
		}

		return Math.max( headerHeight, desiredHeight );
	}

	private final class DragBehavior implements EventHandler<MouseEvent>
	{
		private boolean dragging = false;
		private double startY = 0;
		private double lastHeight = 200;

		private final EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				minimizedProperty.set( !minimizedProperty.get() );
			}
		};

		@Override
		public void handle( MouseEvent event )
		{
			if( event.getEventType() == MouseEvent.MOUSE_PRESSED )
			{
				startY = event.getScreenY() + getHeight();
			}
			else if( event.getEventType() == MouseEvent.DRAG_DETECTED )
			{
				dragging = true;
				minimizedProperty.set( false );
			}
			else if( event.getEventType() == MouseEvent.MOUSE_DRAGGED )
			{
				if( dragging )
				{
					setMaxHeight( boundHeight( startY - event.getScreenY() ) );
				}
			}
			else if( event.getEventType() == MouseEvent.MOUSE_RELEASED )
			{
				dragging = false;
				if( getMaxHeight() <= boundHeight( 0 ) )
				{
					minimizedProperty.set( true );
				}
				else
				{
					lastHeight = getMaxHeight();
				}
			}
			else if( event.getEventType() == MouseEvent.MOUSE_CLICKED )
			{
				if( event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 )
				{
					toggleMinimized();
				}
			}
		}

		public void toggleMinimized()
		{
			double target = boundHeight( 0 );
			if( minimizedProperty.get() )
			{
				if( tabPane.getTabs().isEmpty() )
				{
					return;
				}
				target = boundHeight( lastHeight );
				if( target <= boundHeight( 0 ) )
				{
					target = boundHeight( target
							+ tabPane.getSelectionModel().getSelectedItem().getContent().prefHeight( -1 ) );
				}
			}

			animateToHeight( target );
		}

		public void animateToLastHeight()
		{
			minimizedProperty.set( true );
			animateToHeight( lastHeight );
		}

		private void animateToHeight( double target )
		{
			TimelineBuilder
					.create()
					.keyFrames(
							new KeyFrame( Duration.seconds( 0.2 ), new KeyValue( maxHeightProperty(), target,
									Interpolator.EASE_BOTH ) ) ).onFinished( eventHandler ).build().playFromStart();
		}

	}
}
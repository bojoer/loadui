package com.eviware.loadui.ui.fx.views.inspector;

import java.util.HashSet;
import java.util.Set;
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
import javafx.scene.control.Label;
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
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.util.UIUtils;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Ordering;

public class InspectorView extends AnchorPane
{
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

	private void init()
	{
		tabHeaderArea = ( StackPane )tabPane.lookup( ".tab-header-area" );
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
				Object panel = inspector.getPanel();
				if( !( panel instanceof Node ) )
				{
					panel = new Label( "Unsupported inspector panel." );
				}
				return TabBuilder.create().userData( inspector ).text( inspector.getName() ).content( ( Node )panel )
						.build();
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
				if( newTab != null )
				{
					( ( Inspector )newTab.getUserData() ).onShow();
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

		setMaxHeight( boundHeight( 0 ) );
	}

	private void refreshTabs()
	{
		Set<Tab> added = new HashSet<>();
		Set<Tab> removed = new HashSet<>();
		for( Tab tab : inspectorTabs )
		{
			Inspector inspector = ( Inspector )tab.getUserData();
			String regex = Objects.firstNonNull( inspector.getPerspectiveRegex(), ".*" );
			if( Pattern.matches( regex, PerspectiveEvent.getPath( perspective.getValue() ) ) )
			{
				if( !tabPane.getTabs().contains( tab ) )
				{
					added.add( tab );
				}
			}
			else if( tabPane.getTabs().contains( tab ) )
			{
				removed.add( tab );
			}
		}
		tabPane.getTabs().addAll( added );
		tabPane.getTabs().removeAll( removed );
		FXCollections.sort( tabPane.getTabs(), Ordering.usingToString() );
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
		private double lastHeight = 0;

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
				target = boundHeight( lastHeight );
				if( target <= boundHeight( 0 ) )
				{
					target = boundHeight( target
							+ tabPane.getSelectionModel().getSelectedItem().getContent().prefHeight( -1 ) );
				}
			}
			else
			{
				lastHeight = getHeight();
			}

			TimelineBuilder
					.create()
					.keyFrames(
							new KeyFrame( Duration.seconds( 0.2 ), new KeyValue( maxHeightProperty(), target,
									Interpolator.EASE_BOTH ) ) ).onFinished( eventHandler ).build().playFromStart();
		}
	}
}
package com.eviware.loadui.components.soapui.layout;

import java.io.File;
import java.lang.reflect.Array;
import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuButtonBuilder;
import javafx.scene.control.PopupControl;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.GridPaneBuilder;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent.SoapUITestCaseRunner;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.ui.fx.control.FilePicker;
import com.eviware.loadui.ui.fx.util.Properties;
import com.eviware.loadui.ui.fx.util.SelectionModelUtils;
import com.eviware.loadui.util.BeanInjector;
import com.google.common.collect.ImmutableMap;
import com.sun.javafx.Utils;

public class SoapUiProjectSelector
{
	public static final String TEST_CASE = "testCase";

	private static final Logger log = LoggerFactory.getLogger( SoapUiProjectSelector.class );

	private final Property<File> projectFile;
	private final Property<String> testSuite;
	private final Property<String> testCase;

	private CountDownLatch testCaseLatch = new CountDownLatch( 0 );

	private final ComboBox<String> testSuiteCombo = ComboBoxBuilder.<String> create().maxHeight( Double.MAX_VALUE )
			.maxWidth( Double.MAX_VALUE ).build();
	private final ComboBox<String> testCaseCombo = ComboBoxBuilder.<String> create().maxHeight( Double.MAX_VALUE )
			.maxWidth( Double.MAX_VALUE ).build();

	public static SoapUiProjectSelector newInstance( SoapUISamplerComponent component, ComponentContext context,
			SoapUITestCaseRunner testCaseRunner )
	{
		SoapUiProjectSelector selector = new SoapUiProjectSelector( context );
		context.addEventListener( PropertyEvent.class, selector.new PropertyChangedListener( component, testCaseRunner ) );
		return selector;
	}

	private SoapUiProjectSelector( ComponentContext context )
	{
		projectFile = context.createProperty( "projectFile", File.class, null, false );
		testSuite = context.createProperty( "testSuite", String.class );
		testCase = context.createProperty( TEST_CASE, String.class );
	}

	public LayoutComponent buildLayout()
	{
		return new LayoutComponentImpl( ImmutableMap.<String, Object> builder().put( "component", buildNode() )
				.put( LayoutComponentImpl.CONSTRAINTS, "center, w 270!" ) //
				.build() );
	}

	public Node buildNode()
	{
		SelectionModelUtils.writableSelectedItemProperty( testSuiteCombo.getSelectionModel(), true ).bindBidirectional(
				Properties.convert( testSuite ) );
		SelectionModelUtils.writableSelectedItemProperty( testCaseCombo.getSelectionModel(), true ).bindBidirectional(
				Properties.convert( testCase ) );

		GridPane grid = GridPaneBuilder.create().rowConstraints( new RowConstraints( 18 ) )
				.columnConstraints( new ColumnConstraints( 70, 70, 70 ) ).hgap( 28 ).build();

		final MenuButton menuButton = MenuButtonBuilder.create().text( "Project" ).build();
		menuButton.setOnMouseClicked( new javafx.event.EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent arg0 )
			{
				new ProjectSelector( menuButton ).display();
			}
		} );
		BeanInjector.getBean( Stage.class ).getScene().getStylesheets()
				.add( SoapUiProjectSelector.class.getResource( "loadui-soapui-plugin-style.css" ).toExternalForm() );

		grid.add( menuButton, 0, 0 );
		grid.add( new Label( "TestSuite" ), 1, 0 );
		grid.add( new Label( "TestCase" ), 2, 0 );

		final Label projectLabel = new Label();
		updateProjectLabel( projectLabel );
		projectFile.getOwner().addEventListener( PropertyEvent.class, new EventHandler<PropertyEvent>()
		{
			@Override
			public void handleEvent( final PropertyEvent event )
			{
				if( event.getProperty() == projectFile )
					updateProjectLabel( projectLabel );
			}
		} );
		final Label testSuiteLabel = LabelBuilder.create().build();

		testSuiteLabel.textProperty().bind( Properties.convert( testSuite ) );
		final Label testCaseLabel = new Label();
		testCaseLabel.textProperty().bind( Properties.convert( testCase ) );

		VBox projectVBox = VBoxBuilder.create().minWidth( 140 ).minHeight( 18 ).children( menuButton, projectLabel )
				.build();
		VBox testSuiteVBox = VBoxBuilder.create().minWidth( 140 ).minHeight( 18 )
				.children( new Label( "TestSuite" ), testSuiteLabel ).build();
		VBox testCaseVBox = VBoxBuilder.create().minWidth( 140 ).minHeight( 18 )
				.children( new Label( "TestCase" ), testCaseLabel ).build();

		return HBoxBuilder.create().spacing( 28 ).minWidth( 320 ).children( projectVBox, testSuiteVBox, testCaseVBox )
				.build();
	}

	protected void updateProjectLabel( final Label projectLabel )
	{
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				projectLabel.setText( projectFile.getValue() == null ? "" : projectFile.getValue().getName()
						.replaceFirst( ".xml$", "" ) );
			}
		} );
	}

	public File getProjectFile()
	{
		return projectFile.getValue();
	}

	public String getProjectFileName()
	{
		return projectFile.getStringValue();
	}

	public void setProjectFile( File project )
	{
		projectFile.setValue( project );
	}

	public String getTestSuite()
	{
		return testSuite.getValue();
	}

	public void setTestSuite( final String name )
	{
		testSuite.setValue( name );
	}

	public String getTestCase()
	{
		try
		{
			testCaseLatch.await();
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
		return testCase.getValue();
	}

	public void setTestCase( final String name )
	{
		testCase.setValue( name );
	}

	public void reset()
	{
		projectFile.setValue( null );
		testCase.setValue( null );
		testSuite.setValue( null );
	}

	public void setTestSuites( final String... testSuites )
	{
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				testSuiteCombo.setItems( FXCollections.observableArrayList( testSuites ) );
			}
		} );
	}

	public void setTestCases( final String... testCases )
	{
		log.debug( "setTestCases:\n" );
		for( String tc : testCases )
			log.debug( tc );

		testCaseLatch = new CountDownLatch( 1 );

		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				if( testCases.length > 0 )
				{
					testCaseCombo.setItems( FXCollections.observableArrayList( testCases ) );
					testCase.setValue( testCases[0] );
				}
				testCaseLatch.countDown();
			}
		} );
	}

	private class ProjectSelector extends PopupControl
	{
		private final Parent parent;

		private ProjectSelector( Parent parent )
		{
			this.parent = parent;

			setAutoHide( true );

			Stage stage = BeanInjector.getBean( Stage.class );
			FilePicker picker = new FilePicker( stage, "Select SoapUI project", new ExtensionFilter(
					"SoapUI Project Files", "*.xml" ) );
			picker.selectedProperty().bindBidirectional( Properties.convert( projectFile ) );

			VBox vBox = VBoxBuilder
					.create()
					.styleClass( "project-selector" )
					.fillWidth( true )
					.prefHeight( 325 )
					.prefHeight( 160 )
					.style( "-fx-background-color: #f4f4f4;" )
					.children( new Label( "SoapUI Project" ), picker, new Label( "TestSuite" ), testSuiteCombo,
							new Label( "TestCase" ), testCaseCombo ).build();

			bridge.getChildren().setAll( StackPaneBuilder.create().children( vBox ).build() );
		}

		public void display()
		{
			Point2D point = Utils.pointRelativeTo( parent, 0, 0, HPos.LEFT, VPos.TOP, false );
			show( parent, point.getX(), point.getY() );
		}
	}

	private final class PropertyChangedListener implements EventHandler<PropertyEvent>
	{
		private final SoapUISamplerComponent component;
		private final SoapUITestCaseRunner testCaseRunner;

		public PropertyChangedListener( SoapUISamplerComponent component, SoapUITestCaseRunner testCaseRunner )
		{
			this.component = component;
			this.testCaseRunner = testCaseRunner;
		}

		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( event.getEvent() == PropertyEvent.Event.VALUE )
			{
				Property<?> property = event.getProperty();
				if( property == projectFile )
				{
					component.onProjectUpdated( projectFile.getValue() );
				}
				else if( property == testSuite )
				{
					log.debug( "Reload TestSuite because testSuite changed to " + testSuite.getValue() );
					testCaseRunner.setTestSuite( testSuite.getValue() );
				}
				else if( property == testCase )
				{
					log.debug( "Reload TestCase because testCase changed to " + testCase.getValue() );
					testCaseRunner.setNewTestCase( testCase.getValue() );
				}
			}
		}
	}
}

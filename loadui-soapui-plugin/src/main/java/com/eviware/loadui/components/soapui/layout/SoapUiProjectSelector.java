package com.eviware.loadui.components.soapui.layout;

import java.io.File;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceBoxBuilder;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBuilder;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.CustomMenuItemBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuButtonBuilder;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.GridPaneBuilder;
import javafx.scene.layout.RowConstraints;
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

public class SoapUiProjectSelector
{
	public static final String TEST_CASE = "testCase";

	private static final Logger log = LoggerFactory.getLogger( SoapUiProjectSelector.class );

	private final Property<File> projectFile;
	private final Property<String> testSuite;
	private final Property<String> testCase;

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
		Stage stage = BeanInjector.getBean( Stage.class );
		FilePicker picker = new FilePicker( stage, "Select SoapUI project", new ExtensionFilter( "SoapUI Project Files",
				"*.xml" ) );

		picker.selectedProperty().bindBidirectional( Properties.convert( projectFile ) );

		SelectionModelUtils.writableSelectedItemProperty( testSuiteCombo.getSelectionModel() ).bindBidirectional(
				Properties.convert( testSuite ) );
		SelectionModelUtils.writableSelectedItemProperty( testCaseCombo.getSelectionModel() ).bindBidirectional(
				Properties.convert( testCase ) );

		VBox vBox = VBoxBuilder
				.create()
				.fillWidth( true )
				.prefHeight( 325 )
				.prefHeight( 160 )
				.children( new Label( "SoapUI Project" ), picker, new Label( "TestSuite" ), testSuiteCombo,
						new Label( "TestCase" ), testCaseCombo ).build();
		final CustomMenuItem popup = CustomMenuItemBuilder.create().styleClass( "project-selector" ).hideOnClick( false )
				.content( vBox ).build();

		GridPane grid = GridPaneBuilder.create().rowConstraints( new RowConstraints( 18 ) ).hgap( 28 ).build();

		MenuButton menuButton = MenuButtonBuilder.create().text( "Project" ).items( popup ).build();
		BeanInjector.getBean( Stage.class ).getScene().getStylesheets()
				.add( SoapUiProjectSelector.class.getResource( "loadui-soapui-plugin-style.css" ).toExternalForm() );

		grid.add( menuButton, 0, 0 );
		grid.add( new Label( "TestSuite" ), 1, 0 );
		grid.add( new Label( "TestCase" ), 2, 0 );

		final Label projectLabel = new Label();
		projectFile.getOwner().addEventListener( PropertyEvent.class, new EventHandler<PropertyEvent>()
		{
			@Override
			public void handleEvent( final PropertyEvent event )
			{
				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						if( event.getProperty() == projectFile )
							projectLabel.setText( projectFile == null ? "" : projectFile.getValue().getName() );
					}
				} );
			}
		} );
		final Label testSuiteLabel = new Label();
		testSuiteLabel.textProperty().bind( Properties.convert( testSuite ) );
		final Label testCaseLabel = new Label();
		testCaseLabel.textProperty().bind( Properties.convert( testCase ) );

		grid.add( projectLabel, 0, 1 );
		grid.add( testSuiteLabel, 1, 1 );
		grid.add( testCaseLabel, 2, 1 );

		return grid;
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

	public void setTestSuite( String name )
	{
		testSuite.setValue( name );
	}

	public String getTestCase()
	{
		return testCase.getValue();
	}

	public void setTestCase( String name )
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
		log.debug( "Updates TestSuites" );
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
		log.debug( "Updates TestCases" );
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				testCaseCombo.setItems( FXCollections.observableArrayList( testCases ) );
			}
		} );
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
					testCaseRunner.setTestSuite( testSuite.getValue() );
				else if( property == testCase )
				{
					log.debug( "Reload TestCase because testCase changed." );
					testCaseRunner.setNewTestCase( testCase.getValue() );
				}
			}
		}
	}
}

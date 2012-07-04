package com.eviware.loadui.components.soapui.layout;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.layout.OptionsProvider;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent.SoapUITestCaseRunner;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.impl.layout.OptionsProviderImpl;
import com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl;
import com.google.common.collect.ImmutableMap;

public class SoapUiProjectSelector
{
	public static final String TEST_CASE = "testCase";

	private static final Logger log = LoggerFactory.getLogger( SoapUiProjectSelector.class );

	private final Property<File> projectFile;
	private final Property<String> testSuite;
	private final Property<String> testCase;

	private final OptionsProviderImpl<String> testSuiteOptions = new OptionsProviderImpl<String>();
	private final OptionsProviderImpl<String> testCaseOptions = new OptionsProviderImpl<String>();

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

	public LayoutComponentImpl buildLayout()
	{
		PropertyLayoutComponentImpl<File> projectFileLayoutComponent = new PropertyLayoutComponentImpl<File>(
				ImmutableMap.<String, Object> builder() //
						.put( PropertyLayoutComponentImpl.PROPERTY, projectFile ) //
						.put( PropertyLayoutComponentImpl.LABEL, "soapUI Project" ) //
						.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 300!, spanx 2" ) //
						.put( "mode", "both" ) //
						.build() );

		PropertyLayoutComponentImpl<String> testSuiteLayoutComponent = new PropertyLayoutComponentImpl<String>(
				ImmutableMap.<String, Object> builder() //
						.put( PropertyLayoutComponentImpl.PROPERTY, testSuite ) //
						.put( PropertyLayoutComponentImpl.LABEL, "TestSuite" ) //
						.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 300!, spanx 2" ) //
						.put( "widget", "comboBox" ) //
						.put( OptionsProvider.OPTIONS, testSuiteOptions ) //
						.build() );

		PropertyLayoutComponentImpl<String> testCaseLayoutComponent = new PropertyLayoutComponentImpl<String>(
				ImmutableMap.<String, Object> builder() //
						.put( PropertyLayoutComponentImpl.PROPERTY, testCase ) //
						.put( PropertyLayoutComponentImpl.LABEL, "soapUI TestCase" ) //
						.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 300!, spanx 2" ) //
						.put( "widget", "comboBox" ) //
						.put( OptionsProvider.OPTIONS, testCaseOptions ) //
						.build() );

		return new LayoutComponentImpl( ImmutableMap.<String, Object> builder()
				.put( "soapUIProject", projectFileLayoutComponent ).put( "testSuite", testSuiteLayoutComponent )
				.put( TEST_CASE, testCaseLayoutComponent ).put( PropertyLayoutComponentImpl.CONSTRAINTS, "h 50!" ) //
				.build() );
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
		testSuiteOptions.setOptions( testSuites );
	}

	public void setTestCases( final String... testCases )
	{
		testCaseOptions.setOptions( testCases );
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

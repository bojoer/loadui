/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.components.soapui.layout;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.layout.OptionsProvider;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.components.soapui.SoapUIComponentActivator;
import com.eviware.loadui.components.soapui.SoapUISamplerComponent.SoapUITestCaseRunner;
import com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl;
import com.eviware.loadui.impl.layout.SettingsLayoutContainerImpl;
import com.google.common.collect.ImmutableMap;

public class GeneralSettings
{
	private static final Logger log = LoggerFactory.getLogger( GeneralSettings.class );

	private static final String DISABLE_SOAPUI_ASSERTIONS = "disableSoapuiAssertions";
	private static final String CLOSE_CONNECTIONS_AFTER_REQUEST = "closeConnectionsAfterRequest";
	private static final String TEST_CASE_ONLY = "TestCase only";
	public static final String PROJECT_PASSWORD = "_projectPassword";
	public static final String SETTINGS_FILE = "settingsFile";
	public static final String OUTPUT_LEVEL = "OutputLevel";
	public static final String RAISE_ERROR = "raiseError";
	public static final String OUTPUT_TESTCASE_PROPERTIES = "outputTestCaseProperties";
	public static final String USE_PROJECT_RELATIVE_PATH = "useProjectRelativePath";
	public static final String ALL_TEST_STEPS_INCLUDED = "TestCase and all TestSteps";
	public static final String REQUEST_TEST_STEPS_INCLUDED = "TestCase and RequestTestSteps";

	private final List<String> outputLevelOptions = Arrays.asList( TEST_CASE_ONLY, REQUEST_TEST_STEPS_INCLUDED,
			ALL_TEST_STEPS_INCLUDED );

	private final Property<Boolean> raiseAssertionOnError;

	private final Property<String> outputLevel;
	private final Property<File> settingsFile;
	private final Property<Boolean> useProjectRelativePath;
	private final Property<String> projectPassword;
	private final Property<Boolean> outputTestCaseProperties;
	private final Property<Boolean> closeConnections;
	private final Property<Boolean> disableSoapUIAssertions;

	public static GeneralSettings newInstance( ComponentContext context, SoapUITestCaseRunner testCaseRunner )
	{
		GeneralSettings instance = new GeneralSettings( context, testCaseRunner );
		instance.initSettingsFile();
		return instance;
	}

	private GeneralSettings( ComponentContext context, SoapUITestCaseRunner testCaseRunner )
	{
		settingsFile = context.createProperty( SETTINGS_FILE, File.class );
		projectPassword = context.createProperty( PROJECT_PASSWORD, String.class );
		useProjectRelativePath = context.createProperty( USE_PROJECT_RELATIVE_PATH, Boolean.class, false );
		raiseAssertionOnError = context.createProperty( RAISE_ERROR, Boolean.class, true );
		outputTestCaseProperties = context.createProperty( OUTPUT_TESTCASE_PROPERTIES, Boolean.class, true );
		outputLevel = context.createProperty( OUTPUT_LEVEL, String.class, TEST_CASE_ONLY );
		closeConnections = context.createProperty( CLOSE_CONNECTIONS_AFTER_REQUEST, Boolean.class, false );
		disableSoapUIAssertions = context.createProperty( DISABLE_SOAPUI_ASSERTIONS, Boolean.class, false );
		context.addEventListener( PropertyEvent.class, new PropertyChangedHandler( testCaseRunner ) );
	}

	public Boolean getRaiseAssertionOnError()
	{
		return raiseAssertionOnError.getValue();
	}

	public String getOutputLevel()
	{
		return outputLevel.getValue();
	}

	public File getSettingsFile()
	{
		return settingsFile.getValue();
	}

	public void setSettingsFile( File f )
	{
		settingsFile.setValue( f );
	}

	public Boolean getUseProjectRelativePath()
	{
		return useProjectRelativePath.getValue();
	}

	public String getProjectPassword()
	{
		return projectPassword.getValue();
	}

	public void setProjectPassword( String p )
	{
		projectPassword.setValue( p );
	}

	public Boolean getOutputTestCaseProperties()
	{
		return outputTestCaseProperties.getValue();
	}

	public Boolean getCloseConnections()
	{
		return closeConnections.getValue();
	}

	public Boolean getDisableSoapUIAssertions()
	{
		return disableSoapUIAssertions.getValue();
	}

	public void setDisableSoapUIAssertions( boolean areDisabled )
	{
		disableSoapUIAssertions.setValue( areDisabled );
	}

	public SettingsLayoutContainerImpl buildLayout()
	{
		SettingsLayoutContainerImpl settingsLayoutTab = new SettingsLayoutContainerImpl( "General", "", "", "align top",
				"" );

		settingsLayoutTab.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, raiseAssertionOnError ) //
				.put( PropertyLayoutComponentImpl.LABEL, "Raise Project Failure on Error" ) //
				.build() ) );

		settingsLayoutTab.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, outputLevel ) //
				.put( PropertyLayoutComponentImpl.LABEL, "Output Level" ) //
				.put( OptionsProvider.OPTIONS, outputLevelOptions ) //
				.build() ) );

		settingsLayoutTab.add( new PropertyLayoutComponentImpl<File>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, settingsFile ) //
				.put( PropertyLayoutComponentImpl.LABEL, "SoapUI settings" ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 200!, spanx 2" ) //
				.build() ) );

		settingsLayoutTab.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, useProjectRelativePath ) //
				.put( PropertyLayoutComponentImpl.LABEL, "Use relative path for project" ) //
				.build() ) );

		settingsLayoutTab.add( new PropertyLayoutComponentImpl<File>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, projectPassword ) //
				.put( PropertyLayoutComponentImpl.LABEL, "Project Password" ) //
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "w 200!, spanx 2" ) //
				.put( "widget", "password" ) //
				.build() ) );

		settingsLayoutTab.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, outputTestCaseProperties ) //
				.put( PropertyLayoutComponentImpl.LABEL, "Add TestCase Properties to Result Message" ) //
				.build() ) );

		settingsLayoutTab.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, closeConnections ) //
				.put( PropertyLayoutComponentImpl.LABEL, "Close connections between each request" ) //
				.build() ) );

		settingsLayoutTab.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, disableSoapUIAssertions ) //
				.put( PropertyLayoutComponentImpl.LABEL, "Disable all SoapUI assertions" ) //
				.build() ) );

		return settingsLayoutTab;
	}

	public void initSettingsFile()
	{
		File current = SoapUIComponentActivator.loadSettings( getSettingsFile() );
		if( current.exists() )
			setSettingsFile( current );
	}

	private class PropertyChangedHandler implements EventHandler<PropertyEvent>
	{
		private final SoapUITestCaseRunner testCaseRunner;

		public PropertyChangedHandler( SoapUITestCaseRunner testCaseRunner )
		{
			this.testCaseRunner = testCaseRunner;
		}

		@Override
		public void handleEvent( PropertyEvent event )
		{
			Property<?> property = event.getProperty();

			if( property == settingsFile )
			{
				File current = SoapUIComponentActivator.loadSettings( settingsFile.getValue() );
				if( !current.equals( settingsFile.getValue() ) && current.exists() )
				{
					settingsFile.setValue( current );
				}
				initSettingsFile();
			}
			else if( property == closeConnections )
			{
				testCaseRunner.setCloseConnections( closeConnections.getValue() );
			}
			else if( property == disableSoapUIAssertions )
			{
				log.debug( "Reload TestCase because disableSoapUIAssertions changed." );
				testCaseRunner.reloadTestCase();
			}

		}

	}
}

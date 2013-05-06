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
package com.eviware.loadui.components.soapui;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javafx.scene.Node;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.component.ComponentContext.Scope;
import com.eviware.loadui.api.component.categories.RunnerCategory;
import com.eviware.loadui.api.counter.CounterHolder;
import com.eviware.loadui.api.events.ActionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.events.WeakEventHandler;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.api.layout.SettingsLayoutContainer;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.serialization.Value;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.terminal.OutputTerminal;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.components.soapui.layout.GeneralSettings;
import com.eviware.loadui.components.soapui.layout.MetricsDisplay;
import com.eviware.loadui.components.soapui.layout.MiscLayoutComponents;
import com.eviware.loadui.components.soapui.layout.SoapUiProjectSelector;
import com.eviware.loadui.components.soapui.testStepsTable.TestStepsTableModel;
import com.eviware.loadui.components.soapui.utils.SoapUiProjectUtils;
import com.eviware.loadui.impl.component.ActivityStrategies;
import com.eviware.loadui.impl.component.categories.RunnerBase;
import com.eviware.loadui.impl.layout.ActionLayoutComponentImpl;
import com.eviware.loadui.impl.layout.LayoutComponentImpl;
import com.eviware.loadui.impl.layout.LayoutContainerImpl;
import com.eviware.loadui.impl.layout.PropertyLayoutComponentImpl;
import com.eviware.loadui.impl.layout.SeparatorLayoutComponentImpl;
import com.eviware.loadui.impl.layout.SettingsLayoutContainerImpl;
import com.eviware.loadui.integration.SoapUIProjectLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.config.LoadTestConfig;
import com.eviware.soapui.config.SecurityTestConfig;
import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTestContext;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.datasink.DataSink;
import com.eviware.soapui.impl.wsdl.teststeps.datasource.DataSource;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTestRunListener;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class SoapUISamplerComponent extends RunnerBase
{
	/**
	 * Creates a real deep copy of a TestCaseConfig, since .copy() doesn't quite
	 * do it.
	 * 
	 * @param config
	 * @return
	 */
	private static TestCaseConfig deepCopy( TestCaseConfig config )
	{
		final SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();

		try
		{
			return TestCaseConfig.Factory.parse( config.toString() );
		}
		catch( XmlException e )
		{
			log.error( "Failed manual copy, using .copy() instead: ", e );
			return ( TestCaseConfig )config.copy();
		}
		finally
		{
			state.restore();
		}
	}

	public static final String PROJECT_FILE_WORKING_COPY = "_projectFileworkingCopy";
	public static final String PROJECT_RELATIVE_PATH = "projectRelativePath";

	public static final String SOAPUI_CONTEXT_PARAM = "soapui_context";

	public static final String PROPERTIES = SoapUISamplerComponent.class.getSimpleName() + "_properties";
	private static final String DISABLED_TESTSTEPS = "disabledTestSteps";
	public static final String TYPE = SoapUISamplerComponent.class.getName();

	@SuppressWarnings( "hiding" )
	private static final Logger log = LoggerFactory.getLogger( SoapUISamplerComponent.class );

	private static final Joiner.MapJoiner mapJoiner = Joiner.on( ',' ).withKeyValueSeparator( "=" );
	private static final Splitter.MapSplitter mapSplitter = Splitter.on( ',' ).omitEmptyStrings()
			.withKeyValueSeparator( "=" );

	private static final ThreadLocal<StringToObjectMap> runContexts = new ThreadLocal<>();

	// Properties

	/*
	 * Working copy is always non composite project, so it can be propagated to
	 * the agents. If original project is non composite, it is loaded and its non
	 * composite copy is created.
	 */
	private final Property<File> projectFileWorkingCopy;

	/*
	 * project relative path. this is also non relevant on agents, since the path
	 * used there is always different than on controller.
	 */
	private final Property<String> projectRelativePath;
	private final Property<String> testSteps_isDisabled;

	private final TestStepNotifier testStepNotifier = new TestStepNotifier();
	private final List<WsdlTestCaseRunner> runners = Collections.synchronizedList( new ArrayList<WsdlTestCaseRunner>() );

	private final SoapUITestCaseRunner runner = new SoapUITestCaseRunner();

	private final ResetActionListener actionListener = new ResetActionListener();

	private boolean reloadingProject;
	WsdlTestCase soapuiTestCase;

	private final SoapUiProjectSelector projectSelector;
	private final SoapUILoadTestRunner loadTestRunner = new SoapUILoadTestRunner( this );
	@CheckForNull
	private WsdlLoadTestContext loadTestRunContext = null;
	final DummyLoadTest soapuiLoadTest = new DummyLoadTest( this );
	private final OutputTerminal errorTerminal;
	private final AtomicLong sampleIndex = new AtomicLong();
	private final ActionLayoutComponentImpl runOnceAction;
	private final ActionLayoutComponentImpl openInSoapUIAction;

	private final GeneralSettings generalSettings;

	private final ScheduledExecutorService executor;
	private File loaduiProjectFolder;
	private final Map<String, StatisticVariable.Mutable> timeTakenVariableMap = new HashMap<>();
	private final Map<String, StatisticVariable.Mutable> responseSizeVariableMap = new HashMap<>();

	private final ConcurrentMap<String, String> testSteps_isDisabled_Map = Maps.newConcurrentMap();

	private final ConcurrentMap<String, Value<Number>> totalValues = Maps.newConcurrentMap();
	private final LoadingCache<String, AtomicInteger> testStepsInvocationCount = CacheBuilder.newBuilder().build(
			new CacheLoader<String, AtomicInteger>()
			{
				@Override
				public AtomicInteger load( final String stepName ) throws Exception
				{
					return new AtomicInteger();
				}
			} );

	private final TestStepsTableModel testStepsTableModel;
	private final MetricsDisplay metricsDisplay;

	public SoapUISamplerComponent( ComponentContext context )
	{
		super( context );
		context.setHelpUrl( "http://www.loadui.org/Runners/soapui-runner-component.html" );
		errorTerminal = context.createOutput( "samplerErrors", "Errors", "Outputs the result of each failed request." );
		Map<String, Class<?>> resultSignature = Maps.newHashMap();
		resultSignature.put( "SampleIndex", Long.class );
		resultSignature.put( TIMESTAMP_MESSAGE_PARAM, Long.class );
		resultSignature.put( "Reason", String.class );
		resultSignature.put( "Results", String[].class );
		context.setSignature( errorTerminal, resultSignature );

		projectFileWorkingCopy = context.createProperty( PROJECT_FILE_WORKING_COPY, File.class );
		projectRelativePath = context.createProperty( PROJECT_RELATIVE_PATH, String.class, null, false );

		testSteps_isDisabled = context.createProperty( DISABLED_TESTSTEPS, String.class, "" );

		ProjectItem project = context.getCanvas().getProject();

		testStepsTableModel = new TestStepsTableModel( this );
		generalSettings = GeneralSettings.newInstance( context, runner );
		projectSelector = SoapUiProjectSelector.newInstance( this, context, runner );

		SoapUiProjectUtils.registerJdbcDrivers();

		// If on controller, set working copy of the project file. This does not
		// make sense on agents since projectFileWorkingCopy is propagated from
		// the controller.
		if( context.isController() )
		{
			loaduiProjectFolder = project.getProjectFile().getParentFile();
			if( generalSettings.getUseProjectRelativePath() )
			{
				// If relative path is used, calculate real (absolute) path and set
				// it.
				File relativeFile = new File( loaduiProjectFolder, projectRelativePath.getValue() );
				if( relativeFile.exists() )
					projectSelector.setProjectFile( relativeFile );
			}
			projectFileWorkingCopy.setValue( SoapUiProjectUtils.makeNonCompositeCopy( projectSelector.getProjectFile() ) );
		}

		setProject( projectFileWorkingCopy.getValue() );
		setTestSuite( projectSelector.getTestSuite() );
		runner.reloadTestCase();

		context.addEventListener( ActionEvent.class, actionListener );

		getContext().setInvalid( soapuiTestCase == null );

		LayoutContainer layout = new LayoutContainerImpl( "gap 10 5", "", "align top", "" );
		LayoutContainer box = new LayoutContainerImpl( "ins 0", "", "align top", "" );

		box.add( projectSelector.buildLayout() );

		layout.add( box );
		layout.add( new SeparatorLayoutComponentImpl( false, "newline, growx, spanx" ) );

		box = new LayoutContainerImpl( "wrap 3, ins 0", "", "align top", "" );

		boolean isHeadless = GraphicsEnvironment.isHeadless();
		if( isHeadless )
		{
			log.debug( "Skipping creation of SoapUI Runner's TestStepsTable, since in headless mode." );
		}
		else
		{
			box.add( testStepsTableModel.buildLayout() );
		}

		openInSoapUIAction = MiscLayoutComponents.buildOpenInSoapUiButton( projectSelector.getProjectFileName(),
				projectSelector.getTestSuite(), projectSelector.getTestCase() );
		openInSoapUIAction.setEnabled( soapuiTestCase != null );
		box.add( openInSoapUIAction );

		runOnceAction = new ActionLayoutComponentImpl( ImmutableMap.<String, Object> builder() //
				.put( ActionLayoutComponentImpl.LABEL, "Run Once" ) //
				.put( ActionLayoutComponentImpl.ACTION, new Runnable()
				{
					@Override
					public void run()
					{
						getContext().triggerAction( RunnerCategory.SAMPLE_ACTION, Scope.COMPONENT );
					}
				} ).build() );

		runOnceAction.setEnabled( soapuiTestCase != null );
		box.add( runOnceAction );

		ActionLayoutComponentImpl abortRunningAction = new ActionLayoutComponentImpl( ImmutableMap
				.<String, Object> builder() //
				.put( ActionLayoutComponentImpl.LABEL, "Abort Running TestCases" ) //
				.put( ActionLayoutComponentImpl.ACTION, new Runnable()
				{
					@Override
					public void run()
					{
						getContext().triggerAction( "ABORT", Scope.COMPONENT );
					}
				} ).build() );

		box.add( abortRunningAction );

		layout.add( box );
		layout.add( new SeparatorLayoutComponentImpl( true, "growy" ) );

		LayoutContainer wrapperBox = new LayoutContainerImpl( "wrap, ins 0", "", "align top", "" );

		metricsDisplay = new MetricsDisplay( this );
		LayoutContainer metrics = metricsDisplay.buildLayout();

		wrapperBox.add( metrics );

		LayoutContainer compactLayout = new LayoutContainerImpl( Collections.<String, Object> emptyMap() );
		compactLayout.add( metrics );
		context.setCompactLayout( compactLayout );

		wrapperBox.add( new ActionLayoutComponentImpl( ImmutableMap.<String, Object> builder()
				.put( ActionLayoutComponentImpl.LABEL, "Reset" )
				.put( PropertyLayoutComponentImpl.CONSTRAINTS, "align right" )
				.put( ActionLayoutComponentImpl.ACTION, new Runnable()
				{
					@Override
					public void run()
					{
						metricsDisplay.setResetValues( getSampleCounter().get(), getDiscardCounter().get(),
								getFailureCounter().get() );
					}
				} ).build() ) );

		layout.add( wrapperBox );

		clearAndCreateSettingTabs( context );
		context.setLayout( layout );

		executor = Executors.newSingleThreadScheduledExecutor( new ThreadFactoryBuilder().setDaemon( true )
				.setNameFormat( "soapUI-runner-%d" ).build() );
		executor.scheduleAtFixedRate( ledUpdater, 500, 500, TimeUnit.MILLISECONDS );
		executor.scheduleAtFixedRate( new Runnable()
		{
			@Override
			public void run()
			{
				TerminalMessage message = getContext().newMessage();
				metricsDisplay.appendMetricsToMessage( message );
			}
		}, 1000, 1000, TimeUnit.MILLISECONDS );

		context.addEventListener( PropertyEvent.class, new PropertyChangedListener() );
		context.addEventListener( ActionEvent.class, new EventHandler<ActionEvent>()
		{
			@Override
			public void handleEvent( ActionEvent event )
			{
				if( event.getKey().equals( "RESET" ) )
				{
					metricsDisplay.setResetValues( 0, 0, 0 );
					sampleIndex.set( 0 );
					testStepsInvocationCount.invalidateAll();
				}
			}
		} );
	}

	private void clearAndCreateSettingTabs( ComponentContext context )
	{
		context.clearSettingsTabs();
		context.addSettingsTab( generalSettings.buildLayout() );
		// testcase properties tab
		SettingsLayoutContainerImpl settingsTestCaseTab = new SettingsLayoutContainerImpl( "Properties", "", "",
				"align top", "" );

		HashMap<String, Callable<Node>> nodeMap = new HashMap<>();
		nodeMap.put( "component", TestCasePropertiesNode.createTableView( this, context ) );

		settingsTestCaseTab.add( new LayoutComponentImpl( nodeMap ) );

		context.addSettingsTab( settingsTestCaseTab );
		context.addSettingsTab( generateAdvancedTab() );
	}

	private SettingsLayoutContainer generateAdvancedTab()
	{
		SettingsLayoutContainer advancedSettings = new SettingsLayoutContainerImpl( "Advanced", "", "", "align top", "" );
		advancedSettings.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, concurrentSamplesProperty ) //
				.put( PropertyLayoutComponentImpl.LABEL, "Max concurrent requests" ) //
				.build() ) );
		advancedSettings.add( new PropertyLayoutComponentImpl<String>( ImmutableMap.<String, Object> builder() //
				.put( PropertyLayoutComponentImpl.PROPERTY, maxQueueSizeProperty ) //
				.put( PropertyLayoutComponentImpl.LABEL, "Max queue size" ) //
				.build() ) );
		return advancedSettings;
	}

	boolean isOnRunningCanvas()
	{
		return getContext().getCanvas().isRunning();
	}

	String getLabel()
	{
		return getContext().getLabel();
	}

	String getId()
	{
		return getContext().getId();
	}

	public final void setProject( File projectFile )
	{
		if( projectFile == null )
		{
			return;
		}
		else if( !projectFile.exists() )
		{
			showMessage( "Specified SoapUI project file " + projectFile.getAbsolutePath()
					+ " does not exist. File may have been moved, renamed or deleted." );
			return;
		}

		log.debug( "Setting SoapUI project to {}", projectFile );
		runner.reloadProject( projectFile );
	}

	public void setTestCase( String name )
	{
		projectSelector.setTestCase( name );
	}

	public final void setTestSuite( final String testSuiteName )
	{
		runner.setTestSuite( testSuiteName );
	}

	private void showMessage( final String message )
	{
		// open new thread and set class loader so UISupport can display
		// dialogs
		new Thread()
		{
			@Override
			public void run()
			{
				// wait for openInSoapUIAction to initialize in order to take its
				// class loader
				while( openInSoapUIAction == null )
				{
					try
					{
						sleep( 100 );
					}
					catch( InterruptedException e )
					{
						e.printStackTrace();
					}
				}
				setContextClassLoader( openInSoapUIAction.getClass().getClassLoader() );
				UISupport.showErrorMessage( message );
			}
		}.start();
	}

	private void clearStatisticVariables()
	{
		for( StatisticVariable.Mutable variable : timeTakenVariableMap.values() )
		{
			getContext().removeStatisticVariable( variable.getLabel() );
		}
		for( StatisticVariable.Mutable variable : responseSizeVariableMap.values() )
			getContext().removeStatisticVariable( variable.getLabel() );
		timeTakenVariableMap.clear();
		responseSizeVariableMap.clear();
	}

	public int getTestStepInvocationCount( TestStep step )
	{
		return ( int )( totalValues.get( step.getName() ).getValue().longValue() % 100000 );
	}

	public void setTestStepIsDisabled( TestStep step, boolean isDisabled )
	{
		setTestStepIsDisabled( step.getName(), isDisabled );
	}

	public void setTestStepIsDisabled( String stepName, boolean isDisabled )
	{
		testSteps_isDisabled_Map.put( escapeTestStepName( stepName ), Boolean.toString( isDisabled ) );
		testSteps_isDisabled.setValue( mapJoiner.join( testSteps_isDisabled_Map ) );
	}

	private static String escapeTestStepName( String name )
	{
		return name.replace( '=', '!' ).replace( ',', '*' );
	}

	public void setDisableSoapUIAssertions( boolean areDisabled )
	{
		generalSettings.setDisableSoapUIAssertions( areDisabled );
	}

	private void unsetProject()
	{
		projectFileWorkingCopy.setValue( null );
		projectRelativePath.setValue( null );
		runner.setTestCase( null );
		runner.setTestSuite( null );
		projectSelector.reset();
	}

	public void onProjectUpdated( File projectFile )
	{
		if( getContext().isController() && !reloadingProject )
		{
			projectRelativePath.setValue( SoapUIComponentActivator.findRelativePath( loaduiProjectFolder, projectFile ) );
			projectFileWorkingCopy.setValue( SoapUiProjectUtils.makeNonCompositeCopy( projectFile ) );
		}
	}

	public TestCase getTestCase()
	{
		return soapuiTestCase;
	}

	private final class PropertyChangedListener implements EventHandler<PropertyEvent>
	{
		@Override
		public void handleEvent( PropertyEvent event )
		{
			if( event.getEvent() == PropertyEvent.Event.VALUE )
			{
				Property<?> property = event.getProperty();
				if( property == projectFileWorkingCopy && !reloadingProject )
				{
					log.debug( "setting project" );
					setProject( projectFileWorkingCopy.getValue() );
				}
				else if( property == testSteps_isDisabled )
				{
					log.debug( "Reload TestCase because testSteps_isDisabled changed." );
					runner.reloadTestCase();
				}
			}
		}
	}

	final Runnable ledUpdater = new Runnable()
	{
		@Override
		public void run()
		{
			if( soapuiTestCase != null )
			{
				if( getCurrentlyRunning() > 0 )
					getContext().setActivityStrategy( ActivityStrategies.BLINKING );
				else
					getContext().setActivityStrategy( ActivityStrategies.ON );
			}
			else
			{
				getContext().setActivityStrategy( ActivityStrategies.OFF );
			}
		}
	};

	@Override
	protected TerminalMessage sample( TerminalMessage triggerMessage, Object sampleId )
	{
		WsdlTestCaseRunner testCaseRunner = runner.run( triggerMessage );
		if( testCaseRunner != null )
		{
			// do this first so we don't override any of the default properties
			if( generalSettings.getOutputTestCaseProperties() != null && generalSettings.getOutputTestCaseProperties() )
			{
				for( String name : testCaseRunner.getTestCase().getPropertyNames() )
				{
					triggerMessage.put( name, testCaseRunner.getTestCase().getPropertyValue( name ) );
				}
			}

			//Copy the run context to the output message
			StringToObjectMap soapUIContext = runContexts.get();
			runContexts.remove();
			if( soapUIContext != null )
			{
				triggerMessage.put( SOAPUI_CONTEXT_PARAM, soapUIContext );
			}

			triggerMessage.put( STATUS_MESSAGE_PARAM, testCaseRunner.getStatus() == TestRunner.Status.FINISHED );
			triggerMessage.put( TIME_TAKEN_MESSAGE_PARAM, testCaseRunner.getTimeTaken() );
			triggerMessage.put( TIMESTAMP_MESSAGE_PARAM, testCaseRunner.getStartTime() );
			triggerMessage.put( SAMPLE_ID, testCaseRunner.getTestCase().getName() );
			int size = 0;
			for( TestStepResult result : testCaseRunner.getResults() )
			{
				size += result.getSize();
			}
			triggerMessage.put( RESPONSE_SIZE_MESSAGE_PARAM, size );
		}

		return triggerMessage;
	}

	@Override
	public void onRelease()
	{
		super.onRelease();
		runner.release();
		executor.shutdown();
		testStepsTableModel.release();
		metricsDisplay.release();
	}

	private final class TestStepNotifier extends TestRunListenerAdapter
	{
		@Override
		public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
		{
			for( LoadTestRunListener listener : soapuiLoadTest.getLoadTestRunListeners() )
			{
				listener.beforeTestCase( loadTestRunner, loadTestRunContext, testRunner, runContext );
			}
		}

		@Override
		public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep )
		{
			for( LoadTestRunListener listener : soapuiLoadTest.getLoadTestRunListeners() )
			{
				listener.beforeTestStep( loadTestRunner, loadTestRunContext, testRunner, runContext, testStep );
			}
			testStepsInvocationCount.getUnchecked( testStep.getName() ).getAndAdd( 1 ); // + 1 );
		}

		@Override
		public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result )
		{
			for( LoadTestRunListener listener : soapuiLoadTest.getLoadTestRunListeners() )
			{
				listener.afterTestStep( loadTestRunner, loadTestRunContext, testRunner, runContext, result );
			}

			if( ( generalSettings.getOutputLevel().equals( GeneralSettings.REQUEST_TEST_STEPS_INCLUDED ) && result
					.getTestStep() instanceof SamplerTestStep )
					|| generalSettings.getOutputLevel().equals( GeneralSettings.ALL_TEST_STEPS_INCLUDED ) )
			{
				TerminalMessage message = getContext().newMessage();

				message.put( RunnerCategory.SAMPLE_ID, result.getTestStep().getName() );
				message.put( STATUS_MESSAGE_PARAM, result.getStatus() == TestStepStatus.OK );
				message.put( "TestStepStatus", result.getStatus() );
				message.put( TIME_TAKEN_MESSAGE_PARAM, result.getTimeTaken() );
				message.put( TIMESTAMP_MESSAGE_PARAM, result.getTimeStamp() );
				message.put( RESPONSE_SIZE_MESSAGE_PARAM, result.getSize() );
				message.put( SOAPUI_CONTEXT_PARAM, new StringToObjectMap( runContext.getProperties() ) );

				getContext().send( getResultTerminal(), message );
			}

			// Update the StatisticalVariables related to the TestStep
			String testStepName = result.getTestStep().getName();
			StatisticVariable.Mutable timeTakenVariable = timeTakenVariableMap.get( testStepName );
			timeTakenVariable.update( result.getTimeStamp(), result.getTimeTaken() );

			if( responseSizeVariableMap.containsKey( testStepName ) )
			{
				StatisticVariable.Mutable responseSizeVariable = responseSizeVariableMap.get( testStepName );
				responseSizeVariable.update( result.getTimeStamp(), result.getSize() );
			}
		}

		@Override
		public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
		{
			for( LoadTestRunListener listener : soapuiLoadTest.getLoadTestRunListeners() )
			{
				listener.afterTestCase( loadTestRunner, loadTestRunContext, testRunner, runContext );
			}

			runContexts.set( new StringToObjectMap( runContext.getProperties() ) );
		}
	}

	public class SoapUITestCaseRunner implements SoapUIProjectLoader.ProjectUpdateListener, Releasable
	{
		private WsdlProject project;
		private WsdlTestSuite testSuite;
		private TestCaseConfig config;
		private final List<WsdlTestCase> testCasePool = Collections.synchronizedList( new LinkedList<WsdlTestCase>() );
		private int testCaseRevisionCount = 0;
		private final Map<WsdlTestCase, Integer> testCaseRevisions = Maps.newHashMap();

		public SoapUITestCaseRunner()
		{
			SoapUIProjectLoader.getInstance().addProjectUpdateListener( this );
		}

		public WsdlTestCaseRunner run( TerminalMessage triggerMessage )
		{
			if( soapuiTestCase == null || reloadingProject )
				return null;

			SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
			WsdlTestCase testCase = null;
			WsdlTestCaseRunner testCaseRunner = null;
			boolean failedWithException = false;
			long index = sampleIndex.getAndIncrement();
			try
			{
				testCase = getTestCase();
				testCaseRevisions.put( testCase, testCaseRevisionCount );
				TestCasePropertiesNode.overrideTestCaseProperties( testCase, getContext().getProperties() );
				TestCasePropertiesNode.overrideTestCaseProperties( testCase, triggerMessage );
				testCase.addTestRunListener( testStepNotifier );

				//Use existing context if available
				StringToObjectMap soapUIContext = triggerMessage.get( SOAPUI_CONTEXT_PARAM ) instanceof StringToObjectMap ? ( StringToObjectMap )triggerMessage
						.get( SOAPUI_CONTEXT_PARAM ) : new StringToObjectMap();
				testCaseRunner = new WsdlTestCaseRunner( testCase, soapUIContext );

				testCaseRunner.getRunContext().setProperty( TestCaseRunContext.THREAD_INDEX, getCurrentlyRunning() - 1 );
				testCaseRunner.getRunContext().setProperty( TestCaseRunContext.RUN_COUNT, getSampleCounter().get() );
				testCaseRunner.getRunContext().setProperty( TestCaseRunContext.LOAD_TEST_RUNNER, loadTestRunner );
				testCaseRunner.getRunContext().setProperty( TestCaseRunContext.LOAD_TEST_CONTEXT, loadTestRunContext );
				testCaseRunner.getRunContext().setProperty( TestCaseRunContext.TOTAL_RUN_COUNT, getSampleCounter().get() );

				runners.add( testCaseRunner );
				testCaseRunner.run();
			}
			catch( Exception e )
			{
				failedWithException = true;
				e.printStackTrace();

				getFailureCounter().increment();
				getFailedAssertionCounter().increment();

				if( generalSettings.getRaiseAssertionOnError() )
				{
					getContext().getCounter( CanvasItem.ASSERTION_COUNTER ).increment();
				}
			}
			finally
			{
				if( testCase != null )
				{
					testCase.removeTestRunListener( testStepNotifier );

					// only put back if it hasn't changed because of a reload
					if( testCaseRevisions.get( testCase ) == testCaseRevisionCount )
					{
						SoapUiProjectUtils.clearResponse( testCase );
						testCasePool.add( testCase );
					}
					else
					{
						log.debug( "Dropping testCase" );
						testCase.release();
					}
				}

				if( testCaseRunner != null )
				{
					if( !failedWithException && testCaseRunner.getStatus() == Status.FAILED )
					{
						getFailureCounter().increment();
						getFailedAssertionCounter().increment();

						if( generalSettings.getRaiseAssertionOnError() )
							getContext().getCounter( CanvasItem.ASSERTION_COUNTER ).increment();

						TerminalMessage errorMessage = getContext().newMessage();
						errorMessage.put( "SampleIndex", index );
						errorMessage.put( TIMESTAMP_MESSAGE_PARAM, testCaseRunner.getStartTime() );
						errorMessage.put( "Reason", testCaseRunner.getReason() );

						StringList results = new StringList();
						for( TestStepResult result : testCaseRunner.getResults() )
						{
							results.addAll( result.getMessages() );
						}

						errorMessage.put( "Results", results );

						getContext().send( errorTerminal, errorMessage );
					}

					runners.remove( testCaseRunner );
				}

				state.restore();
			}

			return testCaseRunner;
		}

		public synchronized void setNewTestCase( final String testCaseName )
		{
			testSteps_isDisabled_Map.clear();
			testSteps_isDisabled.setValue( "" );
			setTestCase( testCaseName );

			clearOveriddableProperties( getContext() );
		}

		private void clearOveriddableProperties( ComponentContext context )
		{
			for( Property<?> property : context.getProperties() )
			{
				if( property.getKey().startsWith( TestCasePropertiesNode.OVERRIDING_VALUE_PREFIX ) )
				{
					context.deleteProperty( property.getKey() );
				}

			}
		}

		private synchronized WsdlTestCase getTestCase()
		{
			if( testCasePool.isEmpty() )
			{
				if( config == null )
				{
					config = deepCopy( soapuiTestCase.getConfig() );
					config.setLoadTestArray( new LoadTestConfig[0] );
					config.setSecurityTestArray( new SecurityTestConfig[0] );
				}

				WsdlTestCase tc = soapuiTestCase.getTestSuite().buildTestCase( deepCopy( config ), true );
				tc.afterLoad();
				Settings settings = tc.getSettings();
				settings.setBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES, false );
				settings.setBoolean( HttpSettings.CLOSE_CONNECTIONS, generalSettings.getCloseConnections() );
				tc.setDiscardOkResults( true );
				tc.setMaxResults( 0 );
				testCasePool.add( tc );
			}

			return testCasePool.remove( 0 );
		}

		@Override
		public void release()
		{
			SoapUIProjectLoader loader = SoapUIProjectLoader.getInstance();
			loader.removeProjectUpdateListener( this );

			for( WsdlTestCaseRunner runnerToCancel : runners )
				runnerToCancel.cancel( "Releasing SoapUIcomponent in loadUI" );

			for( WsdlTestCase tc : testCasePool )
				tc.release();

			runners.clear();
			testCasePool.clear();

			if( project != null )
				loader.releaseProject( project );
		}

		private void reloadProject( File projectFile2 )
		{
			log.debug( "reloadProject()" );
			final SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
			try
			{
				final SoapUIProjectLoader projectLoader = SoapUIProjectLoader.getInstance();
				if( !projectLoader.isProjectLoaded( projectFile2.getAbsolutePath() ) )
				{
					project = projectLoader.getProject( projectFile2.getAbsolutePath() );
				}
				else
				{
					project = projectLoader
							.getProject( projectFile2.getAbsolutePath(), generalSettings.getProjectPassword() );
				}
				if( project != null )
				{
					generalSettings.setProjectPassword( project.getShadowPassword() );
					initProject();
				}
				else
				{
					unsetProject();
				}
			}
			catch( Exception e )
			{
				log.debug( "Error reloading soapUI project: {} ", e );
			}
			finally
			{
				state.restore();
			}
		}

		public void setTestSuite( final String testSuiteName )
		{
			if( project == null || testSuiteName == null )
			{
				projectSelector.setTestSuites( new String[0] );
				return;
			}
			log.debug( "Setting soapUI TestSuite to {}", testSuiteName );

			SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
			try
			{
				testSuite = project.getTestSuiteByName( testSuiteName );

				String[] testCases = ModelSupport.getNames( testSuite.getTestCaseList() );
				if( testCases.length == 0 )
				{
					projectSelector.setTestCases( new String[0] );
				}
				else
				{
					projectSelector.setTestCases( testCases );
				}
				if( testSuite.getTestCaseByName( projectSelector.getTestCase() ) == null )
				{
					log.debug( "testCases[0]: " + testCases[0] );
					projectSelector.setTestCase( testCases.length > 0 ? testCases[0] : null );
				}
				else
				{
					log.debug( "Reloading testCase, because setTestSuite was called." );
					reloadTestCase();
				}

			}
			catch( Exception e )
			{
				log.debug( "Error when setting TestSuite {}", e );
			}
			finally
			{
				state.restore();
			}
		}

		public void setCloseConnections( boolean closeConnections )
		{
			synchronized( testCasePool )
			{
				for( WsdlTestCase tc : runner.testCasePool )
				{
					tc.getSettings().setBoolean( HttpSettings.CLOSE_CONNECTIONS, closeConnections );
				}
			}
		}

		public synchronized void reloadTestCase()
		{
			setTestCase( projectSelector.getTestCase() );
		}

		private synchronized void setTestCase( @CheckForNull final String testCaseName )
		{
			if( testSuite == null || testCaseName == null )
			{
				projectSelector.setTestCases( new String[0] );
				return;
			}

			log.debug( "Setting soapUI TestCase to {}", testCaseName );

			SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();

			try
			{
				WsdlTestCase soapuiTestCaseTemp = testSuite.getTestCaseByName( testCaseName );
				TestCaseConfig configTemp = deepCopy( soapuiTestCaseTemp.getConfig() );
				configTemp.setLoadTestArray( new LoadTestConfig[0] );
				configTemp.setSecurityTestArray( new SecurityTestConfig[0] );

				soapuiTestCase = soapuiTestCaseTemp.getTestSuite().buildTestCase( configTemp, false );
				soapuiTestCase.afterLoad();
				Settings settings = soapuiTestCase.getSettings();
				settings.setBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES, false );
				settings.setBoolean( HttpSettings.CLOSE_CONNECTIONS, generalSettings.getCloseConnections() );
				soapuiTestCase.setDiscardOkResults( true );
				soapuiTestCase.setMaxResults( 0 );

				// make sure there is an ID which is needed to detect if the
				// testCase has changed
				soapuiTestCase.getId();
				SoapUiProjectUtils.makeAllDataSourcesShared( soapuiTestCase );
				SoapUiProjectUtils.disableAllDataSourceLoops( soapuiTestCase );

				testCasePool.clear();
				config = null;
				loadTestRunContext = new WsdlLoadTestContext( loadTestRunner );
				log.debug( "TestCase set to {}.", soapuiTestCase.getName() );

				clearStatisticVariables();
				applyDisabledStateToTestSteps();
				if( generalSettings.getDisableSoapUIAssertions() )
					SoapUiProjectUtils.disableSoapUIAssertions( soapuiTestCase );
				generateStatisticVariables();
			}
			catch( Exception e )
			{
				log.error( "An error occured when trying to set TestCase.", e );
			}
			finally
			{
				state.restore();
				getContext().setInvalid( soapuiTestCase == null );
				if( openInSoapUIAction != null )
					openInSoapUIAction.setEnabled( soapuiTestCase != null );
				if( runOnceAction != null )
					runOnceAction.setEnabled( soapuiTestCase != null );
			}
			testStepsInvocationCount.invalidateAll();
			testStepsTableModel.updateTestCase( soapuiTestCase );
			//			testCasePropertiesNode.putTestCaseProperties( runner.getTestCase().getPropertyList() ); //TODO

			for( Map.Entry<String, Value<Number>> entry : totalValues.entrySet() )
			{
				removeTotal( entry.getKey() );
			}
			totalValues.clear();

			for( final String testStepName : soapuiTestCase.getTestSteps().keySet() )
			{
				totalValues.put( testStepName, createTotal( testStepName, new Callable<Number>()
				{
					@Override
					public Number call() throws Exception
					{
						return testStepsInvocationCount.get( testStepName ).get();
					}
				} ) );
			}
			testCaseRevisionCount++ ;
		}

		private synchronized void applyDisabledStateToTestSteps()
		{
			testSteps_isDisabled_Map.putAll( mapSplitter.split( testSteps_isDisabled.getValue() ) );
			for( TestStep step : soapuiTestCase.getTestStepList() )
			{
				WsdlTestStep wsdlStep = ( WsdlTestStep )step;
				wsdlStep.setDisabled( shouldTestStepBeDisabled( wsdlStep ) );
			}
		}

		private boolean shouldTestStepBeDisabled( @Nonnull final TestStep step )
		{
			if( testSteps_isDisabled_Map.containsKey( escapeTestStepName( step.getName() ) ) )
			{
				return Boolean.parseBoolean( testSteps_isDisabled_Map.get( escapeTestStepName( step.getName() ) ) );
			}
			return step.isDisabled();
		}

		private synchronized void generateStatisticVariables()
		{
			List<TestStep> testStepList = soapuiTestCase.getTestStepList();
			for( TestStep testStep : testStepList )
			{
				StatisticVariable.Mutable timeTakenVariable = getContext().addStatisticVariable(
						testStep.getName() + ": TimeTaken", "elapsed time for the TestStep to complete", "SAMPLE" );
				timeTakenVariableMap.put( testStep.getName(), timeTakenVariable );

				log.debug( "Added teststep: {}", testStep.getName() );

				if( testStep instanceof SamplerTestStep )
				{
					StatisticVariable.Mutable responseSizeVariable = getContext().addStatisticVariable(
							testStep.getName() + ": ResponseSize", "response size (in bytes)", "SAMPLE" );
					responseSizeVariableMap.put( testStep.getName(), responseSizeVariable );
				}
			}
		}

		@Override
		public void onProjectRelease( WsdlProject oldProject )
		{
		}

		@Override
		public void projectUpdated( String file, WsdlProject oldProject, WsdlProject newProject )
		{
			// our project?
			if( oldProject == project )
			{
				reloadingProject = true;

				synchronized( runners )
				{
					for( WsdlTestCaseRunner r : runners )
					{
						if( r.isRunning() )
							r.cancel( "Releasing component in loadUI" );
					}
				}
				// wait for runners to stop
				long limit = 60;
				while( limit > 0 && !runners.isEmpty() )
				{
					try
					{
						Thread.sleep( 1000 );
						limit-- ;
					}
					catch( InterruptedException e )
					{
						e.printStackTrace();
					}
				}
				for( WsdlTestCase tc : testCasePool )
					tc.release();

				runners.clear();
				testCasePool.clear();

				synchronized( this )
				{
					config = null;
				}

				SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
				try
				{
					SoapUIProjectLoader.getInstance().releaseProject( oldProject );
					project = newProject;

					if( newProject != null && !newProject.isWrongPasswordSupplied() )
					{
						initProject();
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
				finally
				{
					state.restore();
				}
				if( newProject != null && !newProject.isWrongPasswordSupplied() )
				{
					if( getContext().isController() )
					{
						// this will trigger change of projectFileWorkingCopy in
						// context listener
						projectSelector.setProjectFile( new File( file ) );
						projectRelativePath.setValue( SoapUIComponentActivator.findRelativePath( loaduiProjectFolder,
								projectSelector.getProjectFile() ) );
					}
					else
					{
						// update occurred on agent, not sure if this is necessary
						projectFileWorkingCopy.setValue( new File( file ) );
					}
					generalSettings.setProjectPassword( newProject.getShadowPassword() );
				}
				else
				{
					unsetProject();
				}
				reloadingProject = false;
			}
		}

		private void initProject()
		{
			log.debug( "initProject()" );
			String[] testSuites = ModelSupport.getNames( project.getTestSuiteList() );
			if( testSuites.length == 0 )
			{
				projectSelector.setTestSuites( new String[0] );
				projectSelector.setTestCases( new String[0] );
				projectSelector.setTestCase( null );
			}
			else
			{
				log.debug( "setTestSuites()" );
				projectSelector.setTestSuites( testSuites );
			}
			String current = projectSelector.getTestSuite();
			if( project.getTestSuiteByName( current ) == null )
			{
				projectSelector.setTestSuite( testSuites.length > 0 ? testSuites[0] : null );
			}
			else
				setTestSuite( current );
		}
	}

	@Override
	protected int onCancel()
	{
		SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
		int count = 0;
		try
		{
			synchronized( runners )
			{
				for( WsdlTestCaseRunner runnerToCancel : runners )
				{
					try
					{
						if( runnerToCancel.isRunning() )
						{
							runnerToCancel.cancel( "Canceled by loadUI" );
							count++ ;
						}
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
				}
			}
			runners.clear();
		}
		finally
		{
			state.restore();
		}
		return count;
	}

	private class ResetActionListener implements WeakEventHandler<ActionEvent>
	{
		@Override
		public void handleEvent( ActionEvent event )
		{
			if( CounterHolder.COUNTER_RESET_ACTION.equals( event.getKey() ) )
			{
				loadTestRunContext = new WsdlLoadTestContext( loadTestRunner );
			}
			else if( CanvasItem.COMPLETE_ACTION.equals( event.getKey() ) && loadTestRunContext != null )
			{
				for( Object o : loadTestRunContext.values() )
				{
					if( o instanceof DataSource )
					{
						( ( DataSource )o ).finish( null, null );
					}
					else if( o instanceof DataSink )
					{
						( ( DataSink )o ).finish( null, null );
					}
				}
			}
		}
	}
}

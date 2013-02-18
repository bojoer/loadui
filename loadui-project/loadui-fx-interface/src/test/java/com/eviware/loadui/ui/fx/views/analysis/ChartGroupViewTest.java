package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.test.TestFX.targetWindow;
import static com.eviware.loadui.ui.fx.util.test.TestFX.wrap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.scene.SceneBuilder;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.addressable.Addressable;
import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.statistics.model.Chart;
import com.eviware.loadui.api.statistics.model.Chart.Owner;
import com.eviware.loadui.api.statistics.model.ChartGroup;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.config.PropertyConfig;
import com.eviware.loadui.config.PropertyListConfig;
import com.eviware.loadui.config.impl.ChartConfigImpl;
import com.eviware.loadui.impl.statistics.model.ChartGroupImpl;
import com.eviware.loadui.impl.statistics.model.ChartImpl;
import com.eviware.loadui.ui.fx.util.test.FXScreenController;
import com.eviware.loadui.ui.fx.util.test.FXTestUtils;
import com.eviware.loadui.ui.fx.util.test.TestFX;
import com.eviware.loadui.util.test.BeanInjectorMocker;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;

public class ChartGroupViewTest
{

	private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
	private static TestFX controller;

	public static class TestApp extends Application
	{

		@Override
		public void start( Stage stage ) throws Exception
		{
			//FIXME this test should be implemented some time but Mockito is playing up and getting into an infinite recursion
			System.out.println("Mocking owner");
			Owner owner = mock( Owner.class );

			AddressableRegistry mockRegistry = mock( AddressableRegistry.class );
			when( mockRegistry.lookup( "SH" ) ).thenReturn( owner );

			System.out.println("Mocking BeanInjector");
			BeanInjectorMocker injector = BeanInjectorMocker.newInstance();
			injector.put( AddressableRegistry.class, mockRegistry );

			List<PropertyConfig> propertyConfigs = Lists.newArrayList( mock( PropertyConfig.class ) );

			System.out.println("Mocking PropertyListConfig");
			PropertyListConfig attribs = mock( PropertyListConfig.class );
			when( attribs.getPropertyList() ).thenReturn( propertyConfigs );

			ChartGroupImpl parent = mock( ChartGroupImpl.class );
			
			ChartConfigImpl config = mock( ChartConfigImpl.class );
			//when( config.isSetStatisticHolder() ).thenReturn( true );
			//when( config.getStatisticHolder() ).thenReturn( "SH" );
			//when( config.getAttributes() ).thenReturn( attribs );

			System.out.println("Creating new Chart");
			
			//Chart chart = new ChartImpl( parent, config );
			//Collection<Chart> charts = Arrays.asList( chart );
			//ChartGroup chartGroup = mock( ChartGroup.class );
			//when( chartGroup.getChildren() ).thenReturn( charts );
			
			//ObservableValue<Execution> currentExecution = mock( ObservableValue.class );

			Observable poll = mock( Observable.class );
			System.out.println("Creating new ChartGroupView");
			//final ChartGroupView view = new ChartGroupView( chartGroup, currentExecution, poll );

			//stage.setScene( SceneBuilder.create().root( view ).build() );
			System.out.println("Showing stage");
			//stage.show();
			stageFuture.set( stage );
		}

	}

	@Test
	public void test() throws InterruptedException
	{
		//TODO implement tests
	}

	@Before
	public void createWindow() throws Throwable
	{

		controller = wrap( new FXScreenController() );
		FXTestUtils.launchApp( TestApp.class );
		Stage stage = targetWindow( stageFuture.get( 5, TimeUnit.SECONDS ) );
		FXTestUtils.bringToFront( stage );

	}

}

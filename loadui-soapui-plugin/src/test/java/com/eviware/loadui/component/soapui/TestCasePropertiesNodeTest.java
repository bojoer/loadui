package com.eviware.loadui.component.soapui;

import static com.eviware.loadui.components.soapui.TestCasePropertiesNode.OVERRIDING_VALUE_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.components.soapui.TestCasePropertiesNode;
import com.eviware.loadui.ui.fx.util.TestingProperty;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestProperty;

public class TestCasePropertiesNodeTest
{
	private List<TestProperty> testCaseProperties = new ArrayList<>();

	@SuppressWarnings( "unchecked" )
	public ComponentContext createContextMock()
	{
		ComponentContext context = mock( ComponentContext.class );
		when( context.getProperties() ).thenReturn( Collections.<Property<?>> emptySet() );
		when( context.createProperty( Mockito.anyString(), ( Class<String> )Mockito.any( Class.class ), Mockito.any() ) )
				.thenAnswer( new Answer<Property<?>>()
				{
					@Override
					public Property<?> answer( InvocationOnMock invocation ) throws Throwable
					{
						Object[] args = invocation.getArguments();
						return new TestingProperty<String>( String.class, ( String )args[0], "testing Context" );
					}
				} );

		return context;
	}

	@Before
	public void setup()
	{
		testCaseProperties.clear();
		testCaseProperties.add( new TestTestProperty( "p1", "valuep-1" ) );
		testCaseProperties.add( new TestTestProperty( "p2", "valuep-2" ) );
		testCaseProperties.add( new TestTestProperty( "p3", "valuep-3" ) );
		testCaseProperties.add( new TestTestProperty( "p4", "valuep-4" ) );
		testCaseProperties.add( new TestTestProperty( "p5", "valuep-5" ) );
	}

	public WsdlTestCase createTestCaseMock()
	{
		WsdlTestCase testCase = mock( WsdlTestCase.class );
		when( testCase.getPropertyList() ).thenReturn( testCaseProperties );

		Mockito.doAnswer( new Answer<Void>()
		{
			@Override
			public Void answer( InvocationOnMock invocation ) throws Throwable
			{

				for( TestProperty p : testCaseProperties )
				{
					if( p.getName().equals( invocation.getArguments()[0] ) )
					{
						p.setValue( ( String )invocation.getArguments()[1] );
						return null;
					}
				}

				return null;
			}
		} ).when( testCase ).setPropertyValue( Mockito.anyString(), Mockito.anyString() );

		return testCase;
	}

	//	@Test
	//	public void putTestCasePropertiesTest()
	//	{
	//		ComponentContext context = createContextMock();
	//		TestCasePropertiesNode properties = new TestCasePropertiesNode( "", context );
	//		List<TestProperty> testProperties = new ArrayList<>();
	//
	//		for( int i = 1; i < 11; i++ )
	//		{
	//			testProperties.add( new TestTestProperty( "name " + i, "value " + i ) );
	//		}
	//
	//		properties.putTestCaseProperties( testProperties );
	//
	//		assertEquals( 10, properties.size() );
	//
	//		int i = 1;
	//		for( Property<?> s : properties.getData() )
	//		{
	//			assertEquals( TestCasePropertiesNode.OVERRIDING_VALUE_PREFIX + "name " + i, s.getKey() );
	//			assertEquals( "value " + i, s.getValue() );
	//			i++ ;
	//		}
	//
	//	}
	//
	//	@Ignore
	//	@Test
	//	public void putTestCasePropertiesResetTest()
	//	{
	//		ComponentContext context = createContextMock();
	//
	//		TestCasePropertiesNode properties = new TestCasePropertiesNode( "", context );
	//
	//		List<TestProperty> testProperties1 = new ArrayList<>();
	//
	//		for( int i = 1; i < 11; i++ )
	//		{
	//			testProperties1.add( new TestTestProperty( "name " + i, "value " + i ) );
	//		}
	//
	//		List<TestProperty> testProperties2 = new ArrayList<>();
	//
	//		for( int i = 1; i < 11; i++ )
	//		{
	//			testProperties2.add( new TestTestProperty( "name2 " + i, "value2 " + i ) );
	//		}
	//
	//		properties.putTestCaseProperties( testProperties2 );
	//
	//		properties.putTestCaseProperties( testProperties1 );
	//
	//		assertEquals( 10, properties.size() );
	//
	//		int i = 1;
	//		for( Property<?> s : properties.getData() )
	//		{
	//			assertEquals( "name " + i, s.getKey() );
	//			assertEquals( "value " + i, s.getValue() );
	//			i++ ;
	//		}
	//
	//	}

	@Test
	public void OverridingTestCasePropertiesTest()
	{
		WsdlTestCase testCase = createTestCaseMock();

		Collection<Property<?>> overridingProperties = new ArrayList<>();

		overridingProperties
				.add( new TestingProperty<String>( String.class, OVERRIDING_VALUE_PREFIX + "p1", "newValue1" ) );
		overridingProperties
				.add( new TestingProperty<String>( String.class, OVERRIDING_VALUE_PREFIX + "p4", "newValue4" ) );
		overridingProperties
				.add( new TestingProperty<String>( String.class, OVERRIDING_VALUE_PREFIX + "p5", "newValue5" ) );
		overridingProperties.add( new TestingProperty<String>( String.class, "p2", "newValue" ) );

		System.out.println( testCase.getPropertyList().size() );
		TestCasePropertiesNode.overrideTestCaseProperties( testCase, overridingProperties );

		List<TestProperty> data = testCase.getPropertyList();

		assertEquals( "p1", data.get( 0 ).getName() );
		assertEquals( "newValue1", data.get( 0 ).getValue() );

		assertEquals( "p2", data.get( 1 ).getName() );
		assertEquals( "valuep-2", data.get( 1 ).getValue() );

		assertEquals( "p3", data.get( 2 ).getName() );
		assertEquals( "valuep-3", data.get( 2 ).getValue() );

		assertEquals( "p4", data.get( 3 ).getName() );
		assertEquals( "newValue4", data.get( 3 ).getValue() );

		assertEquals( "p5", data.get( 4 ).getName() );
		assertEquals( "newValue5", data.get( 4 ).getValue() );
	}

}

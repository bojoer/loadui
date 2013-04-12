package com.eviware.loadui.component.soapui;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.junit.Test;
import org.mockito.Mockito;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.components.soapui.TestCasePropertiesNode;
import com.eviware.loadui.ui.fx.util.TestingProperty;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestProperty;

public class TestCasePropertiesTest
{

	@SuppressWarnings( "unchecked" )
	public ComponentContext createContextMock()
	{
		ComponentContext context = mock( ComponentContext.class );
		when( context.getProperties() ).thenReturn( Collections.<Property<?>> emptySet() );
		when( context.createProperty( Mockito.anyString(), ( Class<String> )Mockito.any( Class.class ) ) ).thenReturn(
				new TestingProperty<String>( String.class, "i am not", "testing Context" ) );

		return context;
	}

	@Test
	public void putTestCasePropertiesTest()
	{
		ComponentContext context = createContextMock();

		TestCasePropertiesNode properties = new TestCasePropertiesNode( "", context );

		List<TestProperty> testProperties = new ArrayList<>();

		for( int i = 1; i < 11; i++ )
		{
			testProperties.add( new TestTestProperty( "name " + i, "value " + i ) );
		}

		properties.putTestCaseProperties( testProperties );

		assertEquals( 10, properties.size() );

		int i = 1;
		for( Property<?> s : properties.getData() )
		{
			assertEquals( "name " + i, s.getKey() );
			assertEquals( "value " + i, s.getValue() );
			i++ ;
		}

	}

	@Test
	public void putTestCasePropertiesResetTest()
	{
		ComponentContext context = createContextMock();

		TestCasePropertiesNode properties = new TestCasePropertiesNode( "", context );

		List<TestProperty> testProperties1 = new ArrayList<>();

		for( int i = 1; i < 11; i++ )
		{
			testProperties1.add( new TestTestProperty( "name " + i, "value " + i ) );
		}

		List<TestProperty> testProperties2 = new ArrayList<>();

		for( int i = 1; i < 11; i++ )
		{
			testProperties2.add( new TestTestProperty( "name2 " + i, "value2 " + i ) );
		}

		properties.putTestCaseProperties( testProperties2 );

		properties.putTestCaseProperties( testProperties1 );

		assertEquals( 10, properties.size() );

		int i = 1;
		for( Property<?> s : properties.getData() )
		{
			assertEquals( "name " + i, s.getKey() );
			assertEquals( "value " + i, s.getValue() );
			i++ ;
		}

	}

	@Test
	public void loadOverridingPropertiesTest()
	{
		ComponentContext context = createContextMock();

		TestCasePropertiesNode properties = new TestCasePropertiesNode( "", context );

		List<TestProperty> testProperties = new ArrayList<>();

		testProperties.add( new TestTestProperty( "p1", "valuep1" ) );
		testProperties.add( new TestTestProperty( "p2", "valuep2" ) );
		testProperties.add( new TestTestProperty( "p3", "valuep3" ) );
		testProperties.add( new TestTestProperty( "p4", "valuep4" ) );
		testProperties.add( new TestTestProperty( "p5", "valuep5" ) );

		properties.putTestCaseProperties( testProperties );

		Collection<Property<?>> overridingProperties = new ArrayList<>();

		overridingProperties.add( new TestingProperty<String>( String.class, "_valueToOverride_p1", "valuep-1" ) );
		overridingProperties.add( new TestingProperty<String>( String.class, "p2", "valuep-7" ) );
		overridingProperties.add( new TestingProperty<String>( String.class, "p3", "valuep-8" ) );
		overridingProperties.add( new TestingProperty<String>( String.class, "_valueToOverride_p4", "valuep-4" ) );
		overridingProperties.add( new TestingProperty<String>( String.class, "_valueToOverride_p5", "valuep-5" ) );
		overridingProperties.add( new TestingProperty<String>( String.class, "p6", "valuep-6" ) );

		properties.loadOverridingProperties( overridingProperties );

		assertEquals( 5, properties.size() );

		Property<?>[] data = ( Property<?>[] )properties.getData().toArray();

		assertEquals( 5, properties.getData().size() );

		assertEquals( "p1", data[0].getKey() );
		assertEquals( "valuep-1", data[0].getValue() );

		assertEquals( "p2", data[1].getKey() );
		assertEquals( "valuep2", data[1].getValue() );

		assertEquals( "p3", data[2].getKey() );
		assertEquals( "valuep3", data[2].getValue() );

		assertEquals( "p4", data[3].getKey() );
		assertEquals( "valuep-4", data[3].getValue() );

		assertEquals( "p5", data[4].getKey() );
		assertEquals( "valuep-5", data[4].getValue() );

	}

	private class TestTestProperty implements TestProperty
	{

		private String name;
		private String value;

		public TestTestProperty( String name, String value )
		{
			this.name = name;
			this.value = value;

		}

		@Override
		public String getDefaultValue()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDescription()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ModelItem getModelItem()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public SchemaType getSchemaType()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QName getType()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getValue()
		{
			return value;
		}

		@Override
		public boolean isReadOnly()
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isRequestPart()
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setValue( String arg0 )
		{
			// TODO Auto-generated method stub

		}

	}
}

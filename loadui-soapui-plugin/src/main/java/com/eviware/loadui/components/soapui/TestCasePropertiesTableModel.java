package com.eviware.loadui.components.soapui;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.api.terminal.TerminalMessage;
import com.eviware.loadui.api.ui.table.StringToStringTableModel;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class TestCasePropertiesTableModel extends StringToStringTableModel
{
	public static TestCasePropertiesTableModel newInstance( ComponentContext context )
	{
		TestCasePropertiesTableModel t = new TestCasePropertiesTableModel();
		t.addTableModelListener( t.new PropertiesTableListener( context ) );
		return t;
	}

	private TestCasePropertiesTableModel()
	{
	}

	public void putTestCaseProperties( List<TestProperty> soapUiTestCaseProperties )
	{
		clear();
		for( TestProperty tp : soapUiTestCaseProperties )
		{
			if( !propertyAlreadyExists( tp.getName() ) )
			{
				addRow( tp.getName(), tp.getValue() );
			}
		}
		removeAllPropertiesExcept( soapUiTestCaseProperties );
	}

	/**
	 * Applies properties to a TestCase from an incoming message.
	 * 
	 * @param testCase
	 * @param triggerMessage
	 */

	public void overrideTestCaseProperties( WsdlTestCase testCase, TerminalMessage triggerMessage )
	{
		for( int i = 0; i < getRowCount(); i++ )
		{
			testCase.setPropertyValue( ( String )getValueAt( i, 0 ), ( String )getValueAt( i, 1 ) );
		}

		for( String name : testCase.getPropertyNames() )
		{
			if( triggerMessage.containsKey( name ) )
				testCase.setPropertyValue( name, String.valueOf( triggerMessage.get( name ) ) );
		}
	}

	public void loadOverridingProperties( Collection<Property<?>> properties )
	{
		// fetch overriding values for testcase properties
		for( int i = 0; i < getRowCount(); i++ )
		{
			// check if property for overridden value already exists
			for( Property<?> p : properties )
			{
				if( p.getKey().equals( "_valueToOverride_" + getValueAt( i, 0 ) ) )
				{
					String propertyValue = null;
					if( p.getValue() != null )
					{
						propertyValue = p.getValue().toString();
					}
					setValueAt( propertyValue, i, 1 );
				}
			}
		}
	}

	private boolean propertyAlreadyExists( String name )
	{
		for( int i = 0; i < getRowCount(); i++ )
		{
			if( ( ( String )getValueAt( i, 0 ) ).equals( name ) )
			{
				return true;
			}
		}
		return false;
	}

	private void removeAllPropertiesExcept( List<TestProperty> soapUiTestCaseProperties )
	{
		int rowCount = getRowCount();
		List<String> obsoleteProperties = new LinkedList<String>();

		for( int i = 0; i < rowCount; i++ )
		{
			final String name = ( String )getValueAt( i, 0 );
			if( !Iterables.any( soapUiTestCaseProperties, new Predicate<TestProperty>()
			{
				@Override
				public boolean apply( TestProperty p )
				{
					return p.getName().equals( name );
				}

			} ) )
			{
				obsoleteProperties.add( name );
			}
		}

		for( String name : obsoleteProperties )
		{
			removeRow( name );
		}
	}

	private class PropertiesTableListener implements TableModelListener
	{
		private final ComponentContext context;

		public PropertiesTableListener( ComponentContext context )
		{
			this.context = context;
		}

		@Override
		public void tableChanged( TableModelEvent e )
		{
			for( int i = 0; i < getRowCount(); i++ )
			{
				// check if property for overridden value already exists
				boolean properyExisted = false;
				for( Property<?> p : context.getProperties() )
				{
					if( p.getKey().equals( "_valueToOverride_" + getValueAt( i, 0 ) ) )
					{
						p.setValue( getValueAt( i, 1 ) );
						properyExisted = true;
						break;
					}
				}

				if( !properyExisted )
				{
					Property<?> p = context.createProperty( "_valueToOverride_" + getValueAt( i, 0 ).toString(),
							String.class );
					Object value = getValueAt( i, 1 );
					p.setValue( Objects.firstNonNull( value.toString(), "" ) );
				}
			}

		}
	}

}

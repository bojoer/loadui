package com.eviware.loadui.component.soapui;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestProperty;

	public class TestTestProperty implements TestProperty
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
		public void setValue( String value )
		{
			this.value = value;
		}

		

	}
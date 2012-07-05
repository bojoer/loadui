package com.eviware.loadui.components.soapui;

import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunListener;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCase;

class DummyLoadTest implements LoadTest
{
	/**
	 * 
	 */
	private final SoapUISamplerComponent soapUISamplerComponent;

	/**
	 * @param soapUISamplerComponent
	 */
	DummyLoadTest( SoapUISamplerComponent soapUISamplerComponent )
	{
		this.soapUISamplerComponent = soapUISamplerComponent;
	}

	private final Set<LoadTestRunListener> loadTestRunListeners = new HashSet<LoadTestRunListener>();
	private LoadTestRunListener[] loadTestRunListenersArray;

	@Override
	public TestCase getTestCase()
	{
		return this.soapUISamplerComponent.soapuiTestCase;
	}

	@Override
	public void addLoadTestRunListener( LoadTestRunListener listener )
	{
		loadTestRunListeners.add( listener );
	}

	@Override
	public void removeLoadTestRunListener( LoadTestRunListener listener )
	{
		loadTestRunListeners.remove( listener );
	}

	public LoadTestRunListener[] getLoadTestRunListeners()
	{
		if( loadTestRunListenersArray == null )
		{
			loadTestRunListenersArray = loadTestRunListeners
					.toArray( new LoadTestRunListener[loadTestRunListeners.size()] );
		}

		return loadTestRunListenersArray;
	}

	@Override
	public LoadTestRunner run()
	{
		return null;
	}

	@Override
	public List<? extends ModelItem> getChildren()
	{
		return null;
	}

	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getId()
	{
		return soapUISamplerComponent.getId();
	}

	@Override
	public String getName()
	{
		return soapUISamplerComponent.getLabel();
	}

	@Override
	public ModelItem getParent()
	{
		return null;
	}

	@Override
	public Settings getSettings()
	{
		return this.soapUISamplerComponent.soapuiTestCase.getSettings();
	}

	@Override
	public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
	}

	@Override
	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
	}

	@Override
	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
	}

	@Override
	public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
	}
}
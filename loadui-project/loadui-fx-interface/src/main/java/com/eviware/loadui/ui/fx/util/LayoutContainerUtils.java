package com.eviware.loadui.ui.fx.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.eviware.loadui.api.layout.ActionLayoutComponent;
import com.eviware.loadui.api.layout.LayoutComponent;
import com.eviware.loadui.api.layout.LayoutContainer;
import com.eviware.loadui.api.layout.PropertyLayoutComponent;
import com.eviware.loadui.api.layout.SettingsLayoutContainer;
import com.eviware.loadui.ui.fx.control.SettingsTab;
import com.eviware.loadui.ui.fx.control.SettingsTab.Builder;

public class LayoutContainerUtils
{
	public static List<SettingsTab> settingsTabsFromLayoutContainers(
			Collection<? extends SettingsLayoutContainer> containers )
	{
		List<SettingsTab> tabs = new LinkedList<>();
		for( SettingsLayoutContainer layoutContainer : containers )
		{
			Builder tabBuilder = Builder.create( layoutContainer.getLabel() );
			layoutContainerToField( layoutContainer, tabBuilder );
			tabs.add( tabBuilder.build() );
		}
		return tabs;
	}

	private static void layoutContainerToField( LayoutContainer container, Builder tabBuilder )
	{
		for( LayoutComponent component : container )
		{
			if( component instanceof PropertyLayoutComponent )
			{
				PropertyLayoutComponent<?> propertyComponent = ( PropertyLayoutComponent<?> )component;
				tabBuilder.field( propertyComponent.getLabel(), propertyComponent.getProperty() );
			}
			else if( component instanceof ActionLayoutComponent)
			{
				ActionLayoutComponent action = (ActionLayoutComponent) component;
				tabBuilder.button( action );
			}
			else if( component instanceof LayoutContainer )
			{
				layoutContainerToField( ( LayoutContainer )component, tabBuilder );
			}
		}
	}
}

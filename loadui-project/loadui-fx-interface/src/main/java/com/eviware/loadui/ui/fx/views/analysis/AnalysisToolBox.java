package com.eviware.loadui.ui.fx.views.analysis;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fx;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.ui.fx.control.ToolBox;
import com.eviware.loadui.ui.fx.util.ObservableLists;
import com.eviware.loadui.ui.fx.views.assertions.AssertionToolboxItem;
import com.eviware.loadui.ui.fx.views.assertions.AssertionUtils;
import com.eviware.loadui.ui.fx.views.statistics.StatisticHolderToolBox;
import com.google.common.base.Function;

public class AnalysisToolBox extends StatisticHolderToolBox
{
	private static final String ASSERTION_CATEGORY = "Assertions";

	@SuppressWarnings( "rawtypes" )
	private static final Function<AssertionItem, AssertionToolboxItem> ASSERTION_TO_VIEW = new Function<AssertionItem, AssertionToolboxItem>()
	{
		@Override
		public AssertionToolboxItem apply( AssertionItem input )
		{
			AssertionToolboxItem assertionToolboxItem = new AssertionToolboxItem( input );
			ToolBox.setCategory( assertionToolboxItem, ASSERTION_CATEGORY );

			return assertionToolboxItem;
		}
	};

	private final Property<ProjectItem> project = new SimpleObjectProperty<>( this, "project" );
	private ObservableList<AssertionToolboxItem> assertionViews;
	private ObservableList<? extends Node> items;

	public AnalysisToolBox()
	{
		project.addListener( new ChangeListener<ProjectItem>()
		{
			@Override
			public void changed( ObservableValue<? extends ProjectItem> arg0, ProjectItem oldProject,
					ProjectItem newProject )
			{
				if( oldProject != null )
				{
					Bindings.unbindContent( getItems(), items );
				}
				if( newProject != null )
				{
					assertionViews = transform( fx( AssertionUtils.assertions( newProject ) ), ASSERTION_TO_VIEW );
					items = ObservableLists.concat( getStatisticHolderToolboxItems(), assertionViews );
					Bindings.bindContent( getItems(), items );
				}
			}
		} );

		Bindings.unbindContent( getItems(), getStatisticHolderToolboxItems() );
	}

	public void setProject( ProjectItem value )
	{
		project.setValue( value );
	}

	public ProjectItem getProject()
	{
		return project.getValue();
	}

	public Property<ProjectItem> projectProperty()
	{
		return project;
	}
}

package com.eviware.loadui.ui.fx.views.assertions;

import static com.eviware.loadui.ui.fx.util.ObservableLists.appendElement;
import static com.eviware.loadui.ui.fx.util.ObservableLists.concat;
import static com.eviware.loadui.ui.fx.util.ObservableLists.ofCollection;
import static com.eviware.loadui.ui.fx.util.ObservableLists.transform;
import javafx.collections.ObservableList;

import com.eviware.loadui.api.assertion.AssertionAddon;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.SceneItem;
import com.google.common.base.Function;

public class AssertionUtils
{
	@SuppressWarnings( "rawtypes" )
	public static ObservableList<AssertionItem> assertions( ProjectItem project )
	{
		return concat( transform(
				appendElement( ofCollection( project, ProjectItem.SCENES, SceneItem.class, project.getChildren() ), project ),
				new Function<CanvasItem, ObservableList<AssertionItem>>()
				{
					@Override
					public ObservableList<AssertionItem> apply( CanvasItem canvas )
					{
						return ofCollection( canvas, AssertionAddon.ASSERTION_ITEMS, AssertionItem.class,
								canvas.getAddon( AssertionAddon.class ).getAssertions() );
					}
				} ) );
	}
}
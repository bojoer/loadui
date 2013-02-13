package com.eviware.loadui.ui.fx.api;

import javax.annotation.CheckForNull;

import javafx.scene.image.Image;

/**
 * An ImageResolver can find the Images representing some Objects.
 * 
 * @author Henrik
 * 
 */

public interface ImageResolver
{
	@CheckForNull
	Image getImageFor( Object object );
}

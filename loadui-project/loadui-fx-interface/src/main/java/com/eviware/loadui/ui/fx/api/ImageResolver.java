package com.eviware.loadui.ui.fx.api;

import javax.annotation.Nullable;

import javafx.scene.image.Image;

public interface ImageResolver
{
	@Nullable
	public Image resolveImageFor( Object object );
}

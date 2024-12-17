/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.nebula.gallery;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.widgets.gallery.GalleryItem;

/**
 * Model {@link GalleryItem}.
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class GalleryItemInfo extends ItemInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GalleryItemInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected GalleryItem getWidget() {
		return (GalleryItem) super.getWidget();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Orientation
	//
	////////////////////////////////////////////////////////////////////////////
	public boolean isHorizontal() throws Exception {
		ObjectInfo parent = getParent();
		if (parent instanceof GalleryItemInfo) {
			return ((GalleryItemInfo) parent).isHorizontal();
		} else {
			return !((GalleryInfo) parent).isHorizontal();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		{
			Rectangle bounds = getComponentBounds();
			if (getParent() instanceof GalleryItemInfo) {
				GalleryItemInfo parent = (GalleryItemInfo) getParent();
				Rectangle parentBounds = parent.getComponentBounds();
				bounds.performTranslate(-parentBounds.x, -parentBounds.y);
			}
			setModelBounds(bounds);
		}
		// continue in super()
		super.refresh_fetch();
	}

	private Rectangle getComponentBounds() throws Exception {
		return new Rectangle(getWidget().getBounds());
	}

	public boolean isGroupItem() {
		return !(getParent() instanceof GalleryItemInfo);
	}
}

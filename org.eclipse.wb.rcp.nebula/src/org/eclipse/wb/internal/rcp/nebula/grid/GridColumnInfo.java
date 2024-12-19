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
package org.eclipse.wb.internal.rcp.nebula.grid;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.widgets.grid.GridColumn;

/**
 * Model {@link GridColumn}.
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class GridColumnInfo extends ItemInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridColumnInfo(AstEditor editor,
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
	public GridColumn getWidget() {
		return (GridColumn) getObject();
	}

	/**
	 * Convenience method that calls the package-private {@code getBounds()} method
	 * of the {@link GridColumn}.
	 */
	protected org.eclipse.swt.graphics.Rectangle getSwtBounds() throws Exception {
		return (org.eclipse.swt.graphics.Rectangle) ReflectionUtils.invokeMethod(getObject(), "getBounds()");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		{
			Rectangle bounds = new Rectangle(getSwtBounds());
			if (getParent() instanceof GridColumnGroupInfo parent) {
				Rectangle parentBounds = new Rectangle(parent.getSwtBounds());
				bounds.performTranslate(-parentBounds.x, -parentBounds.y);
			}
			setModelBounds(bounds);
		}
		// continue in super()
		super.refresh_fetch();
	}
}

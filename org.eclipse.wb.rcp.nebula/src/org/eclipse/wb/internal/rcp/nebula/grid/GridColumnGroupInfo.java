/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.nebula.widgets.grid.GridColumnGroup;

import java.util.List;

/**
 * Model {@link GridColumnGroup}.
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class GridColumnGroupInfo extends ItemInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridColumnGroupInfo(AstEditor editor,
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
	public GridColumnGroup getWidget() {
		return (GridColumnGroup) getObject();
	}

	/**
	 * Convenience method that calls the package-private {@code getBounds()} method
	 * of the {@link GridColumnGroup}.
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
			List<GridColumnInfo> columns = getChildren(GridColumnInfo.class);
			for (GridColumnInfo column : columns) {
				bounds.union(new Rectangle(column.getSwtBounds()));
			}
			setModelBounds(bounds);
		}
		// continue in super()
		super.refresh_fetch();
	}
}

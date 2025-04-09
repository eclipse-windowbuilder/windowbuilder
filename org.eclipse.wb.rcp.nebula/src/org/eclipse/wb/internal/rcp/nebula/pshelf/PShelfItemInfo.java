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
package org.eclipse.wb.internal.rcp.nebula.pshelf;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;

import org.eclipse.nebula.widgets.pshelf.PShelfItem;

/**
 * Model for {@link PShelfItem}.
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class PShelfItemInfo extends ItemInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PShelfItemInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	@Override
	public PShelfItem getWidget() {
		return (PShelfItem) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getComponentObject() {
		return ReflectionUtils.getFieldObject(getObject(), "bodyParent");
	}

	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		fixBodyBounds();
	}

	/**
	 * "Body" control is located on "bodyParent", but we consider "body" as child of this item and in
	 * {@link #fetchItemBounds()} we move bounds of item up. So, we have to move bounds of "body"
	 * down.
	 */
	private void fixBodyBounds() {
		int itemHeight = getItemHeight();
		ControlInfo body = getChildren(ControlInfo.class).get(0);
		body.getBounds().performTranslate(0, itemHeight);
		body.getModelBounds().performTranslate(0, itemHeight);
	}

	private int getItemHeight() {
		Object shelfObject = ReflectionUtils.getFieldObject(getObject(), "parent");
		return ReflectionUtils.getFieldInt(shelfObject, "itemHeight");
	}
}

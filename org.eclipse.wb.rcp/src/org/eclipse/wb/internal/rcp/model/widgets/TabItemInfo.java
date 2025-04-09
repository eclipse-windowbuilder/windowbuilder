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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.TabItem;

/**
 * Model for {@link TabItem}.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage rcp.model.widgets
 */
public final class TabItemInfo extends AbstractTabItemInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TabItemInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	@Override
	public TabItem getWidget() {
		return (TabItem) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		// set bounds
		{
			Rectangle bounds = new Rectangle(OSSupport.get().getTabItemBounds(getObject()));
			setModelBounds(bounds);
		}
	}
}

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
package org.eclipse.wb.internal.rcp.nebula.radiogroup;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.widgets.radiogroup.RadioItem;
import org.eclipse.swt.widgets.Button;

/**
 * Model for {@link RadioItem}.
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class RadioItemInfo extends ItemInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RadioItemInfo(AstEditor editor,
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
	public RadioItem getWidget() {
		return (RadioItem) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		fixBodyBounds();
	}

	/**
	 * Get bounds from inner {@link Button} widget.
	 */
	private void fixBodyBounds() throws Exception {
		Rectangle bounds = new Rectangle(getWidget().getButton().getBounds());
		setModelBounds(bounds);
	}
}

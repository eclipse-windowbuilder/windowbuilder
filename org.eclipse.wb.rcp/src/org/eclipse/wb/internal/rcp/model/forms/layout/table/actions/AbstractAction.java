/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.forms.layout.table.actions;

import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.ITableWrapDataInfo;

import org.eclipse.jface.action.Action;

/**
 * Abstract {@link Action} for modifying horizontal/vertical part of {@link ITableWrapDataInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
abstract class AbstractAction extends ObjectInfoAction {
	protected final ITableWrapDataInfo m_layoutData;
	protected final boolean m_horizontal;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractAction(ITableWrapDataInfo layoutData,
			String text,
			int style,
			String iconPath,
			boolean horizontal) {
		super(layoutData.getUnderlyingModel(), text, style);
		m_layoutData = layoutData;
		m_horizontal = horizontal;
		// set image
		if (iconPath != null) {
			String path = "info/layout/TableWrapLayout/" + (horizontal ? "h" : "v") + "/menu/" + iconPath;
			setImageDescriptor(Activator.getImageDescriptor(path));
		}
	}
}
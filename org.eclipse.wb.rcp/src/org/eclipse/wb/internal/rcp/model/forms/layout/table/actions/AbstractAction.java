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
package org.eclipse.wb.internal.rcp.model.forms.layout.table.actions;

import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.ITableWrapDataInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

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
			ImageDescriptor icon,
			boolean horizontal) {
		super(layoutData.getUnderlyingModel(), text, style);
		m_layoutData = layoutData;
		m_horizontal = horizontal;
		setImageDescriptor(icon);
	}
}
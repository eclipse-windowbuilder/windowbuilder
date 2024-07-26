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
package org.eclipse.wb.internal.swt.model.layout.grid.actions;

import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swt.model.layout.grid.IGridDataInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Abstract {@link Action} for modifying horizontal/vertical part of {@link IGridDataInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
abstract class AbstractAction extends ObjectInfoAction {
	protected final IGridDataInfo m_gridData;
	protected final boolean m_horizontal;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractAction(IGridDataInfo gridData,
			String text,
			int style,
			ImageDescriptor icon,
			boolean horizontal) {
		super(gridData.getUnderlyingModel(), text, style);
		m_gridData = gridData;
		m_horizontal = horizontal;
		// set image
		setImageDescriptor(icon);
	}
}
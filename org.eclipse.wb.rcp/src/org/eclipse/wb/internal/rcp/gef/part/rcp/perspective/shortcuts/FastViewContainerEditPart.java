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
package org.eclipse.wb.internal.rcp.gef.part.rcp.perspective.shortcuts;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.shortcuts.FastViewContainerLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.FastViewContainerInfo;

/**
 * {@link EditPart} for {@link FastViewContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class FastViewContainerEditPart extends AbstractShortcutContainerEditPart {
	private final FastViewContainerInfo m_container;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FastViewContainerEditPart(FastViewContainerInfo container) {
		super(container);
		m_container = container;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(new FastViewContainerLayoutEditPolicy(m_container));
	}
}

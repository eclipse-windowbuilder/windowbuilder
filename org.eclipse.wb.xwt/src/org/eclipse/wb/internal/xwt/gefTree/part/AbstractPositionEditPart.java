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
package org.eclipse.wb.internal.xwt.gefTree.part;

import org.eclipse.wb.core.gefTree.part.ObjectEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.xwt.gefTree.policy.AbstractPositionLayoutEditPolicy;
import org.eclipse.wb.internal.xwt.model.widgets.AbstractPositionInfo;

/**
 * {@link EditPart} for {@link AbstractPositionInfo}.
 *
 * @author scheglov_ke
 * @coverage XWT.gefTree.part
 */
public final class AbstractPositionEditPart extends ObjectEditPart {
	private final AbstractPositionInfo m_position;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractPositionEditPart(AbstractPositionInfo position) {
		super(position);
		m_position = position;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refreshEditPolicies() {
		super.refreshEditPolicies();
		installEditPolicy(new AbstractPositionLayoutEditPolicy(m_position));
	}
}

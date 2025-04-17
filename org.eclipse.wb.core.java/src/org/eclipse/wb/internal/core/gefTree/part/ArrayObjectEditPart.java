/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.core.gefTree.part;

import org.eclipse.wb.core.gefTree.part.ObjectEditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.gefTree.policy.ArrayObjectLayoutEditPolicy;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;

/**
 * {@link TreeEditPart} for {@link AbstractArrayObjectInfo}.
 *
 * @author sablin_aa
 * @coverage core.gefTree
 */
public final class ArrayObjectEditPart extends ObjectEditPart {
	private final AbstractArrayObjectInfo m_arrayInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ArrayObjectEditPart(AbstractArrayObjectInfo object) {
		super(object);
		m_arrayInfo = object;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ArrayObjectLayoutEditPolicy(m_arrayInfo));
	}
}

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
package org.eclipse.wb.internal.swt.gefTree.part;

import org.eclipse.wb.core.gefTree.policy.layout.LayoutPolicyUtils;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.gefTree.policy.DropLayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

/**
 * {@link EditPart} for {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage swt.gefTree.part
 */
public class CompositeEditPart extends ControlEditPart {
	private final CompositeInfo m_composite;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CompositeEditPart(CompositeInfo composite) {
		super(composite);
		m_composite = composite;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	private LayoutInfo m_currentLayout;

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		// support for dropping LayoutInfo's
		if (m_composite.hasLayout()) {
			installEditPolicy(new DropLayoutEditPolicy(m_composite));
		}
	}

	@Override
	protected void refreshEditPolicies() {
		super.refreshEditPolicies();
		// support for dropping components
		if (m_composite.hasLayout()) {
			LayoutInfo layout = m_composite.getLayout();
			if (layout != m_currentLayout) {
				LayoutEditPolicy policy = LayoutPolicyUtils.createLayoutEditPolicy(this, layout);
				if (policy != null) {
					m_currentLayout = layout;
					installEditPolicy(EditPolicy.LAYOUT_ROLE, policy);
				}
			}
		}
	}
}

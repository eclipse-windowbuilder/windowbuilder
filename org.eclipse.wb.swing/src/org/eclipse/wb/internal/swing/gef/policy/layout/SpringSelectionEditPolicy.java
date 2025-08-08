/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.gef.policy.layout;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteComplexSelectionEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.snapping.ComponentAttachmentInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.spring.SpringLayoutInfo;

import org.eclipse.swt.layout.FormLayout;

/**
 * Selection policy for edit containers with {@link FormLayout}.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class SpringSelectionEditPolicy extends AbsoluteComplexSelectionEditPolicy<ComponentInfo> {
	private final SpringLayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SpringSelectionEditPolicy(SpringLayoutInfo layout) {
		super(layout);
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Overrides
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	@Deprecated
	protected ComponentAttachmentInfo getComponentAttachmentInfo(IAbstractComponentInfo widget,
			int side) throws Exception {
		return m_layout.getComponentAttachmentInfo(widget, side);
	}

	@Override
	protected void showSelection() {
		super.showSelection();
	}

	@Override
	protected void hideSelection() {
		super.hideSelection();
	}
}

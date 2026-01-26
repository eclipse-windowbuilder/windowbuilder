/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.swing2swt.gef;

import org.eclipse.wb.core.gef.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.swing2swt.layout.BorderLayoutInfo;

import org.eclipse.gef.EditPart;

/**
 * Implementation of {@link ILayoutEditPolicyFactory} for Swing2SWT.
 *
 * @author scheglov_ke
 * @coverage rcp.swing2swt.gef
 */
public final class LayoutEditPolicyFactory implements ILayoutEditPolicyFactory {
	////////////////////////////////////////////////////////////////////////////
	//
	// ILayoutEditPolicyFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
		if (model instanceof BorderLayoutInfo) {
			return new BorderLayoutEditPolicy((BorderLayoutInfo) model);
		}
		// not found
		return null;
	}
}

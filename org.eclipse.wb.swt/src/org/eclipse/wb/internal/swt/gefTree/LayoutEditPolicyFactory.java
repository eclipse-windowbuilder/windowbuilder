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
package org.eclipse.wb.internal.swt.gefTree;

import org.eclipse.wb.core.gefTree.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.gefTree.policy.AbsoluteLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gefTree.policy.FormLayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplAutomatic;

import org.eclipse.gef.EditPart;

/**
 * Implementation of {@link ILayoutEditPolicyFactory} for SWT.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage swt.gefTree
 */
public final class LayoutEditPolicyFactory implements ILayoutEditPolicyFactory {
	////////////////////////////////////////////////////////////////////////////
	//
	// ILayoutEditPolicyFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
		if (model instanceof AbsoluteLayoutInfo) {
			return new AbsoluteLayoutEditPolicy<>((AbsoluteLayoutInfo) model);
		}
		if (model instanceof FormLayoutInfo formLayoutInfo) {
			if (formLayoutInfo.getImpl() instanceof FormLayoutInfoImplAutomatic) {
				return new FormLayoutEditPolicy<>(formLayoutInfo);
			} else {
				// TODO:
			}
		}
		return null;
	}
}